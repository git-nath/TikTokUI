package com.example.tiktokui.ui.screens

import android.content.Intent
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.net.Uri
import android.widget.Toast
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.outlined.ChatBubble
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.PersonAddAlt1
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.OpenInBrowser
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.TurnedInNot
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.example.tiktokui.ui.theme.TikTokAccent
import com.example.tiktokui.ui.theme.TikTokOutline
import com.example.tiktokui.ui.theme.TikTokSurfaceVariant
import com.example.tiktokui.ui.theme.TikTokTextSecondary
import com.example.tiktokui.ui.theme.TikTokUITheme
import java.util.UUID
import java.io.File
import kotlinx.coroutines.Dispatchers
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
    var appState by remember { mutableStateOf(store.load()) }
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
    val videoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedVideoUri = uri
        if (uri != null) caption = ""
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
                            localPath = store.copyIntoLibrary(source.uri, source.displayName),
                            sourceUri = source.uri.toString(),
                            sourceFolderUri = uri.toString(),
                            displayName = source.displayName,
                            caption = source.displayName.substringBeforeLast('.')
                        )
                    }
                }
            }
            appState = appState.copy(
                folders = (appState.folders + SelectedFolder(uri = uri.toString(), name = folderName)).distinctBy { it.uri },
                videos = (importedVideos + appState.videos).distinctBy { it.localPath }
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

    val homeFeedPosts = remember(appState.videos, appState.playOrder) {
        val importedPosts = appState.videos.map { it.toVideoPostUiModel() }
        val orderedPosts = when (appState.playOrder) {
            PlayOrder.Sequential -> importedPosts
            PlayOrder.Shuffle -> importedPosts.shuffled()
        }
        orderedPosts + samplePosts
    }
    val pagerState = rememberPagerState(initialPage = 0) { homeFeedPosts.size }

    LaunchedEffect(homeFeedPosts.size) {
        if (selectedTab == BottomTab.Home && appState.videos.isNotEmpty() && pagerState.currentPage != 0) {
            pagerState.animateScrollToPage(0)
        }
    }

    LaunchedEffect(pagerState.currentPage, selectedTab) {
        if (selectedTab == BottomTab.Home) {
            pausedVideoId = null
            showComments = false
            expandedCaptionPostId = null
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
                expandedCaptionPostId = expandedCaptionPostId,
                onToggleCaption = { postId ->
                    expandedCaptionPostId = if (expandedCaptionPostId == postId) null else postId
                },
                onCaptionChange = { postId, updatedCaption ->
                    val index = appState.videos.indexOfFirst { it.id == postId }
                    if (index >= 0) {
                        val updatedVideos = appState.videos.toMutableList()
                        updatedVideos[index] = updatedVideos[index].copy(caption = updatedCaption)
                        appState = appState.copy(videos = updatedVideos)
                    }
                },
                selectedTab = selectedTab,
                onTabSelected = { tapped ->
                    selectedTab = when {
                        tapped == BottomTab.Profile && selectedTab == BottomTab.Profile -> BottomTab.Home
                        tapped == BottomTab.Inbox && selectedTab == BottomTab.Inbox -> BottomTab.Home
                        else -> tapped
                    }
                },
                onCreateClick = {
                    showComments = false
                    videoPicker.launch("video/*")
                }
            )

            BottomTab.Profile -> TikTokProfileScreen(
                postedVideos = appState.videos.map { it.toVideoPostUiModel() },
                selectedFolders = appState.folders,
                playOrder = appState.playOrder,
                selectedSection = profileSection,
                selectedTab = selectedTab,
                onTabSelected = { tapped ->
                    selectedTab = if (tapped == BottomTab.Profile) BottomTab.Home else tapped
                },
                onCreateClick = { videoPicker.launch("video/*") },
                onAddFolderClick = { folderPicker.launch(null) },
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

            BottomTab.Inbox -> InboxScreen(
                links = appState.inboxLinks,
                selectedTab = selectedTab,
                onTabSelected = { tapped ->
                    selectedTab = if (tapped == BottomTab.Inbox) BottomTab.Home else tapped
                },
                onCreateClick = { videoPicker.launch("video/*") },
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
                    appState = appState.copy(
                        videos = listOf(
                            StoredVideo(
                                id = UUID.randomUUID().toString(),
                                localPath = store.copyIntoLibrary(uri, store.resolveDisplayName(uri)),
                                sourceUri = uri.toString(),
                                sourceFolderUri = null,
                                displayName = store.resolveDisplayName(uri),
                                caption = caption.ifBlank { "New post" }
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

    }
}

@Composable
private fun HomeFeedPager(
    posts: List<VideoPostUiModel>,
    pagerState: PagerState,
    pausedVideoId: String?,
    onTogglePlayback: (String) -> Unit,
    onCommentsClick: () -> Unit,
    expandedCaptionPostId: String?,
    onToggleCaption: (String) -> Unit,
    onCaptionChange: (String, String) -> Unit,
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
                isCaptionExpanded = expandedCaptionPostId == post.id,
                onToggleCaption = { onToggleCaption(post.id) },
                onCaptionChange = { updatedCaption -> onCaptionChange(post.id, updatedCaption) }
            )
        }

        BottomNavBar(
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding(),
            selectedTab = selectedTab,
            onTabSelected = onTabSelected,
            onCreateClick = onCreateClick,
            darkTheme = true
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
    isCaptionExpanded: Boolean,
    onToggleCaption: () -> Unit,
    onCaptionChange: (String) -> Unit
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

        FeedOverlay(
            post = post,
            showTabs = post.uri == null,
            onCommentsClick = onCommentsClick,
            isCaptionExpanded = isCaptionExpanded,
            onToggleCaption = onToggleCaption,
            onCaptionChange = onCaptionChange
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
                setOnPreparedListener { mediaPlayer ->
                    mediaPlayer.isLooping = true
                    if (shouldPlay) start()
                }
            }
        },
        update = { view ->
            if (shouldPlay) {
                if (!view.isPlaying) view.start()
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
    isCaptionExpanded: Boolean,
    onToggleCaption: () -> Unit,
    onCaptionChange: (String) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (showTabs) {
            TopTabs(modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding())
        }

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
                onDismiss = onToggleCaption,
                onCaptionChange = onCaptionChange
            )
        }

        RightActionRail(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(bottom = 92.dp, end = 12.dp),
            post = post,
            onCommentsClick = onCommentsClick
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
private fun TopTabs(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.padding(top = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Following",
            color = Color.White.copy(alpha = 0.72f),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium)
        )
        Text(
            text = "For You",
            color = Color.White,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp)
        )
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
    onDismiss: () -> Unit,
    onCaptionChange: (String) -> Unit
) {
    val scrollState = rememberScrollState()
    val panelAlpha by animateFloatAsState(if (post.isEditable) 0.96f else 0.92f, label = "captionPanelAlpha")
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onDismiss)
    ) {
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
                    .clickable(enabled = false) {},
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
                        OutlinedTextField(
                            value = post.caption,
                            onValueChange = onCaptionChange,
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 8,
                            maxLines = 20,
                            placeholder = { Text("Write caption", color = Color.White.copy(alpha = 0.55f)) },
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = Color.White,
                                fontSize = 16.sp,
                                lineHeight = 27.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            shape = RoundedCornerShape(16.dp)
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
    onCommentsClick: () -> Unit
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
            count = post.shares
        )

        MusicDisc()
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
private fun MusicDisc() {
    Box(
        modifier = Modifier.size(44.dp).clip(CircleShape).background(
            Brush.radialGradient(colors = listOf(Color(0xFF4B4B4B), Color(0xFF1A1A1A), Color.Black))
        ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.size(14.dp).clip(CircleShape).background(Color(0xFFF5C542))
        )
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
    val surfaceColor = if (darkTheme) Color.Black.copy(alpha = 0.95f) else Color.White
    val foreground = if (darkTheme) Color.White else Color.Black
    val secondary = if (darkTheme) Color.White.copy(alpha = 0.72f) else TikTokTextSecondary

    Surface(modifier = modifier.fillMaxWidth(), color = surfaceColor) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(
                icon = Icons.Outlined.Home,
                label = "Home",
                selected = selectedTab == BottomTab.Home,
                activeColor = foreground,
                inactiveColor = secondary,
                onClick = { onTabSelected(BottomTab.Home) }
            )
            NavItem(
                icon = Icons.Outlined.Search,
                label = "Discover",
                activeColor = foreground,
                inactiveColor = secondary
            )
            CreateButton(onClick = onCreateClick)
            NavItem(
                icon = Icons.Outlined.ChatBubble,
                label = "Inbox",
                selected = selectedTab == BottomTab.Inbox,
                activeColor = foreground,
                inactiveColor = secondary,
                onClick = { onTabSelected(BottomTab.Inbox) }
            )
            NavItem(
                icon = Icons.Outlined.PersonOutline,
                label = "Me",
                selected = selectedTab == BottomTab.Profile,
                activeColor = foreground,
                inactiveColor = secondary,
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
    onClick: (() -> Unit)? = null
) {
    Column(
        modifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) activeColor else inactiveColor,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = label,
            color = if (selected) activeColor else inactiveColor,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
        )
    }
}

@Composable
private fun TikTokProfileScreen(
    postedVideos: List<VideoPostUiModel>,
    selectedFolders: List<SelectedFolder>,
    playOrder: PlayOrder,
    selectedSection: ProfileSection,
    selectedTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit,
    onCreateClick: () -> Unit,
    onAddFolderClick: () -> Unit,
    onSectionSelected: (ProfileSection) -> Unit,
    onTogglePlayOrder: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            ProfileTopBar()
            HorizontalDivider(color = TikTokOutline)
            ProfileHeader(totalPosts = postedVideos.size)
            ProfileTabRow(
                selectedSection = selectedSection,
                onSectionSelected = onSectionSelected
            )
            when (selectedSection) {
                ProfileSection.Posts -> ProfileGrid(modifier = Modifier.weight(1f), postedVideos = postedVideos)
                ProfileSection.Folders -> FolderManagerCard(
                    modifier = Modifier.weight(1f),
                    folders = selectedFolders,
                    playOrder = playOrder,
                    onAddFolderClick = onAddFolderClick,
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
    onAddFolderClick: () -> Unit,
    onTogglePlayOrder: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Surface(
            color = Color(0xFFF7F8FA),
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, TikTokOutline.copy(alpha = 0.55f))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Selected folders",
                    color = Color.Black,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                if (folders.isEmpty()) {
                    Text(
                        text = "No folders added yet. Add a folder and the app will import its videos into LocalTikTok.",
                        color = TikTokTextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        folders.forEach { folder ->
                            Surface(
                                color = Color.White,
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
                                            text = "Imported into LocalTikTok",
                                            color = TikTokTextSecondary,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Outlined.Lock,
                                        contentDescription = null,
                                        tint = Color.Black.copy(alpha = 0.72f)
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
                modifier = Modifier.weight(1f).clickable(onClick = onAddFolderClick),
                color = Color.White,
                shape = RoundedCornerShape(18.dp),
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
                color = Color(0xFF111111),
                shape = RoundedCornerShape(18.dp)
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
    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Inbox",
                color = Color.Black,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Saved links are cleaned up first, then previewed inside the app before you open them elsewhere.",
                color = TikTokTextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (links.isEmpty()) {
                    Surface(
                        color = Color(0xFFF7F7F7),
                        shape = RoundedCornerShape(18.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TikTokOutline.copy(alpha = 0.6f))
                    ) {
                        Text(
                            text = "No shared links yet. Use Android Share on a link and choose this app.",
                            color = TikTokTextSecondary,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    links.forEach { link ->
                        val cleanedUrl = remember(link.url) { cleanSharedUrl(link.url) }
                        val title = remember(cleanedUrl) { linkDisplayTitle(cleanedUrl) }
                        Surface(
                            color = Color(0xFFF7F7F7),
                            shape = RoundedCornerShape(22.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, TikTokOutline.copy(alpha = 0.6f))
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
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.PersonAddAlt1,
            contentDescription = "Add friends",
            tint = Color.Black,
            modifier = Modifier.size(22.dp)
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Jacob West",
                color = Color.Black,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(text = " \u25BE", color = Color.Black, style = MaterialTheme.typography.labelMedium)
        }
        Icon(
            imageVector = Icons.Outlined.MoreHoriz,
            contentDescription = "More",
            tint = Color.Black,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun ProfileHeader(totalPosts: Int) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(84.dp).clip(CircleShape).background(
                Brush.radialGradient(colors = listOf(Color(0xFFFFC78E), Color(0xFFE07B52), Color(0xFF8F4E2F)))
            ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier.size(64.dp).clip(CircleShape).background(
                    Brush.verticalGradient(colors = listOf(Color(0xFF9B7A58), Color(0xFF5F4835)))
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "@jacob_w",
            color = Color.Black,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
        )
        Spacer(modifier = Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(28.dp), verticalAlignment = Alignment.CenterVertically) {
            ProfileStat(value = "14", label = "Following")
            ProfileStat(value = "38", label = "Followers")
            ProfileStat(value = totalPosts.toString(), label = "Posts")
        }
        Spacer(modifier = Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(4.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, TikTokOutline)
            ) {
                Text(
                    text = "Edit profile",
                    color = Color.Black,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.padding(horizontal = 30.dp, vertical = 9.dp)
                )
            }
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(4.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, TikTokOutline)
            ) {
                Box(modifier = Modifier.size(width = 36.dp, height = 36.dp), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.TurnedInNot,
                        contentDescription = "Bookmarks",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(text = "Tap to add bio", color = TikTokTextSecondary, style = MaterialTheme.typography.bodySmall)
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
            .padding(horizontal = 16.dp, vertical = 8.dp),
        color = Color(0xFFF6F7F9),
        shape = RoundedCornerShape(18.dp)
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
                label = "Folders",
                icon = Icons.Outlined.Lock,
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
        color = if (selected) Color.White else Color.Transparent,
        shape = RoundedCornerShape(14.dp),
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
                tint = if (selected) Color.Black else TikTokTextSecondary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                color = if (selected) Color.Black else TikTokTextSecondary,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
            )
        }
    }
}

@Composable
private fun ProfileGrid(modifier: Modifier = Modifier, postedVideos: List<VideoPostUiModel>) {
    val tiles = remember(postedVideos) {
        if (postedVideos.isEmpty()) listOf(ProfileGridItem.CreateCard)
        else postedVideos.map { ProfileGridItem.VideoTile(it) }
    }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 68.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tiles) { tile ->
                when (tile) {
                    is ProfileGridItem.VideoTile -> ProfileVideoTile(tile.post)
                    ProfileGridItem.CreateCard -> CreateCardTile()
                }
            }
        }
    }
}

@Composable
private fun ProfileVideoTile(post: VideoPostUiModel) {
    val thumbnail = rememberVideoThumbnail(post.localPath)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(156.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Black),
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
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.10f)))
        Surface(
            modifier = Modifier.padding(10.dp),
            color = Color.Black.copy(alpha = 0.46f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = post.caption,
                color = Color.White,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun CreateCardTile() {
    Box(
        modifier = Modifier.fillMaxWidth().height(124.dp).background(
            Brush.verticalGradient(listOf(Color(0xFFFDFDFD), Color(0xFFF6F4F4)))
        ),
        contentAlignment = Alignment.Center
    ) {
        Surface(color = Color.White, shape = RoundedCornerShape(8.dp), shadowElevation = 2.dp) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Canvas(modifier = Modifier.width(80.dp).height(18.dp)) {
                    drawCircle(Color(0xFF3CCAE6), radius = 10f, center = Offset(10f, size.height / 2))
                    drawArc(
                        color = Color(0xFFF74172),
                        startAngle = 20f,
                        sweepAngle = 220f,
                        useCenter = false,
                        topLeft = Offset(size.width * 0.28f, -4f),
                        size = Size(38f, 28f),
                        style = Stroke(width = 10f, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = Color(0xFF212121),
                        startAngle = 215f,
                        sweepAngle = 100f,
                        useCenter = false,
                        topLeft = Offset(size.width * 0.58f, 1f),
                        size = Size(24f, 20f),
                        style = Stroke(width = 7f, cap = StrokeCap.Round)
                    )
                }
                Text(
                    text = "Tap to create",
                    color = Color.Black,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "a new video",
                    color = Color.Black.copy(alpha = 0.78f),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
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
            color = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            tonalElevation = 0.dp,
            shadowElevation = 10.dp
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "New video",
                    color = Color.Black,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = selectedVideoUri?.lastPathSegment ?: "Selected video",
                    color = TikTokTextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = caption,
                    onValueChange = onCaptionChange,
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    maxLines = 6,
                    label = { Text("Caption") },
                    placeholder = { Text("Write a caption for your video") }
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
                    shape = RoundedCornerShape(18.dp)
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
            android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
    }
    return uri
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

private enum class BottomTab { Home, Inbox, Profile }
private enum class ProfileSection { Posts, Folders }

private sealed interface ProfileGridItem {
    data class VideoTile(val post: VideoPostUiModel) : ProfileGridItem
    data object CreateCard : ProfileGridItem
}

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

private fun StoredVideo.toVideoPostUiModel(): VideoPostUiModel {
    val uri = Uri.fromFile(java.io.File(localPath))
    return VideoPostUiModel(
        id = id,
        uri = uri,
        localPath = localPath,
        username = "",
        caption = caption,
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
