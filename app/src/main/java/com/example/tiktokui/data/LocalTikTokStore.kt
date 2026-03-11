package com.example.tiktokui.data

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.MediaStore
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

data class AppPersistedState(
    val videos: List<StoredVideo> = emptyList(),
    val folders: List<SelectedFolder> = emptyList(),
    val inboxLinks: List<SharedLinkItem> = emptyList(),
    val playOrder: PlayOrder = PlayOrder.Sequential,
    val videoSourceMode: VideoSourceMode = VideoSourceMode.Folders
)

data class StoredVideo(
    val id: String,
    val localPath: String,
    val sourceUri: String?,
    val sourceFolderUri: String?,
    val displayName: String,
    val caption: String,
    val comments: List<String> = emptyList()
)

data class SelectedFolder(
    val uri: String,
    val name: String
)

data class SharedLinkItem(
    val id: String,
    val url: String,
    val receivedAt: Long
)

enum class PlayOrder {
    Sequential,
    Shuffle
}

enum class VideoSourceMode {
    All,
    Folders
}

class LocalTikTokStore(private val context: Context) {
    private val prefs = context.getSharedPreferences("local_tiktok_store", Context.MODE_PRIVATE)
    private val libraryDirName = "LocalTikTok"
    private val favoritesDirName = "LocalTikTok Favorite"

    fun load(): AppPersistedState {
        val raw = prefs.getString("app_state", null) ?: return AppPersistedState()
        return runCatching {
            val json = JSONObject(raw)
            AppPersistedState(
                videos = json.optJSONArray("videos").toStoredVideos(),
                folders = json.optJSONArray("folders").toFolders(),
                inboxLinks = json.optJSONArray("inbox").toInboxLinks(),
                playOrder = PlayOrder.valueOf(json.optString("play_order", PlayOrder.Sequential.name)),
                videoSourceMode = VideoSourceMode.valueOf(json.optString("video_source_mode", VideoSourceMode.Folders.name))
            )
        }.getOrDefault(AppPersistedState())
    }

    fun save(state: AppPersistedState) {
        val json = JSONObject().apply {
            put("play_order", state.playOrder.name)
            put("video_source_mode", state.videoSourceMode.name)
            put("videos", JSONArray().apply {
                state.videos.forEach { video ->
                    put(JSONObject().apply {
                        put("id", video.id)
                        put("local_path", video.localPath)
                        put("source_uri", video.sourceUri)
                        put("source_folder_uri", video.sourceFolderUri)
                        put("display_name", video.displayName)
                        put("caption", video.caption)
                        put("comments", JSONArray(video.comments))
                    })
                }
            })
            put("folders", JSONArray().apply {
                state.folders.forEach { folder ->
                    put(JSONObject().apply {
                        put("uri", folder.uri)
                        put("name", folder.name)
                    })
                }
            })
            put("inbox", JSONArray().apply {
                state.inboxLinks.forEach { link ->
                    put(JSONObject().apply {
                        put("id", link.id)
                        put("url", link.url)
                        put("received_at", link.receivedAt)
                    })
                }
            })
        }
        prefs.edit().putString("app_state", json.toString()).apply()
    }

    fun copyIntoLibrary(source: Uri, preferredName: String): String {
        val targetDir = File(context.filesDir, libraryDirName).apply { mkdirs() }
        val safeName = preferredName.ifBlank { "video_${System.currentTimeMillis()}.mp4" }
        val target = createUniqueTargetFile(targetDir, safeName)
        context.contentResolver.openInputStream(source)?.use { input ->
            FileOutputStream(target).use { output -> input.copyTo(output) }
        }
        return target.absolutePath
    }

    fun favoriteVideo(video: StoredVideo, onProgress: (Int) -> Unit = {}): StoredVideo {
        val targetDir = File(context.filesDir, favoritesDirName).apply { mkdirs() }
        val safeName = video.displayName.ifBlank { "favorite_${System.currentTimeMillis()}.mp4" }
        val sourceUri = video.sourceUri?.takeIf { it.isNotBlank() }?.let(Uri::parse)
        val currentFile = video.localPath
            .takeIf { it.isNotBlank() && !it.startsWith("content://") && !it.startsWith("file://") }
            ?.let(::File)
        val sourceSize = when {
            currentFile != null && currentFile.exists() -> currentFile.length()
            sourceUri != null -> resolveContentLength(sourceUri)
            else -> -1L
        }

        if (currentFile?.parentFile?.absolutePath == targetDir.absolutePath) {
            onProgress(100)
            return video
        }

        findExistingFavorite(targetDir, safeName, sourceSize)?.let { existingFavorite ->
            onProgress(100)
            return video.copy(localPath = existingFavorite.absolutePath)
        }

        val target = createUniqueTargetFile(targetDir, safeName)

        when {
            currentFile != null && currentFile.exists() -> {
                if (!currentFile.renameTo(target)) {
                    currentFile.inputStream().use { input ->
                        FileOutputStream(target).use { output ->
                            copyWithProgress(
                                input = BufferedInputStream(input),
                                output = output,
                                totalBytes = currentFile.length(),
                                onProgress = onProgress
                            )
                        }
                    }
                    if (!currentFile.delete()) {
                        throw IOException("Unable to delete original file after favoriting")
                    }
                }
                onProgress(100)
            }
            sourceUri != null -> {
                context.contentResolver.openInputStream(sourceUri)?.use { input ->
                    FileOutputStream(target).use { output ->
                        copyWithProgress(
                            input = BufferedInputStream(input),
                            output = output,
                            totalBytes = resolveContentLength(sourceUri),
                            onProgress = onProgress
                        )
                    }
                } ?: throw IOException("Unable to open source video for favoriting")
                deleteSourceIfPossible(sourceUri)
                onProgress(100)
            }
            else -> throw IOException("Video source is unavailable")
        }

        return video.copy(localPath = target.absolutePath)
    }

    fun takePersistablePermission(uri: Uri) {
        runCatching {
            context.contentResolver.takePersistableUriPermission(
                uri,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
        }
    }

    fun resolveDisplayName(uri: Uri): String {
        val docUri = if (DocumentsContract.isTreeUri(uri)) {
            DocumentsContract.buildDocumentUriUsingTree(uri, DocumentsContract.getTreeDocumentId(uri))
        } else {
            uri
        }
        return runCatching {
            context.contentResolver.query(docUri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor ->
                    if (cursor.moveToFirst()) cursor.getString(0) else null
                }
        }.getOrNull() ?: uri.lastPathSegment?.substringAfterLast('/') ?: "Folder"
    }

    fun scanFolderVideos(treeUri: Uri): List<FolderVideoSource> {
        val results = mutableListOf<FolderVideoSource>()
        scanChildren(treeUri, treeUri, results)
        return results
    }

    fun scanAllDeviceVideos(): List<FolderVideoSource> {
        val collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME
        )
        val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"
        val results = mutableListOf<FolderVideoSource>()
        context.contentResolver.query(collection, projection, null, null, sortOrder)?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idIndex)
                val name = cursor.getString(nameIndex) ?: "video_$id.mp4"
                val uri = Uri.withAppendedPath(collection, id.toString())
                results += FolderVideoSource(uri = uri, displayName = name)
            }
        }
        return results
    }

    private fun scanChildren(rootTreeUri: Uri, treeUri: Uri, sink: MutableList<FolderVideoSource>) {
        val documentId = DocumentsContract.getTreeDocumentId(treeUri)
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, documentId)
        context.contentResolver.query(
            childrenUri,
            arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE
            ),
            null,
            null,
            null
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val childId = cursor.getString(0)
                val name = cursor.getString(1) ?: "video"
                val mimeType = cursor.getString(2)
                val childUri = DocumentsContract.buildDocumentUriUsingTree(rootTreeUri, childId)
                when {
                    mimeType == DocumentsContract.Document.MIME_TYPE_DIR -> scanChildren(rootTreeUri, childUri, sink)
                    mimeType?.startsWith("video/") == true || name.endsWith(".mp4", true) || name.endsWith(".webm", true) || name.endsWith(".mkv", true) -> {
                        sink += FolderVideoSource(uri = childUri, displayName = name)
                    }
                }
            }
        }
    }

    private fun createUniqueTargetFile(targetDir: File, preferredName: String): File {
        val safeName = preferredName.ifBlank { "video_${System.currentTimeMillis()}.mp4" }
        val extension = safeName.substringAfterLast('.', "mp4")
        val baseName = safeName.substringBeforeLast('.', safeName)
        var target = File(targetDir, "$baseName.$extension")
        var counter = 1
        while (target.exists()) {
            target = File(targetDir, "${baseName}_$counter.$extension")
            counter++
        }
        return target
    }

    private fun findExistingFavorite(targetDir: File, preferredName: String, sourceSize: Long): File? {
        val normalizedName = preferredName.trim().lowercase()
        return targetDir.listFiles()?.firstOrNull { file ->
            file.isFile &&
                file.name.trim().lowercase() == normalizedName &&
                (sourceSize <= 0L || file.length() == sourceSize)
        }
    }

    private fun resolveContentLength(uri: Uri): Long {
        return runCatching {
            context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { descriptor ->
                descriptor.length.takeIf { it >= 0 } ?: descriptor.declaredLength.takeIf { it >= 0 }
            } ?: -1L
        }.getOrDefault(-1L)
    }

    private fun copyWithProgress(
        input: BufferedInputStream,
        output: FileOutputStream,
        totalBytes: Long,
        onProgress: (Int) -> Unit
    ) {
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var copiedBytes = 0L
        var lastProgress = -1
        onProgress(0)
        while (true) {
            val read = input.read(buffer)
            if (read <= 0) break
            output.write(buffer, 0, read)
            copiedBytes += read
            if (totalBytes > 0) {
                val progress = ((copiedBytes * 100) / totalBytes).toInt().coerceIn(0, 100)
                if (progress != lastProgress) {
                    lastProgress = progress
                    onProgress(progress)
                }
            }
        }
        output.flush()
    }

    private fun deleteSourceIfPossible(uri: Uri) {
        runCatching {
            if (DocumentsContract.isDocumentUri(context, uri)) {
                DocumentsContract.deleteDocument(context.contentResolver, uri)
            }
        }
    }
}

data class FolderVideoSource(
    val uri: Uri,
    val displayName: String
)

private fun JSONArray?.toStoredVideos(): List<StoredVideo> {
    if (this == null) return emptyList()
    return buildList {
        for (index in 0 until length()) {
            val item = optJSONObject(index) ?: continue
            add(
                StoredVideo(
                    id = item.optString("id"),
                    localPath = item.optString("local_path"),
                    sourceUri = item.optString("source_uri").takeIf { it.isNotBlank() },
                    sourceFolderUri = item.optString("source_folder_uri").takeIf { it.isNotBlank() },
                    displayName = item.optString("display_name"),
                    caption = item.optString("caption"),
                    comments = item.optJSONArray("comments")?.let { comments ->
                        buildList {
                            for (commentIndex in 0 until comments.length()) {
                                comments.optString(commentIndex).takeIf { it.isNotBlank() }?.let(::add)
                            }
                        }
                    } ?: emptyList()
                )
            )
        }
    }
}

private fun JSONArray?.toFolders(): List<SelectedFolder> {
    if (this == null) return emptyList()
    return buildList {
        for (index in 0 until length()) {
            val item = optJSONObject(index) ?: continue
            add(SelectedFolder(uri = item.optString("uri"), name = item.optString("name")))
        }
    }
}

private fun JSONArray?.toInboxLinks(): List<SharedLinkItem> {
    if (this == null) return emptyList()
    return buildList {
        for (index in 0 until length()) {
            val item = optJSONObject(index) ?: continue
            add(
                SharedLinkItem(
                    id = item.optString("id"),
                    url = item.optString("url"),
                    receivedAt = item.optLong("received_at")
                )
            )
        }
    }
}
