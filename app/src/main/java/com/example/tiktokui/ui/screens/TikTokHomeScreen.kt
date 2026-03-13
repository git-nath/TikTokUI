package com.example.tiktokui.ui.screens

import android.content.Intent
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.net.Uri
import android.provider.DocumentsContract
import android.widget.Toast
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ChatBubble
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.OpenInBrowser
import androidx.compose.material.icons.outlined.PersonAddAlt1
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.TurnedInNot
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.tiktokui.data.LocalTikTokStore
import com.example.tiktokui.data.PlayOrder
import com.example.tiktokui.data.SelectedFolder
import com.example.tiktokui.data.SharedLinkItem
import com.example.tiktokui.data.StoredVideo
import com.example.tiktokui.data.TodoItem
import com.example.tiktokui.data.VideoSourceMode
import com.example.tiktokui.ui.theme.TikTokAccent
import com.example.tiktokui.ui.theme.TikTokAccentSoft
import com.example.tiktokui.ui.theme.TikTokBackground
import com.example.tiktokui.ui.theme.TikTokCard
import com.example.tiktokui.ui.theme.TikTokCardMuted
import com.example.tiktokui.ui.theme.TikTokJournalGold
import com.example.tiktokui.ui.theme.TikTokJournalNavy
import com.example.tiktokui.ui.theme.TikTokOutline
import com.example.tiktokui.ui.theme.TikTokSurfaceVariant
import com.example.tiktokui.ui.theme.TikTokSuccess
import com.example.tiktokui.ui.theme.TikTokTextSecondary
import com.example.tiktokui.ui.theme.TikTokTodoAccent
import com.example.tiktokui.ui.theme.TikTokTodoAccentSoft
import com.example.tiktokui.ui.theme.TikTokTodoDone
import com.example.tiktokui.ui.theme.TikTokTodoUndone
import com.example.tiktokui.ui.theme.TikTokUITheme
import java.util.UUID
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch

@Composable
fun TikTokHomeScreen(
    modifier: Modifier = Modifier,
    incomingSharedText: String? = null,
    onSharedTextConsumed: () -> Unit = {}
) {
    val context = LocalContext.current
    val store = remember(context) { LocalTikTokStore(context) }
    val scope = rememberCoroutineScope()
    var appState by remember {
        mutableStateOf(
            store.load().let { loaded ->
                if (loaded.folders.isNotEmpty()) {
                    loaded.copy(videoSourceMode = VideoSourceMode.Folders)
                } else {
                    loaded
                }
            }
        )
    }
    val samplePosts = remember { sampleFeedPosts() }
    var selectedTab by rememberSaveable { mutableStateOf(BottomTab.Home) }
    var showComments by rememberSaveable { mutableStateOf(false) }
    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var caption by rememberSaveable { mutableStateOf("") }
    var pausedVideoId by rememberSaveable { mutableStateOf<String?>(null) }
    var editingPostId by rememberSaveable { mutableStateOf<String?>(null) }
    var editingCaptionText by rememberSaveable { mutableStateOf("") }
    var expandedCaptionPostId by rememberSaveable { mutableStateOf<String?>(null) }
    var profileSection by rememberSaveable { mutableStateOf(ProfileSection.Posts) }
    var inboxPreviewUrl by rememberSaveable { mutableStateOf<String?>(null) }
    var inboxInspectUrl by rememberSaveable { mutableStateOf<String?>(null) }
    var allModeVideos by remember { mutableStateOf<List<StoredVideo>>(emptyList()) }
    var favoriteMoveProgress by remember { mutableStateOf<FavoriteMoveProgress?>(null) }
    var pendingFavoritePostId by rememberSaveable { mutableStateOf<String?>(null) }
    var profileLaunchPostId by rememberSaveable { mutableStateOf<String?>(null) }

    val loadAllVideos: suspend () -> Unit = {
        val savedVideosBySource = appState.videos.associateBy { it.sourceUri ?: it.localPath }
        val scannedVideos = withContext(Dispatchers.IO) {
            store.scanAllDeviceVideos().map { source ->
                val savedVideo = savedVideosBySource[source.uri.toString()]
                StoredVideo(
                    id = savedVideo?.id ?: "all_${source.uri}",
                    localPath = source.uri.toString(),
                    sourceUri = source.uri.toString(),
                    sourceFolderUri = "__all_videos__",
                    displayName = source.displayName,
                    caption = savedVideo?.caption.orEmpty(),
                    comments = savedVideo?.comments ?: emptyList()
                )
            }
        }
        allModeVideos = scannedVideos
        appState = appState.copy(videoSourceMode = VideoSourceMode.All)
    }
    val mediaPermission = remember {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            android.Manifest.permission.READ_MEDIA_VIDEO
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }
    val videoPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        selectedVideoUri = uri
        if (uri != null) caption = ""
    }
    val allVideosPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            scope.launch {
                loadAllVideos()
            }
        } else {
            Toast.makeText(context, "Video access permission is required for All videos", Toast.LENGTH_SHORT).show()
        }
    }
    val folderPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            store.takePersistablePermission(uri)
            val folderName = store.resolveDisplayName(uri)
            val importedVideos = withContext(Dispatchers.IO) {
                store.scanFolderVideos(uri).mapNotNull { source ->
                    val alreadyImported = appState.videos.any { it.sourceUri == source.uri.toString() }
                    if (alreadyImported) {
                        null
                    } else {
                        StoredVideo(
                            id = UUID.randomUUID().toString(),
                            localPath = source.uri.toString(),
                            sourceUri = source.uri.toString(),
                            sourceFolderUri = uri.toString(),
                            displayName = source.displayName,
                            caption = ""
                        )
                    }
                }
            }
            appState = appState.copy(
                folders = (appState.folders + SelectedFolder(uri = uri.toString(), name = folderName)).distinctBy { it.uri },
                videos = (importedVideos + appState.videos).distinctBy { it.localPath },
                videoSourceMode = VideoSourceMode.Folders
            )
        }
    }

    LaunchedEffect(appState) {
        store.save(appState)
    }

    LaunchedEffect(incomingSharedText) {
        val sharedText = incomingSharedText?.trim().orEmpty()
        if (sharedText.startsWith("http://") || sharedText.startsWith("https://")) {
            appState = appState.copy(
                inboxLinks = listOf(
                    SharedLinkItem(
                        id = UUID.randomUUID().toString(),
                        url = sharedText,
                        receivedAt = System.currentTimeMillis()
                    )
                ) + appState.inboxLinks
            )
            selectedTab = BottomTab.Inbox
            onSharedTextConsumed()
        }
    }

    val visibleVideos = remember(appState.videos, appState.videoSourceMode, allModeVideos) {
        when (appState.videoSourceMode) {
            VideoSourceMode.All -> allModeVideos
            VideoSourceMode.Folders -> appState.videos.filter { it.sourceFolderUri != "__all_videos__" || appState.folders.isEmpty() }
        }
    }
    val favoriteVideos = remember(appState.videos) {
        appState.videos.filter { it.isFavoriteVideo() }
    }
    val regularProfileVideos = remember(appState.videos) {
        appState.videos.filterNot { it.isFavoriteVideo() }
    }
    val homeFeedPosts = remember(visibleVideos, appState.playOrder) {
        val importedPosts = visibleVideos.map { it.toVideoPostUiModel() }
        val orderedPosts = when (appState.playOrder) {
            PlayOrder.Sequential -> importedPosts
            PlayOrder.Shuffle -> importedPosts.shuffled()
        }
        orderedPosts + samplePosts
    }
    val pagerState = rememberPagerState(initialPage = 0) { homeFeedPosts.size }
    val currentFeedPostId = homeFeedPosts.getOrNull(pagerState.currentPage)?.id

    fun resolveStoredVideo(postId: String): StoredVideo? {
        return appState.videos.firstOrNull { it.id == postId }
            ?: visibleVideos.firstOrNull { it.id == postId }
            ?: allModeVideos.firstOrNull { it.id == postId }
    }

    fun persistCaptionNote(postId: String, note: String) {
        val sourceVideo = resolveStoredVideo(postId) ?: return
        val normalizedNote = note.trim()
        val existingIndex = appState.videos.indexOfFirst { it.id == postId }
        val updatedVideo = sourceVideo.copy(caption = normalizedNote)
        val updatedVideos = appState.videos.toMutableList()
        if (existingIndex >= 0) {
            updatedVideos[existingIndex] = updatedVideo
        } else {
            updatedVideos.add(0, updatedVideo)
        }
        appState = appState.copy(videos = updatedVideos.distinctBy { it.id })
        allModeVideos = allModeVideos.map { video ->
            if (video.id == postId) video.copy(caption = normalizedNote) else video
        }
    }

    fun openCaptionEditor(postId: String) {
        val sourceVideo = resolveStoredVideo(postId)
        editingPostId = postId
        editingCaptionText = sourceVideo?.normalizedCaption().orEmpty()
        expandedCaptionPostId = postId
    }

    fun closeCaptionEditor(save: Boolean) {
        val targetPostId = editingPostId ?: expandedCaptionPostId
        if (save && targetPostId != null) {
            persistCaptionNote(targetPostId, editingCaptionText)
        }
        editingPostId = null
        editingCaptionText = ""
        expandedCaptionPostId = null
    }

    fun moveVideoToFavorites(postId: String) {
        val existingIndex = appState.videos.indexOfFirst { it.id == postId }
        val sourceVideo = when {
            existingIndex >= 0 -> appState.videos[existingIndex]
            else -> resolveStoredVideo(postId)
        }

        if (sourceVideo == null) {
            Toast.makeText(context, "Couldn't find this video", Toast.LENGTH_SHORT).show()
            return
        }

        favoriteMoveProgress = FavoriteMoveProgress(
            videoName = sourceVideo.displayName,
            progress = 0
        )
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    store.favoriteVideo(sourceVideo) { progress ->
                        scope.launch {
                            favoriteMoveProgress = favoriteMoveProgress?.copy(progress = progress)
                        }
                    }
                }
            }.onSuccess { favoriteResult ->
                val updatedVideos = appState.videos.toMutableList()
                if (existingIndex >= 0) {
                    updatedVideos[existingIndex] = favoriteResult.video
                } else {
                    updatedVideos.add(0, favoriteResult.video)
                }
                appState = appState.copy(videos = updatedVideos.distinctBy { it.id })
                allModeVideos = allModeVideos.filterNot { it.id == postId }
                favoriteMoveProgress = favoriteMoveProgress?.copy(progress = 100)
                val message = if (favoriteResult.alreadyFavorited) {
                    "Already in favorites"
                } else {
                    "Moved to TikTokUi/Favorite Vidios"
                }
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }.onFailure {
                favoriteMoveProgress = null
                Toast.makeText(context, "Couldn't add this video to favorites", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(homeFeedPosts.size) {
        if (selectedTab == BottomTab.Home && appState.videos.isNotEmpty() && pagerState.currentPage != 0) {
            pagerState.animateScrollToPage(0)
        }
    }

    LaunchedEffect(pagerState.currentPage, selectedTab) {
        if (selectedTab == BottomTab.Home) {
            pausedVideoId = null
            showComments = false
            if (expandedCaptionPostId != null && expandedCaptionPostId != currentFeedPostId) {
                closeCaptionEditor(save = true)
            }
        } else if (expandedCaptionPostId != null) {
            closeCaptionEditor(save = true)
        }
    }

    LaunchedEffect(profileLaunchPostId, homeFeedPosts, selectedTab) {
        val targetId = profileLaunchPostId ?: return@LaunchedEffect
        if (selectedTab != BottomTab.Home) return@LaunchedEffect
        val targetIndex = homeFeedPosts.indexOfFirst { it.id == targetId }
        if (targetIndex >= 0) {
            pagerState.scrollToPage(targetIndex)
            pausedVideoId = null
            profileLaunchPostId = null
        }
    }

    LaunchedEffect(pendingFavoritePostId, currentFeedPostId, pausedVideoId, selectedTab) {
        val queuedId = pendingFavoritePostId ?: return@LaunchedEffect
        if (selectedTab != BottomTab.Home) return@LaunchedEffect
        val isStillActive = queuedId == currentFeedPostId && pausedVideoId == null
        if (!isStillActive) {
            pendingFavoritePostId = null
            moveVideoToFavorites(queuedId)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when (selectedTab) {
            BottomTab.Home -> HomeFeedPager(
                posts = homeFeedPosts,
                pagerState = pagerState,
                pausedVideoId = pausedVideoId,
                onTogglePlayback = { postId ->
                    pausedVideoId = if (pausedVideoId == postId) null else postId
                },
                onCommentsClick = { showComments = true },
                onShareClick = { postId ->
                    val sourceVideo = appState.videos.firstOrNull { it.id == postId }
                        ?: visibleVideos.firstOrNull { it.id == postId }
                    openVideoLocation(context, sourceVideo)
                },
                onCopyPath = { postId ->
                    val sourceVideo = resolveStoredVideo(postId)
                    if (sourceVideo != null) {
                        val pathText = sourceVideo.sourceUri ?: sourceVideo.localPath
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Video path", pathText))
                        Toast.makeText(context, "Path copied", Toast.LENGTH_SHORT).show()
                    }
                },
                onFavoriteClick = { postId ->
                    if (postId == currentFeedPostId && pausedVideoId == null) {
                        pendingFavoritePostId = postId
                        Toast.makeText(context, "Favorite queued. Scroll to move it.", Toast.LENGTH_SHORT).show()
                    } else {
                        moveVideoToFavorites(postId)
                    }
                },
                expandedCaptionPostId = expandedCaptionPostId,
                onToggleCaption = { postId ->
                    if (expandedCaptionPostId == postId) {
                        closeCaptionEditor(save = true)
                    } else {
                        openCaptionEditor(postId)
                    }
                },
                onCaptionChange = { postId, updatedCaption ->
                    if (editingPostId == postId) {
                        editingCaptionText = updatedCaption
                    }
                },
                activeCaptionDraft = if (editingPostId == expandedCaptionPostId) editingCaptionText else null,
                selectedTab = selectedTab,
                feedCounter = "${pagerState.currentPage + 1}/${homeFeedPosts.size}",
                onTabSelected = { tapped ->
                    selectedTab = when {
                        tapped == selectedTab -> BottomTab.Home
                        else -> tapped
                    }
                },
                onCreateClick = {
                    showComments = false
                    videoPicker.launch(arrayOf("video/*"))
                }
            )

            BottomTab.Profile -> TikTokProfileScreen(
                postedVideos = regularProfileVideos.map { it.toVideoPostUiModel() },
                favoriteVideos = favoriteVideos.map { it.toVideoPostUiModel() },
                selectedFolders = appState.folders,
                playOrder = appState.playOrder,
                videoSourceMode = appState.videoSourceMode,
                selectedSection = profileSection,
                selectedTab = selectedTab,
                onOpenVideo = { postId ->
                    selectedTab = BottomTab.Home
                    profileLaunchPostId = postId
                },
                onTabSelected = { tapped ->
                    selectedTab = if (tapped == selectedTab) BottomTab.Home else tapped
                },
                onCreateClick = { videoPicker.launch(arrayOf("video/*")) },
                onAddFolderClick = { folderPicker.launch(null) },
                onEnableAllVideosClick = {
                    if (ContextCompat.checkSelfPermission(context, mediaPermission) == PackageManager.PERMISSION_GRANTED) {
                        scope.launch {
                            loadAllVideos()
                        }
                    } else {
                        allVideosPermissionLauncher.launch(mediaPermission)
                    }
                },
                onUseFolderModeClick = {
                    appState = appState.copy(videoSourceMode = VideoSourceMode.Folders)
                },
                onSectionSelected = { profileSection = it },
                onTogglePlayOrder = {
                    appState = appState.copy(
                        playOrder = if (appState.playOrder == PlayOrder.Sequential) {
                            PlayOrder.Shuffle
                        } else {
                            PlayOrder.Sequential
                        }
                    )
                }
            )

            BottomTab.Todo -> TodoScreen(
                todos = appState.todos,
                selectedTab = selectedTab,
                onTabSelected = { tapped ->
                    selectedTab = if (tapped == selectedTab) BottomTab.Home else tapped
                },
                onCreateClick = { videoPicker.launch(arrayOf("video/*")) },
                onAddTodo = { text ->
                    appState = appState.copy(
                        todos = listOf(
                            TodoItem(
                                id = UUID.randomUUID().toString(),
                                text = text.trim(),
                                createdAt = System.currentTimeMillis()
                            )
                        ) + appState.todos
                    )
                },
                onToggleTodo = { todoId ->
                    appState = appState.copy(
                        todos = appState.todos.map { todo ->
                            if (todo.id == todoId) todo.copy(isDone = !todo.isDone) else todo
                        }
                    )
                },
                onDeleteTodo = { todoId ->
                    appState = appState.copy(
                        todos = appState.todos.filter { it.id != todoId }
                    )
                }
            )

            BottomTab.Inbox -> InboxScreen(
                links = appState.inboxLinks,
                selectedTab = selectedTab,
                onTabSelected = { tapped ->
                    selectedTab = if (tapped == selectedTab) BottomTab.Home else tapped
                },
                onCreateClick = { videoPicker.launch(arrayOf("video/*")) },
                onPreviewLink = { inboxPreviewUrl = it },
                onInspectLink = { inboxInspectUrl = it }
            )
        }

        if (showComments && selectedTab == BottomTab.Home) {
            TikTokCommentsScreen(
                onDismissRequest = { showComments = false },
                showStandaloneBackdrop = false
            )
        }

        if (selectedVideoUri != null) {
            UploadCaptionSheet(
                selectedVideoUri = selectedVideoUri,
                caption = caption,
                onCaptionChange = { caption = it },
                onDismiss = {
                    selectedVideoUri = null
                    caption = ""
                },
                onPost = {
                    val uri = selectedVideoUri ?: return@UploadCaptionSheet
                    val persistedUri = persistReadPermission(context, uri)
                    appState = appState.copy(
                        videos = listOf(
                            StoredVideo(
                                id = UUID.randomUUID().toString(),
                                localPath = persistedUri.toString(),
                                sourceUri = persistedUri.toString(),
                                sourceFolderUri = null,
                                displayName = store.resolveDisplayName(persistedUri),
                                caption = caption.trim()
                            )
                        ) + appState.videos
                    )
                    selectedVideoUri = null
                    caption = ""
                    selectedTab = BottomTab.Home
                    pausedVideoId = null
                    showComments = false
                }
            )
        }

        if (inboxPreviewUrl != null) {
            LinkPreviewSheet(
                url = inboxPreviewUrl!!,
                onDismiss = { inboxPreviewUrl = null }
            )
        }

        if (inboxInspectUrl != null) {
            LinkInspectSheet(
                url = inboxInspectUrl!!,
                onDismiss = { inboxInspectUrl = null }
            )
        }

        favoriteMoveProgress?.let { progress ->
            FavoriteMoveOverlay(
                state = progress,
                onDismiss = { favoriteMoveProgress = null }
            )
        }

    }
}

@Composable
private fun HomeFeedPager(
    posts: List<VideoPostUiModel>,
    pagerState: PagerState,
    pausedVideoId: String?,
    onTogglePlayback: (String) -> Unit,
    onCommentsClick: () -> Unit,
    onShareClick: (String) -> Unit,
    onCopyPath: (String) -> Unit,
    onFavoriteClick: (String) -> Unit,
    expandedCaptionPostId: String?,
    onToggleCaption: (String) -> Unit,
    onCaptionChange: (String, String) -> Unit,
    activeCaptionDraft: String?,
    feedCounter: String,
    selectedTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit,
    onCreateClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        VerticalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            val post = posts[page]
            HomeFeedPage(
                post = post,
                isActive = pagerState.currentPage == page,
                isPaused = pausedVideoId == post.id,
                onTogglePlayback = { onTogglePlayback(post.id) },
                onCommentsClick = onCommentsClick,
                onShareClick = { onShareClick(post.id) },
                onCopyPath = { onCopyPath(post.id) },
                onFavoriteClick = { onFavoriteClick(post.id) },
                isCaptionExpanded = expandedCaptionPostId == post.id,
                onToggleCaption = { onToggleCaption(post.id) },
                onCaptionChange = { updatedCaption -> onCaptionChange(post.id, updatedCaption) },
                captionDraft = if (expandedCaptionPostId == post.id) activeCaptionDraft else null
            )
        }

        BottomNavBar(
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding(),
            selectedTab = selectedTab,
            onTabSelected = onTabSelected,
            onCreateClick = onCreateClick,
            darkTheme = true
        )

        TopTabs(
            modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding(),
            feedCounter = feedCounter
        )
    }
}

@Composable
private fun HomeFeedPage(
    post: VideoPostUiModel,
    isActive: Boolean,
    isPaused: Boolean,
    onTogglePlayback: () -> Unit,
    onCommentsClick: () -> Unit,
    onShareClick: () -> Unit,
    onCopyPath: () -> Unit,
    onFavoriteClick: () -> Unit,
    isCaptionExpanded: Boolean,
    onToggleCaption: () -> Unit,
    onCaptionChange: (String) -> Unit,
    captionDraft: String?
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (post.uri != null) {
            VideoBackground(
                uri = post.uri,
                shouldPlay = isActive && !isPaused,
                onTogglePlayback = onTogglePlayback
            )
            if (isPaused) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Surface(color = Color.Black.copy(alpha = 0.38f), shape = CircleShape) {
                        Icon(
                            imageVector = Icons.Outlined.PlayArrow,
                            contentDescription = "Play video",
                            tint = Color.White,
                            modifier = Modifier.padding(14.dp).size(34.dp)
                        )
                    }
                }
            }
        } else {
            HomeFeedBackground()
        }

        // Folder name chip at top
        if (post.isEditable && post.song.isNotBlank()) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(start = 16.dp, top = 56.dp),
                color = Color.Black.copy(alpha = 0.48f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.10f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FolderOpen,
                        contentDescription = "Source folder",
                        tint = TikTokJournalGold,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = post.song,
                        color = Color.White.copy(alpha = 0.92f),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 180.dp)
                    )
                }
            }
        }

        FeedOverlay(
            post = post,
            showTabs = post.uri == null,
            onCommentsClick = onCommentsClick,
            onShareClick = onShareClick,
            onCopyPath = onCopyPath,
            onFavoriteClick = onFavoriteClick,
            isCaptionExpanded = isCaptionExpanded,
            onToggleCaption = onToggleCaption,
            onCaptionChange = onCaptionChange,
            captionDraft = captionDraft
        )
    }
}

@Composable
private fun VideoBackground(uri: Uri, shouldPlay: Boolean, onTogglePlayback: () -> Unit) {
    AndroidView(
        modifier = Modifier.fillMaxSize().clickable(onClick = onTogglePlayback),
        factory = { ctx ->
            VideoView(ctx).apply {
                setVideoURI(uri)
                setOnErrorListener { _, _, _ -> true }
                setOnPreparedListener { mediaPlayer ->
                    mediaPlayer.isLooping = true
                    if (shouldPlay) start()
                }
            }
        },
        update = { view ->
            if (shouldPlay) {
                if (!view.isPlaying) {
                    runCatching { view.start() }
                }
            } else {
                if (view.isPlaying) view.pause()
            }
        }
    )
}

@Composable
private fun FeedOverlay(
    post: VideoPostUiModel,
    showTabs: Boolean,
    onCommentsClick: () -> Unit,
    onShareClick: () -> Unit,
    onCopyPath: () -> Unit,
    onFavoriteClick: () -> Unit,
    isCaptionExpanded: Boolean,
    onToggleCaption: () -> Unit,
    onCaptionChange: (String) -> Unit,
    captionDraft: String?
) {
    Box(modifier = Modifier.fillMaxSize()) {
        BottomMetaBlock(
            modifier = Modifier.align(Alignment.BottomStart).navigationBarsPadding(),
            post = post,
            isCaptionExpanded = isCaptionExpanded,
            onToggleCaption = onToggleCaption
        )

        if (isCaptionExpanded) {
            CenteredCaptionOverlay(
                modifier = Modifier.align(Alignment.BottomStart),
                post = post,
                onDismiss = { updatedCaption ->
                    onCaptionChange(updatedCaption)
                    onToggleCaption()
                },
                draftCaption = captionDraft ?: post.caption,
                onCaptionDraftChange = onCaptionChange
            )
        }

        RightActionRail(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(bottom = 92.dp, end = 12.dp),
            post = post,
            onCommentsClick = onCommentsClick,
            onShareClick = onShareClick,
            onCopyPath = onCopyPath,
            onFavoriteClick = onFavoriteClick
        )
    }
}

@Composable
private fun HomeFeedBackground() {
    Box(
        modifier = Modifier.fillMaxSize().background(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF07151C), Color(0xFF102730), Color(0xFF0B0E14))
            )
        )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF6BE7FF).copy(alpha = 0.55f), Color.Transparent),
                    center = Offset(size.width * 0.48f, size.height * 0.52f),
                    radius = size.minDimension * 0.42f
                ),
                radius = size.minDimension * 0.42f,
                center = Offset(size.width * 0.48f, size.height * 0.52f)
            )
            drawRoundRect(
                color = Color(0xFFE44362).copy(alpha = 0.78f),
                topLeft = Offset(size.width * 0.64f, size.height * 0.05f),
                size = Size(size.width * 0.045f, size.height * 0.22f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(28f, 28f)
            )
            drawRoundRect(
                color = Color(0xFFE44362).copy(alpha = 0.78f),
                topLeft = Offset(size.width * 0.47f, size.height * 0.05f),
                size = Size(size.width * 0.045f, size.height * 0.22f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(28f, 28f)
            )
            drawArc(
                color = Color(0xFFE44362).copy(alpha = 0.58f),
                startAngle = 65f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(size.width * 0.48f, size.height * 0.40f),
                size = Size(size.width * 0.42f, size.height * 0.34f),
                style = Stroke(width = 18f, cap = StrokeCap.Round)
            )
            drawArc(
                color = Color(0xFFE44362).copy(alpha = 0.45f),
                startAngle = 80f,
                sweepAngle = 150f,
                useCenter = false,
                topLeft = Offset(size.width * 0.22f, size.height * 0.63f),
                size = Size(size.width * 0.42f, size.height * 0.22f),
                style = Stroke(width = 16f, cap = StrokeCap.Round)
            )
        }

        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.32f),
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.58f)
                    )
                )
            )
        )
    }
}

@Composable
private fun TopTabs(modifier: Modifier = Modifier, feedCounter: String) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 10.dp, start = 16.dp, end = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = Color.Black.copy(alpha = 0.22f),
            shape = RoundedCornerShape(999.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Following",
                    color = Color.White.copy(alpha = 0.66f),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
                Surface(
                    color = Color.White.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        text = "For You",
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
        Surface(
            color = Color.Black.copy(alpha = 0.24f),
            shape = RoundedCornerShape(999.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(TikTokAccent)
                )
                Text(
                    text = feedCounter,
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}

@Composable
private fun BottomMetaBlock(
    modifier: Modifier = Modifier,
    post: VideoPostUiModel,
    isCaptionExpanded: Boolean,
    onToggleCaption: () -> Unit
) {
    Column(modifier = modifier.fillMaxWidth(0.78f).padding(start = 16.dp, bottom = 76.dp)) {
        Surface(
            color = if (isCaptionExpanded) Color.White.copy(alpha = 0.16f) else Color.Black.copy(alpha = 0.30f),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.clickable(onClick = onToggleCaption)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp)) {
                Text(
                    text = if (isCaptionExpanded) "Hide caption" else "See caption",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 15.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                if (!isCaptionExpanded) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = post.caption,
                        color = Color.White.copy(alpha = 0.86f),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

    }
}

@Composable
private fun CenteredCaptionOverlay(
    modifier: Modifier = Modifier,
    post: VideoPostUiModel,
    onDismiss: (String) -> Unit,
    draftCaption: String,
    onCaptionDraftChange: (String) -> Unit
) {
    val scrollState = rememberScrollState()
    val panelAlpha by animateFloatAsState(if (post.isEditable) 0.96f else 0.92f, label = "captionPanelAlpha")
    val panelInteractionSource = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(onClick = { onDismiss(draftCaption.trim()) })
        )
        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + slideInHorizontally(initialOffsetX = { -it / 3 }),
            exit = fadeOut() + slideOutHorizontally(targetOffsetX = { -it / 4 }),
            modifier = modifier
                .align(Alignment.BottomStart)
                .padding(start = 0.dp, bottom = 120.dp)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.70f)
                    .requiredHeightIn(min = 210.dp, max = 560.dp)
                    .clickable(
                        interactionSource = panelInteractionSource,
                        indication = null,
                        onClick = {}
                    ),
                color = Color(0xFF111111).copy(alpha = panelAlpha),
                shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 14.dp, end = 16.dp, top = 14.dp, bottom = 14.dp)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (post.isEditable) {
                        Text(
                            text = "Edit caption",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        OutlinedTextField(
                            value = draftCaption,
                            onValueChange = onCaptionDraftChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Transparent),
                            minLines = 8,
                            maxLines = 20,
                            placeholder = { Text("Write caption", color = Color.White.copy(alpha = 0.55f)) },
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = Color.White,
                                fontSize = 16.sp,
                                lineHeight = 27.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                disabledBorderColor = Color.Transparent,
                                errorBorderColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = TikTokAccent
                            )
                        )
                    } else {
                        Text(
                            text = post.caption,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 16.sp,
                                lineHeight = 27.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RightActionRail(
    modifier: Modifier = Modifier,
    post: VideoPostUiModel,
    onCommentsClick: () -> Unit,
    onShareClick: () -> Unit,
    onCopyPath: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Box(contentAlignment = Alignment.BottomCenter) {
            AvatarDisc(brush = post.avatarBrush)
            Box(
                modifier = Modifier.padding(top = 48.dp).size(22.dp).clip(CircleShape).background(TikTokAccent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Follow creator",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        SocialAction(
            icon = {
                Icon(
                    imageVector = Icons.Rounded.Favorite,
                    contentDescription = "Like video",
                    tint = Color.White,
                    modifier = Modifier.size(34.dp)
                )
            },
            count = post.likes
        )
        SocialAction(
            icon = {
                Icon(
                    imageVector = Icons.Outlined.ChatBubble,
                    contentDescription = "Open comments",
                    tint = Color.White,
                    modifier = Modifier.size(31.dp)
                )
            },
            count = post.comments,
            onClick = onCommentsClick
        )
        SocialAction(
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Share,
                    contentDescription = "Share video",
                    tint = Color.White,
                    modifier = Modifier.size(31.dp)
                )
            },
            count = post.shares,
            onClick = onShareClick
        )

        if (post.isEditable) {
            SocialAction(
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.ContentCopy,
                        contentDescription = "Copy path",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                },
                count = "Copy",
                onClick = onCopyPath
            )
        }

        FavoriteDisc(
            enabled = post.isEditable,
            onClick = onFavoriteClick
        )
    }
}

@Composable
private fun SocialAction(icon: @Composable () -> Unit, count: String, onClick: (() -> Unit)? = null) {
    Column(
        modifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        icon()
        Text(
            text = count,
            color = Color.White,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
        )
    }
}

@Composable
private fun AvatarDisc(brush: Brush) {
    Box(
        modifier = Modifier.size(52.dp).clip(CircleShape).background(brush),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.size(46.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.18f))
        )
    }
}

@Composable
private fun FavoriteDisc(enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF4A4A4A), Color(0xFF1A1A1A), Color.Black)
                )
            )
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(width = 20.dp, height = 22.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFF5C542)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Bookmark,
                contentDescription = "Add to favorites",
                tint = Color.Black,
                modifier = Modifier.size(15.dp)
            )
        }
    }
}

@Composable
private fun FavoriteMoveOverlay(state: FavoriteMoveProgress, onDismiss: () -> Unit) {
    LaunchedEffect(state.progress) {
        if (state.progress >= 100) {
            delay(550)
            onDismiss()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.46f)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = Color(0xFF111111),
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
        ) {
            Column(
                modifier = Modifier
                    .width(280.dp)
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    progress = (state.progress / 100f).coerceIn(0f, 1f),
                    color = TikTokAccent,
                    trackColor = Color.White.copy(alpha = 0.12f)
                )
                Text(
                    text = if (state.progress >= 100) "Saved to favorites" else "Moving to favorites",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = state.videoName,
                    color = Color.White.copy(alpha = 0.72f),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
                LinearProgressIndicator(
                    progress = (state.progress / 100f).coerceIn(0f, 1f),
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFF5C542),
                    trackColor = Color.White.copy(alpha = 0.12f)
                )
                Text(
                    text = "${state.progress}%",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
private fun BottomNavBar(
    modifier: Modifier = Modifier,
    selectedTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit,
    onCreateClick: () -> Unit,
    darkTheme: Boolean
) {
    val surfaceColor = if (darkTheme) Color.Black.copy(alpha = 0.92f) else Color.White.copy(alpha = 0.98f)
    val foreground = if (darkTheme) Color.White else Color.Black
    val secondary = if (darkTheme) Color.White.copy(alpha = 0.64f) else TikTokTextSecondary

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = surfaceColor,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (darkTheme) Color.White.copy(alpha = 0.06f) else TikTokOutline.copy(alpha = 0.82f)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(
                icon = Icons.Outlined.Home,
                label = "Home",
                selected = selectedTab == BottomTab.Home,
                activeColor = foreground,
                inactiveColor = secondary,
                darkTheme = darkTheme,
                onClick = { onTabSelected(BottomTab.Home) }
            )
            NavItem(
                icon = Icons.Outlined.Edit,
                label = "Todo",
                selected = selectedTab == BottomTab.Todo,
                activeColor = foreground,
                inactiveColor = secondary,
                darkTheme = darkTheme,
                onClick = { onTabSelected(BottomTab.Todo) }
            )
            CreateButton(onClick = onCreateClick)
            NavItem(
                icon = Icons.Outlined.ChatBubble,
                label = "Inbox",
                selected = selectedTab == BottomTab.Inbox,
                activeColor = foreground,
                inactiveColor = secondary,
                darkTheme = darkTheme,
                onClick = { onTabSelected(BottomTab.Inbox) }
            )
            NavItem(
                icon = Icons.Outlined.PersonOutline,
                label = "Me",
                selected = selectedTab == BottomTab.Profile,
                activeColor = foreground,
                inactiveColor = secondary,
                darkTheme = darkTheme,
                onClick = { onTabSelected(BottomTab.Profile) }
            )
        }
    }
}

@Composable
private fun NavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean = false,
    activeColor: Color,
    inactiveColor: Color,
    darkTheme: Boolean,
    onClick: (() -> Unit)? = null
) {
    Column(
        modifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Surface(
            color = when {
                selected && darkTheme -> Color.White.copy(alpha = 0.10f)
                selected -> TikTokAccentSoft
                else -> Color.Transparent
            },
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) activeColor else inactiveColor,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp).size(20.dp)
            )
        }
        Text(
            text = label,
            color = if (selected) activeColor else inactiveColor,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium)
        )
    }
}

@Composable
private fun TikTokProfileScreen(
    postedVideos: List<VideoPostUiModel>,
    favoriteVideos: List<VideoPostUiModel>,
    selectedFolders: List<SelectedFolder>,
    playOrder: PlayOrder,
    videoSourceMode: VideoSourceMode,
    selectedSection: ProfileSection,
    selectedTab: BottomTab,
    onOpenVideo: (String) -> Unit,
    onTabSelected: (BottomTab) -> Unit,
    onCreateClick: () -> Unit,
    onAddFolderClick: () -> Unit,
    onEnableAllVideosClick: () -> Unit,
    onUseFolderModeClick: () -> Unit,
    onSectionSelected: (ProfileSection) -> Unit,
    onTogglePlayOrder: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 72.dp)
        ) {
            ProfileTopBar()
            HorizontalDivider(color = TikTokOutline)
            ProfileHeader(totalPosts = postedVideos.size + favoriteVideos.size)
            ProfileTabRow(
                selectedSection = selectedSection,
                onSectionSelected = onSectionSelected
            )
            when (selectedSection) {
                ProfileSection.Posts -> ProfileGridInline(
                    postedVideos = postedVideos,
                    onOpenVideo = onOpenVideo
                )
                ProfileSection.Favorites -> ProfileGridInline(
                    postedVideos = favoriteVideos,
                    emptyTitle = "No favorite videos yet",
                    emptySubtitle = "Tap the favorite icon on any video to move it into your favorites folder.",
                    onOpenVideo = onOpenVideo
                )
                ProfileSection.Folders -> FolderManagerCard(
                    modifier = Modifier,
                    folders = selectedFolders,
                    playOrder = playOrder,
                    videoSourceMode = videoSourceMode,
                    onAddFolderClick = onAddFolderClick,
                    onEnableAllVideosClick = onEnableAllVideosClick,
                    onUseFolderModeClick = onUseFolderModeClick,
                    onTogglePlayOrder = onTogglePlayOrder
                )
            }
        }

        BottomNavBar(
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding(),
            selectedTab = selectedTab,
            onTabSelected = onTabSelected,
            onCreateClick = onCreateClick,
            darkTheme = false
        )
    }
}

@Composable
private fun FolderManagerCard(
    modifier: Modifier = Modifier,
    folders: List<SelectedFolder>,
    playOrder: PlayOrder,
    videoSourceMode: VideoSourceMode,
    onAddFolderClick: () -> Unit,
    onEnableAllVideosClick: () -> Unit,
    onUseFolderModeClick: () -> Unit,
    onTogglePlayOrder: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            color = TikTokCard,
            shape = RoundedCornerShape(28.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, TikTokOutline.copy(alpha = 0.55f)),
            shadowElevation = 3.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(color = TikTokAccentSoft, shape = RoundedCornerShape(14.dp)) {
                        Icon(
                            imageVector = Icons.Rounded.Folder,
                            contentDescription = null,
                            tint = TikTokAccent,
                            modifier = Modifier.padding(10.dp).size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Folder library",
                            color = Color.Black,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${folders.size} selected folders · ${if (videoSourceMode == VideoSourceMode.Folders) "Playing folder feed" else "All device videos active"}",
                            color = TikTokTextSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                Surface(
                    color = TikTokCardMuted,
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Journal mode",
                            color = Color.Black,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "Folders first",
                            color = TikTokAccent,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
                Text(
                    text = "Selected folders",
                    color = Color.Black,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                if (folders.isEmpty()) {
                    Text(
                        text = "No folders added yet. Add a folder and the app will play videos directly from it.",
                        color = TikTokTextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        folders.forEach { folder ->
                            Surface(
                                color = TikTokCardMuted,
                                shape = RoundedCornerShape(18.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, TikTokOutline.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = folder.name,
                                            color = Color.Black,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                                        )
                                        Text(
                                            text = "Playing from selected folder",
                                            color = TikTokTextSecondary,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Outlined.Lock,
                                        contentDescription = null,
                                        tint = TikTokTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(
                modifier = Modifier.weight(1f).clickable(onClick = onUseFolderModeClick),
                color = if (videoSourceMode == VideoSourceMode.Folders) TikTokJournalNavy else TikTokCard,
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (videoSourceMode == VideoSourceMode.Folders) Color.Transparent else TikTokOutline.copy(alpha = 0.6f)
                )
            ) {
                Box(modifier = Modifier.padding(vertical = 15.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Folders",
                        color = if (videoSourceMode == VideoSourceMode.Folders) Color.White else Color.Black,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
            Surface(
                modifier = Modifier.weight(1f).clickable(onClick = onEnableAllVideosClick),
                color = if (videoSourceMode == VideoSourceMode.All) TikTokJournalNavy else TikTokCard,
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (videoSourceMode == VideoSourceMode.All) Color.Transparent else TikTokOutline.copy(alpha = 0.6f)
                )
            ) {
                Box(modifier = Modifier.padding(vertical = 15.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = "All videos",
                        color = if (videoSourceMode == VideoSourceMode.All) Color.White else Color.Black,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(
                modifier = Modifier.weight(1f).clickable(onClick = onAddFolderClick),
                color = TikTokCard,
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, TikTokOutline.copy(alpha = 0.6f))
            ) {
                Box(modifier = Modifier.padding(vertical = 15.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Add folder",
                        color = Color.Black,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
            Surface(
                modifier = Modifier.weight(1f).clickable(onClick = onTogglePlayOrder),
                color = Color.Black,
                shape = RoundedCornerShape(20.dp)
            ) {
                Box(modifier = Modifier.padding(vertical = 15.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (playOrder == PlayOrder.Sequential) "Sequential" else "Shuffle",
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        }
    }
}

@Composable
private fun InboxScreen(
    links: List<SharedLinkItem>,
    selectedTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit,
    onCreateClick: () -> Unit,
    onPreviewLink: (String) -> Unit,
    onInspectLink: (String) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().background(TikTokBackground)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                color = TikTokCard,
                shape = RoundedCornerShape(28.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, TikTokOutline.copy(alpha = 0.7f)),
                shadowElevation = 3.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Link inbox",
                        color = Color.Black,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Keep TikTok, YouTube, and Instagram links here before you decide what belongs in your video journal.",
                        color = TikTokTextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (links.isEmpty()) {
                    Surface(
                        color = TikTokCard,
                        shape = RoundedCornerShape(24.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TikTokOutline.copy(alpha = 0.6f))
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Nothing saved yet",
                                color = Color.Black,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Use Android Share on a link and choose this app to drop it here.",
                                color = TikTokTextSecondary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else {
                    links.forEach { link ->
                        val cleanedUrl = remember(link.url) { cleanSharedUrl(link.url) }
                        val title = remember(cleanedUrl) { linkDisplayTitle(cleanedUrl) }
                        Surface(
                            color = TikTokCard,
                            shape = RoundedCornerShape(24.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, TikTokOutline.copy(alpha = 0.6f)),
                            shadowElevation = 2.dp
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = title,
                                    color = Color.Black,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = cleanedUrl,
                                    color = TikTokTextSecondary,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Surface(
                                        modifier = Modifier.weight(1f).clickable { onPreviewLink(cleanedUrl) },
                                        color = Color(0xFF111111),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Box(modifier = Modifier.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "Preview",
                                                color = Color.White,
                                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                                            )
                                        }
                                    }
                                    Surface(
                                        modifier = Modifier.weight(1f).clickable {
                                            onInspectLink(cleanedUrl)
                                        },
                                        color = Color.White,
                                        shape = RoundedCornerShape(14.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, TikTokOutline.copy(alpha = 0.6f))
                                    ) {
                                        Box(modifier = Modifier.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "Inspect",
                                                color = Color.Black,
                                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        BottomNavBar(
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding(),
            selectedTab = selectedTab,
            onTabSelected = onTabSelected,
            onCreateClick = onCreateClick,
            darkTheme = false
        )
    }
}

@Composable
private fun LinkPreviewSheet(url: String, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.52f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxSize(0.88f)
                .clickable(enabled = false) {},
            color = Color.White,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = linkDisplayTitle(url),
                            color = Color.Black,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = url,
                            color = TikTokTextSecondary,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Surface(
                        color = Color(0xFF111111),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Close",
                            color = Color.White,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.clickable(onClick = onDismiss).padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->
                        WebView(context).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            webViewClient = WebViewClient()
                            loadUrl(url)
                        }
                    },
                    update = { it.loadUrl(url) }
                )
            }
        }
    }
}

@Composable
private fun LinkInspectSheet(url: String, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val uri = remember(url) { Uri.parse(url) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.52f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = false) {}
                .navigationBarsPadding(),
            color = Color.White,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Link details",
                    color = Color.Black,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                InspectDetailRow(label = "Type", value = linkDisplayTitle(url))
                InspectDetailRow(label = "Host", value = uri.host ?: "Unknown")
                InspectDetailRow(label = "Path", value = uri.path ?: "/")
                InspectDetailRow(label = "Clean URL", value = url)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        modifier = Modifier.weight(1f).clickable {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Shared URL", url))
                            Toast.makeText(context, "Link copied", Toast.LENGTH_SHORT).show()
                        },
                        color = Color.White,
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TikTokOutline.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ContentCopy,
                                contentDescription = "Copy link",
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Copy",
                                color = Color.Black,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                    Surface(
                        modifier = Modifier.weight(1f).clickable {
                            runCatching {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                )
                            }
                        },
                        color = Color(0xFF111111),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.OpenInBrowser,
                                contentDescription = "Open externally",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Open",
                                color = Color.White,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InspectDetailRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            color = TikTokTextSecondary,
            style = MaterialTheme.typography.labelMedium
        )
        Text(
            text = value,
            color = Color.Black,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun ProfileTopBar() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(color = TikTokCardMuted, shape = RoundedCornerShape(14.dp)) {
            Icon(
                imageVector = Icons.Outlined.PersonAddAlt1,
                contentDescription = "Add friends",
                tint = Color.Black,
                modifier = Modifier.padding(10.dp).size(20.dp)
            )
        }
        Surface(
            color = TikTokCardMuted,
            shape = RoundedCornerShape(999.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Jacob West",
                    color = Color.Black,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(text = " \u25BE", color = TikTokTextSecondary, style = MaterialTheme.typography.labelMedium)
            }
        }
        Surface(color = TikTokCardMuted, shape = RoundedCornerShape(14.dp)) {
            Icon(
                imageVector = Icons.Outlined.MoreHoriz,
                contentDescription = "More",
                tint = Color.Black,
                modifier = Modifier.padding(10.dp).size(20.dp)
            )
        }
    }
}

@Composable
private fun ProfileHeader(totalPosts: Int) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        color = TikTokCard,
        shape = RoundedCornerShape(28.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, TikTokOutline.copy(alpha = 0.8f)),
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0xFFFFC78E), Color(0xFFE07B52), Color(0xFF8F4E2F))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF9B7A58), Color(0xFF5F4835))
                            )
                        )
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "@jacob_w",
                color = Color.Black,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.CenterVertically) {
                ProfileStat(value = totalPosts.toString(), label = "Following")
                ProfileStat(value = totalPosts.toString(), label = "Followers")
                ProfileStat(value = totalPosts.toString(), label = "Posts")
            }
            Spacer(modifier = Modifier.height(18.dp))
            Surface(
                color = TikTokCardMuted,
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, TikTokOutline.copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Bookmark,
                        contentDescription = "Favorite folder",
                        tint = TikTokJournalGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Favorite folder",
                        color = Color.Black,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = "TikTokUi/Favorite Vidios",
                        color = TikTokTextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = Color.Black,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(text = label, color = TikTokTextSecondary, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun ProfileTabRow(
    selectedSection: ProfileSection,
    onSectionSelected: (ProfileSection) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        color = TikTokCardMuted,
        shape = RoundedCornerShape(22.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, TikTokOutline.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ProfileSectionChip(
                modifier = Modifier.weight(1f),
                label = "Posts",
                icon = Icons.Outlined.GridOn,
                selected = selectedSection == ProfileSection.Posts,
                onClick = { onSectionSelected(ProfileSection.Posts) }
            )
            ProfileSectionChip(
                modifier = Modifier.weight(1f),
                label = "Favorites",
                icon = Icons.Filled.Bookmark,
                selected = selectedSection == ProfileSection.Favorites,
                onClick = { onSectionSelected(ProfileSection.Favorites) }
            )
            ProfileSectionChip(
                modifier = Modifier.weight(1f),
                label = "Folders",
                icon = Icons.Rounded.Folder,
                selected = selectedSection == ProfileSection.Folders,
                onClick = { onSectionSelected(ProfileSection.Folders) }
            )
        }
    }
}

@Composable
private fun ProfileSectionChip(
    modifier: Modifier = Modifier,
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        color = if (selected) TikTokCard else Color.Transparent,
        shape = RoundedCornerShape(16.dp),
        border = if (selected) androidx.compose.foundation.BorderStroke(1.dp, TikTokOutline.copy(alpha = 0.7f)) else null,
        shadowElevation = if (selected) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) TikTokJournalNavy else TikTokTextSecondary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                color = if (selected) TikTokJournalNavy else TikTokTextSecondary,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
            )
        }
    }
}

@Composable
private fun ProfileGrid(
    modifier: Modifier = Modifier,
    postedVideos: List<VideoPostUiModel>,
    emptyTitle: String = "No posts yet",
    emptySubtitle: String = "Add a video to build your clean profile grid.",
    onOpenVideo: (String) -> Unit
) {
    if (postedVideos.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            EmptyProfileGridCard(title = emptyTitle, subtitle = emptySubtitle)
        }
        return
    }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 68.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(postedVideos) { post ->
                ProfileVideoTile(post = post, onOpenVideo = onOpenVideo)
            }
        }
    }
}

@Composable
private fun ProfileGridInline(
    postedVideos: List<VideoPostUiModel>,
    emptyTitle: String = "No posts yet",
    emptySubtitle: String = "Add a video to build your clean profile grid.",
    onOpenVideo: (String) -> Unit
) {
    if (postedVideos.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            EmptyProfileGridCard(title = emptyTitle, subtitle = emptySubtitle)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        postedVideos.chunked(3).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                rowItems.forEach { post ->
                    Box(modifier = Modifier.weight(1f)) {
                        ProfileVideoTile(post = post, onOpenVideo = onOpenVideo)
                    }
                }
                repeat(3 - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ProfileVideoTile(post: VideoPostUiModel, onOpenVideo: (String) -> Unit) {
    val thumbnail = rememberVideoThumbnail(post.localPath)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.78f)
            .clip(RoundedCornerShape(4.dp))
            .background(Color.Black)
            .clickable { onOpenVideo(post.id) },
        contentAlignment = Alignment.BottomStart
    ) {
        if (thumbnail != null) {
            Image(
                bitmap = thumbnail,
                contentDescription = post.caption,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize().background(
                    Brush.verticalGradient(listOf(Color(0xFF112334), Color(0xFF080D13)))
                )
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.18f),
                            Color.Black.copy(alpha = 0.52f)
                        )
                    )
                )
        )
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.PlayArrow,
                contentDescription = "Views",
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = post.likes,
                color = Color.White,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
            )
        }
    }
}

@Composable
private fun EmptyProfileGridCard(title: String, subtitle: String) {
    Surface(
        color = Color(0xFFF6F7F9),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, TikTokOutline.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                color = Color.Black,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = subtitle,
                color = TikTokTextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun CreateButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier.width(44.dp).height(28.dp).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.padding(end = 7.dp).size(width = 44.dp, height = 28.dp)
                .clip(RoundedCornerShape(9.dp)).background(Color(0xFF27F4F2))
        )
        Box(
            modifier = Modifier.padding(start = 7.dp).size(width = 44.dp, height = 28.dp)
                .clip(RoundedCornerShape(9.dp)).background(Color(0xFFFF315A))
        )
        Box(
            modifier = Modifier.size(width = 38.dp, height = 28.dp)
                .clip(RoundedCornerShape(9.dp)).background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "Create",
                tint = Color.Black,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun UploadCaptionSheet(
    selectedVideoUri: Uri?,
    caption: String,
    onCaptionChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onPost: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.55f)).clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().clickable(enabled = false) {}.navigationBarsPadding(),
            color = TikTokCard,
            shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
            tonalElevation = 0.dp,
            shadowElevation = 14.dp
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    color = TikTokCardMuted,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Add journal note",
                            color = Color.Black,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "This note stays attached to the video like a private diary entry.",
                            color = TikTokTextSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = selectedVideoUri?.lastPathSegment ?: "Selected video",
                            color = TikTokTextSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                OutlinedTextField(
                    value = caption,
                    onValueChange = onCaptionChange,
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    maxLines = 7,
                    label = { Text("Note") },
                    placeholder = { Text("What happened here? What do you want to remember?") },
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TikTokAccent,
                        unfocusedBorderColor = TikTokOutline.copy(alpha = 0.7f),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        modifier = Modifier.weight(1f).clickable(onClick = onDismiss),
                        color = TikTokSurfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(modifier = Modifier.padding(vertical = 14.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = "Cancel",
                                color = Color.Black,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                    Button(onClick = onPost, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                        Text("Post")
                    }
                }
            }
        }
    }
}

@Composable
private fun CaptionEditSheet(
    caption: String,
    onCaptionChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.58f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = false) {}
                .navigationBarsPadding(),
            color = Color(0xFFF8F8F8),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            tonalElevation = 0.dp,
            shadowElevation = 14.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Edit caption",
                        color = Color.Black,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Outlined.MoreHoriz,
                            contentDescription = "Close editor",
                            tint = Color.Black.copy(alpha = 0.65f)
                        )
                    }
                }
                Text(
                    text = "Make it readable on the feed. Line breaks and long captions are supported.",
                    color = TikTokTextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = caption,
                    onValueChange = onCaptionChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .requiredHeightIn(min = 180.dp, max = 260.dp),
                    minLines = 7,
                    maxLines = 12,
                    label = { Text("Caption") },
                    placeholder = { Text("Write a detailed caption") },
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TikTokOutline,
                        unfocusedBorderColor = TikTokOutline.copy(alpha = 0.7f)
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(onClick = onDismiss),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, TikTokOutline),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Cancel",
                                color = Color.Black,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                    Button(
                        onClick = onSave,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
private fun rememberVideoThumbnail(localPath: String?): androidx.compose.ui.graphics.ImageBitmap? {
    val thumbnail by produceState<Bitmap?>(initialValue = null, localPath) {
        value = if (localPath.isNullOrBlank()) {
            null
        } else {
            withContext(Dispatchers.IO) {
                runCatching {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                        ThumbnailUtils.createVideoThumbnail(
                            File(localPath),
                            android.util.Size(512, 512),
                            null
                        )
                    } else {
                        val retriever = MediaMetadataRetriever()
                        try {
                            retriever.setDataSource(localPath)
                            retriever.getFrameAtTime(0)
                        } finally {
                            retriever.release()
                        }
                    }
                }.getOrNull()
            }
        }
    }
    return thumbnail?.asImageBitmap()
}

private fun persistReadPermission(context: Context, uri: Uri): Uri {
    runCatching {
        context.contentResolver.takePersistableUriPermission(
            uri,
            android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
    }
    return uri
}

private fun openVideoLocation(context: Context, video: StoredVideo?) {
    if (video == null) {
        Toast.makeText(context, "Couldn't find this video location", Toast.LENGTH_SHORT).show()
        return
    }

    val folderUri = video.sourceFolderUri
        ?.takeIf { it.isNotBlank() && it != "__all_videos__" }
        ?.let(Uri::parse)

    if (folderUri == null) {
        Toast.makeText(context, "This video does not have a folder location to open", Toast.LENGTH_SHORT).show()
        return
    }

    val initialFolderUri = runCatching {
        if (DocumentsContract.isTreeUri(folderUri)) {
            DocumentsContract.buildDocumentUriUsingTree(
                folderUri,
                DocumentsContract.getTreeDocumentId(folderUri)
            )
        } else {
            folderUri
        }
    }.getOrDefault(folderUri)

    val opened = runCatching {
        context.startActivity(
            Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                putExtra("android.provider.extra.INITIAL_URI", initialFolderUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
        true
    }.getOrDefault(false)

    if (!opened) {
        Toast.makeText(context, "No app found to open this folder location", Toast.LENGTH_SHORT).show()
    }
}

private fun sampleFeedPosts(): List<VideoPostUiModel> = listOf(
    VideoPostUiModel(
        id = "sample-stage",
        uri = null,
        localPath = null,
        username = "@craig_love",
        caption = "The most satisfying Job #fyp #satisfying #roadmarking",
        song = "Roddy Roundicch - The Rou",
        likes = "328.7K",
        comments = "578",
        shares = "Share",
        avatarBrush = Brush.radialGradient(
            listOf(Color(0xFFFECE8C), Color(0xFFBB4C31), Color(0xFF2F201B))
        )
    )
)

@Composable
private fun TodoScreen(
    todos: List<TodoItem>,
    selectedTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit,
    onCreateClick: () -> Unit,
    onAddTodo: (String) -> Unit,
    onToggleTodo: (String) -> Unit,
    onDeleteTodo: (String) -> Unit
) {
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var newTodoText by rememberSaveable { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize().background(TikTokBackground)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(bottom = 68.dp)
        ) {
            // Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Todo list",
                            color = Color.Black,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${todos.count { it.isDone }}/${todos.size} completed",
                            color = TikTokTextSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Surface(
                        modifier = Modifier.clickable { showAddDialog = true },
                        color = TikTokTodoAccent,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = "Add todo",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Add",
                                color = Color.White,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                }
            }

            // Progress bar
            if (todos.isNotEmpty()) {
                val doneRatio = todos.count { it.isDone }.toFloat() / todos.size
                val animatedProgress by animateFloatAsState(doneRatio, label = "todoProgress")
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = TikTokSuccess,
                    trackColor = TikTokOutline
                )
            }

            // Todo list
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (todos.isEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                        color = Color.White,
                        shape = RoundedCornerShape(24.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TikTokOutline.copy(alpha = 0.6f))
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = null,
                                tint = TikTokTodoAccent,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "No tasks yet",
                                color = Color.Black,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Tap + Add to create your first task.\nTrack what to update in your video journal.",
                                color = TikTokTextSecondary,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    // Undone items first, then done
                    val undone = todos.filter { !it.isDone }
                    val done = todos.filter { it.isDone }

                    undone.forEach { todo ->
                        TodoItemCard(
                            todo = todo,
                            onToggle = { onToggleTodo(todo.id) },
                            onDelete = { onDeleteTodo(todo.id) }
                        )
                    }

                    if (done.isNotEmpty() && undone.isNotEmpty()) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = TikTokOutline
                        )
                        Text(
                            text = "Completed",
                            color = TikTokTextSecondary,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                        )
                    }

                    done.forEach { todo ->
                        TodoItemCard(
                            todo = todo,
                            onToggle = { onToggleTodo(todo.id) },
                            onDelete = { onDeleteTodo(todo.id) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        BottomNavBar(
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding(),
            selectedTab = selectedTab,
            onTabSelected = onTabSelected,
            onCreateClick = onCreateClick,
            darkTheme = false
        )

        // Add dialog
        if (showAddDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.50f))
                    .clickable {
                        showAddDialog = false
                        newTodoText = ""
                    },
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .clickable(enabled = false) {},
                    color = Color.White,
                    shape = RoundedCornerShape(24.dp),
                    shadowElevation = 16.dp
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "New task",
                            color = Color.Black,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        OutlinedTextField(
                            value = newTodoText,
                            onValueChange = { newTodoText = it },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 5,
                            placeholder = { Text("What needs to be updated?") },
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TikTokTodoAccent,
                                unfocusedBorderColor = TikTokOutline.copy(alpha = 0.7f),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        showAddDialog = false
                                        newTodoText = ""
                                    },
                                color = TikTokSurfaceVariant,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Cancel",
                                        color = Color.Black,
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                }
                            }
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        if (newTodoText.isNotBlank()) {
                                            onAddTodo(newTodoText)
                                            newTodoText = ""
                                            showAddDialog = false
                                        }
                                    },
                                color = TikTokTodoAccent,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Add task",
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TodoItemCard(
    todo: TodoItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (todo.isDone) TikTokSuccess.copy(alpha = 0.3f) else TikTokOutline.copy(alpha = 0.6f)
        ),
        shadowElevation = if (todo.isDone) 0.dp else 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = if (todo.isDone) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                contentDescription = if (todo.isDone) "Mark undone" else "Mark done",
                tint = if (todo.isDone) TikTokSuccess else TikTokTodoUndone,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = todo.text,
                color = if (todo.isDone) TikTokTextSecondary else Color.Black,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    textDecoration = if (todo.isDone) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                ),
                modifier = Modifier.weight(1f)
            )
            // Copy text button
            IconButton(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Todo", todo.text))
                    Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.ContentCopy,
                    contentDescription = "Copy text",
                    tint = TikTokTextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
            // Delete button
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Delete todo",
                    tint = TikTokAccent.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

private enum class BottomTab { Home, Todo, Inbox, Profile }
private enum class ProfileSection { Posts, Favorites, Folders }

private data class VideoPostUiModel(
    val id: String,
    val uri: Uri?,
    val localPath: String?,
    val username: String,
    val caption: String,
    val song: String,
    val likes: String,
    val comments: String,
    val shares: String,
    val avatarBrush: Brush,
    val isEditable: Boolean = false
)

private data class FavoriteMoveProgress(
    val videoName: String,
    val progress: Int
)

private fun StoredVideo.normalizedCaption(): String {
    val trimmedCaption = caption.trim()
    val fileStem = displayName.substringBeforeLast('.').trim()
    return if (trimmedCaption.equals(fileStem, ignoreCase = true)) "" else trimmedCaption
}

private fun StoredVideo.isFavoriteVideo(): Boolean {
    val candidate = listOfNotNull(localPath, sourceUri).joinToString(" ").lowercase()
    return "favorite vidios" in candidate || "local tiktok favorite" in candidate
}

private fun StoredVideo.toVideoPostUiModel(): VideoPostUiModel {
    val uri = when {
        localPath.startsWith("content://") || localPath.startsWith("file://") -> Uri.parse(localPath)
        else -> Uri.fromFile(java.io.File(localPath))
    }
    return VideoPostUiModel(
        id = id,
        uri = uri,
        localPath = localPath,
        username = "",
        caption = normalizedCaption(),
        song = displayName,
        likes = "0",
        comments = comments.size.toString(),
        shares = "Share",
        avatarBrush = Brush.linearGradient(
            listOf(Color(0xFF32E0C4), Color(0xFF0E6BA8))
        ),
        isEditable = true
    )
}

private fun cleanSharedUrl(raw: String): String {
    val directUrl = Regex("""https?://\S+""").find(raw)?.value ?: raw.trim()
    return Uri.parse(directUrl).buildUpon().clearQuery().fragment(null).build().toString()
}

private fun linkDisplayTitle(url: String): String {
    val uri = Uri.parse(url)
    val host = uri.host?.removePrefix("www.") ?: "Shared link"
    val path = uri.path.orEmpty()
    return when {
        "tiktok" in host && path.contains("/video/") -> "TikTok video"
        "youtube" in host || host == "youtu.be" -> "YouTube link"
        "instagram" in host -> "Instagram link"
        else -> host.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun TikTokHomeScreenPreview() {
    TikTokUITheme(darkTheme = false, dynamicColor = false) {
        TikTokHomeScreen()
    }
}
