package com.example.tiktokui.data

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

data class AppPersistedState(
    val videos: List<StoredVideo> = emptyList(),
    val folders: List<SelectedFolder> = emptyList(),
    val inboxLinks: List<SharedLinkItem> = emptyList(),
    val playOrder: PlayOrder = PlayOrder.Sequential
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

class LocalTikTokStore(private val context: Context) {
    private val prefs = context.getSharedPreferences("local_tiktok_store", Context.MODE_PRIVATE)

    fun load(): AppPersistedState {
        val raw = prefs.getString("app_state", null) ?: return AppPersistedState()
        return runCatching {
            val json = JSONObject(raw)
            AppPersistedState(
                videos = json.optJSONArray("videos").toStoredVideos(),
                folders = json.optJSONArray("folders").toFolders(),
                inboxLinks = json.optJSONArray("inbox").toInboxLinks(),
                playOrder = PlayOrder.valueOf(json.optString("play_order", PlayOrder.Sequential.name))
            )
        }.getOrDefault(AppPersistedState())
    }

    fun save(state: AppPersistedState) {
        val json = JSONObject().apply {
            put("play_order", state.playOrder.name)
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
        val targetDir = File(context.filesDir, "LocalTikTok").apply { mkdirs() }
        val safeName = preferredName.ifBlank { "video_${System.currentTimeMillis()}.mp4" }
        val extension = safeName.substringAfterLast('.', "mp4")
        val baseName = safeName.substringBeforeLast('.')
        var target = File(targetDir, "$baseName.$extension")
        var counter = 1
        while (target.exists()) {
            target = File(targetDir, "${baseName}_$counter.$extension")
            counter++
        }
        context.contentResolver.openInputStream(source)?.use { input ->
            FileOutputStream(target).use { output -> input.copyTo(output) }
        }
        return target.absolutePath
    }

    fun takePersistablePermission(uri: Uri) {
        runCatching {
            context.contentResolver.takePersistableUriPermission(
                uri,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
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
