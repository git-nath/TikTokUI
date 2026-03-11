package com.example.tiktokui.data

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
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

data class FavoriteMoveResult(
    val video: StoredVideo,
    val alreadyFavorited: Boolean
)

class LocalTikTokStore(private val context: Context) {
    private val prefs = context.getSharedPreferences("local_tiktok_store", Context.MODE_PRIVATE)
    private val libraryDirName = "LocalTikTok"
    private val favoritesDirName = "LocalTikTok Favorite"
    private val favoritesRelativePath = "TikTokUi/Favorite Vidios"

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

    fun favoriteVideo(video: StoredVideo, onProgress: (Int) -> Unit = {}): FavoriteMoveResult {
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
        val currentContentUri = video.localPath
            .takeIf { it.startsWith("content://") }
            ?.let(Uri::parse)

        if (currentFile?.parentFile?.name == favoritesDirName || currentContentUri?.let(::isFavoriteUri) == true) {
            onProgress(100)
            return FavoriteMoveResult(video = video, alreadyFavorited = true)
        }

        findExistingFavorite(safeName, sourceSize)?.let { existingFavorite ->
            onProgress(100)
            return FavoriteMoveResult(
                video = video.copy(
                    localPath = existingFavorite.toString(),
                    sourceUri = existingFavorite.toString()
                ),
                alreadyFavorited = true
            )
        }

        when {
            currentFile != null && currentFile.exists() -> {
                val destinationUri = createFavoriteDestination(safeName)
                try {
                    openFavoriteOutput(destinationUri).use { output ->
                        currentFile.inputStream().use { input ->
                            copyWithProgress(
                                input = BufferedInputStream(input),
                                output = output,
                                totalBytes = currentFile.length(),
                                onProgress = onProgress
                            )
                        }
                    }
                    finalizeFavoriteDestination(destinationUri)
                } catch (error: Throwable) {
                    deleteFavoriteIfCreated(destinationUri)
                    throw error
                }
                if (!currentFile.delete()) {
                    deleteFavoriteIfCreated(destinationUri)
                    throw IOException("Unable to delete original file after favoriting")
                }
                onProgress(100)
                return FavoriteMoveResult(
                    video = video.copy(
                        localPath = destinationUri.toString(),
                        sourceUri = destinationUri.toString()
                    ),
                    alreadyFavorited = false
                )
            }
            sourceUri != null -> {
                val destinationUri = createFavoriteDestination(safeName)
                try {
                    context.contentResolver.openInputStream(sourceUri)?.use { input ->
                        openFavoriteOutput(destinationUri).use { output ->
                            copyWithProgress(
                                input = BufferedInputStream(input),
                                output = output,
                                totalBytes = resolveContentLength(sourceUri),
                                onProgress = onProgress
                            )
                        }
                    } ?: throw IOException("Unable to open source video for favoriting")
                    finalizeFavoriteDestination(destinationUri)
                } catch (error: Throwable) {
                    deleteFavoriteIfCreated(destinationUri)
                    throw error
                }
                if (!deleteSourceIfPossible(sourceUri)) {
                    deleteFavoriteIfCreated(destinationUri)
                    throw IOException("Unable to move source video into favorites")
                }
                onProgress(100)
                return FavoriteMoveResult(
                    video = video.copy(
                        localPath = destinationUri.toString(),
                        sourceUri = destinationUri.toString()
                    ),
                    alreadyFavorited = false
                )
            }
            else -> throw IOException("Video source is unavailable")
        }
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

    private fun findExistingFavorite(preferredName: String, sourceSize: Long): Uri? {
        val collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.SIZE
        )
        val selection = "${MediaStore.Video.Media.DISPLAY_NAME} = ? AND ${MediaStore.Video.Media.RELATIVE_PATH} = ?"
        val args = arrayOf(preferredName, normalizedRelativePath())
        context.contentResolver.query(collection, projection, selection, args, null)?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            while (cursor.moveToNext()) {
                val existingSize = cursor.getLong(sizeIndex)
                if (sourceSize <= 0L || existingSize == sourceSize) {
                    val id = cursor.getLong(idIndex)
                    return Uri.withAppendedPath(collection, id.toString())
                }
            }
        }
        return null
    }

    private fun createFavoriteDestination(displayName: String): Uri {
        val values = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Video.Media.MIME_TYPE, guessMimeType(displayName))
            put(MediaStore.Video.Media.RELATIVE_PATH, normalizedRelativePath())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Video.Media.IS_PENDING, 1)
            }
        }
        return context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
            ?: throw IOException("Unable to create favorite destination")
    }

    private fun openFavoriteOutput(destinationUri: Uri): java.io.OutputStream {
        return context.contentResolver.openOutputStream(destinationUri)
            ?: throw IOException("Unable to open favorite destination")
    }

    private fun deleteFavoriteIfCreated(destinationUri: Uri) {
        runCatching {
            context.contentResolver.delete(destinationUri, null, null)
        }
    }

    private fun finalizeFavoriteDestination(destinationUri: Uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.contentResolver.update(
                destinationUri,
                ContentValues().apply { put(MediaStore.Video.Media.IS_PENDING, 0) },
                null,
                null
            )
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
        output: java.io.OutputStream,
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

    private fun deleteSourceIfPossible(uri: Uri): Boolean {
        return runCatching {
            if (DocumentsContract.isDocumentUri(context, uri)) {
                DocumentsContract.deleteDocument(context.contentResolver, uri)
            } else {
                context.contentResolver.delete(uri, null, null) > 0
            }
        }.getOrDefault(false)
    }

    private fun isFavoriteUri(uri: Uri): Boolean {
        val projection = arrayOf(MediaStore.Video.Media.RELATIVE_PATH)
        context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val relativePath = cursor.getString(0).orEmpty()
                return relativePath.trimEnd('/', '\\') == favoritesRelativePath
            }
        }
        return false
    }

    private fun normalizedRelativePath(): String = "$favoritesRelativePath/"

    private fun guessMimeType(displayName: String): String = when (displayName.substringAfterLast('.', "").lowercase()) {
        "mp4" -> "video/mp4"
        "mkv" -> "video/x-matroska"
        "webm" -> "video/webm"
        "3gp" -> "video/3gpp"
        else -> "video/*"
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
