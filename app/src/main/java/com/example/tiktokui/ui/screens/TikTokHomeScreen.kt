package com.example.tiktokui.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubble
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.PersonAddAlt1
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.TurnedInNot
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tiktokui.ui.theme.TikTokAccent
import com.example.tiktokui.ui.theme.TikTokOutline
import com.example.tiktokui.ui.theme.TikTokSurfaceVariant
import com.example.tiktokui.ui.theme.TikTokTextSecondary
import com.example.tiktokui.ui.theme.TikTokUITheme

@Composable
fun TikTokHomeScreen(modifier: Modifier = Modifier) {
    var selectedTab by remember { mutableStateOf(BottomTab.Home) }
    var showComments by remember { mutableStateOf(false) }
    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var caption by remember { mutableStateOf("") }
    val videoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedVideoUri = uri
        if (uri != null) {
            caption = ""
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when (selectedTab) {
            BottomTab.Home -> {
                HomeFeedBackground()
                HomeFeedOverlay(
                    onCommentsClick = { showComments = true },
                    selectedTab = selectedTab,
                    onTabSelected = { tapped ->
                        selectedTab = if (tapped == BottomTab.Profile && selectedTab == BottomTab.Profile) {
                            BottomTab.Home
                        } else {
                            tapped
                        }
                    },
                    onCreateClick = {
                        showComments = false
                        videoPicker.launch("video/*")
                    }
                )
            }

            BottomTab.Profile -> {
                TikTokProfileScreen(
                    selectedTab = selectedTab,
                    onTabSelected = { tapped ->
                        selectedTab = if (tapped == BottomTab.Profile) BottomTab.Home else tapped
                    },
                    onCreateClick = {
                        videoPicker.launch("video/*")
                    }
                )
            }
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
                    selectedVideoUri = null
                }
            )
        }
    }
}

@Composable
private fun HomeFeedBackground() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF07151C),
                        Color(0xFF102730),
                        Color(0xFF0B0E14)
                    )
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
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 18f, cap = StrokeCap.Round)
            )
            drawArc(
                color = Color(0xFFE44362).copy(alpha = 0.45f),
                startAngle = 80f,
                sweepAngle = 150f,
                useCenter = false,
                topLeft = Offset(size.width * 0.22f, size.height * 0.63f),
                size = Size(size.width * 0.42f, size.height * 0.22f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 16f, cap = StrokeCap.Round)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
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
private fun HomeFeedOverlay(
    onCommentsClick: () -> Unit,
    selectedTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit,
    onCreateClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        TopTabs(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
        )

        BottomMetaBlock(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .navigationBarsPadding()
        )

        RightActionRail(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(bottom = 92.dp, end = 12.dp),
            onCommentsClick = onCommentsClick
        )

        BottomNavBar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding(),
            selectedTab = selectedTab,
            onTabSelected = onTabSelected,
            onCreateClick = onCreateClick
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
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        )
    }
}

@Composable
private fun BottomMetaBlock(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth(0.76f)
            .padding(start = 16.dp, bottom = 76.dp)
    ) {
        Text(
            text = "@craig_love",
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "The most satisfying Job #fyp #satisfying #roadmarking",
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium.copy(
                lineHeight = 20.sp
            )
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Rounded.MusicNote,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Roddy Roundicch - The Rou",
                color = Color.White.copy(alpha = 0.92f),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium)
            )
        }
    }
}

@Composable
private fun RightActionRail(modifier: Modifier = Modifier, onCommentsClick: () -> Unit) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Box(contentAlignment = Alignment.BottomCenter) {
            AvatarDisc()
            Box(
                modifier = Modifier
                    .padding(top = 48.dp)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(TikTokAccent),
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
            count = "328.7K"
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
            count = "578",
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
            count = "Share"
        )

        MusicDisc()
    }
}

@Composable
private fun SocialAction(
    icon: @Composable () -> Unit,
    count: String,
    onClick: (() -> Unit)? = null
) {
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
private fun AvatarDisc() {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFFFECE8C), Color(0xFFBB4C31), Color(0xFF2F201B))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFFDEA14A), Color(0xFF6C331E))
                    )
                )
        )
    }
}

@Composable
private fun MusicDisc() {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF4B4B4B), Color(0xFF1A1A1A), Color.Black)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(Color(0xFFF5C542))
        )
    }
}

@Composable
private fun BottomNavBar(
    modifier: Modifier = Modifier,
    selectedTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit,
    onCreateClick: () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.Black.copy(alpha = 0.95f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(
                icon = Icons.Outlined.Home,
                label = "Home",
                selected = selectedTab == BottomTab.Home,
                onClick = { onTabSelected(BottomTab.Home) }
            )
            NavItem(icon = Icons.Outlined.Search, label = "Discover")
            CreateButton(onClick = onCreateClick)
            NavItem(icon = Icons.Outlined.ChatBubble, label = "Inbox")
            NavItem(
                icon = Icons.Outlined.PersonOutline,
                label = "Me",
                selected = selectedTab == BottomTab.Profile,
                onClick = { onTabSelected(BottomTab.Profile) }
            )
        }
    }
}

@Composable
private fun NavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean = false,
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
            tint = if (selected) Color.White else Color.White.copy(alpha = 0.72f),
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = label,
            color = if (selected) Color.White else Color.White.copy(alpha = 0.72f),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
        )
    }
}

@Composable
private fun TikTokProfileScreen(
    selectedTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit,
    onCreateClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            ProfileTopBar()
            HorizontalDivider(color = TikTokOutline)
            ProfileHeader()
            ProfileTabRow()
            ProfileGrid(
                modifier = Modifier.weight(1f)
            )
        }

        BottomNavBar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding(),
            selectedTab = selectedTab,
            onTabSelected = onTabSelected,
            onCreateClick = onCreateClick
        )
    }
}

@Composable
private fun ProfileTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
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
            Text(
                text = " \u25BE",
                color = Color.Black,
                style = MaterialTheme.typography.labelMedium
            )
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
private fun ProfileHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(84.dp)
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
                    .size(64.dp)
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
        Spacer(modifier = Modifier.height(18.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(28.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileStat(value = "14", label = "Following")
            ProfileStat(value = "38", label = "Followers")
            ProfileStat(value = "91", label = "Likes")
        }
        Spacer(modifier = Modifier.height(18.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                Box(
                    modifier = Modifier
                        .size(width = 36.dp, height = 36.dp),
                    contentAlignment = Alignment.Center
                ) {
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
        Text(
            text = "Tap to add bio",
            color = TikTokTextSecondary,
            style = MaterialTheme.typography.bodySmall
        )
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
        Text(
            text = label,
            color = TikTokTextSecondary,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun ProfileTabRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = TikTokOutline.copy(alpha = 0.45f))
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.GridOn,
            contentDescription = "Posts",
            tint = Color.Black,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(96.dp))
        Icon(
            imageVector = Icons.Outlined.Lock,
            contentDescription = "Private likes",
            tint = TikTokTextSecondary.copy(alpha = 0.6f),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun ProfileGrid(modifier: Modifier = Modifier) {
    val tiles = listOf(
        ProfileTileData(
            brush = Brush.verticalGradient(listOf(Color(0xFF0B3A56), Color(0xFF18D3F2), Color(0xFF09131B)))
        ),
        ProfileTileData(
            brush = Brush.verticalGradient(listOf(Color(0xFFD8D6D1), Color(0xFF8F8A84), Color(0xFF4B4A48)))
        ),
        ProfileTileData(
            brush = Brush.verticalGradient(listOf(Color(0xFF02120B), Color(0xFF125A1B), Color(0xFF78FF72)))
        ),
        ProfileTileData(
            brush = Brush.verticalGradient(listOf(Color(0xFFD8D7D8), Color(0xFFAAA7A5), Color(0xFF5B544F)))
        ),
        ProfileTileData(
            isCreateCard = true,
            brush = Brush.verticalGradient(listOf(Color(0xFFFDFDFD), Color(0xFFF6F4F4)))
        )
    )

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val gridPadding = 1.dp
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 68.dp),
            horizontalArrangement = Arrangement.spacedBy(gridPadding),
            verticalArrangement = Arrangement.spacedBy(gridPadding)
        ) {
            items(tiles) { tile ->
                ProfileGridTile(tile = tile)
            }
        }
    }
}

@Composable
private fun ProfileGridTile(tile: ProfileTileData) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(124.dp)
            .background(tile.brush),
        contentAlignment = Alignment.Center
    ) {
        if (tile.isCreateCard) {
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(8.dp),
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Canvas(
                        modifier = Modifier
                            .width(80.dp)
                            .height(18.dp)
                    ) {
                        drawCircle(Color(0xFF3CCAE6), radius = 10f, center = Offset(10f, size.height / 2))
                        drawArc(
                            color = Color(0xFFF74172),
                            startAngle = 20f,
                            sweepAngle = 220f,
                            useCenter = false,
                            topLeft = Offset(size.width * 0.28f, -4f),
                            size = Size(38f, 28f),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 10f, cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = Color(0xFF212121),
                            startAngle = 215f,
                            sweepAngle = 100f,
                            useCenter = false,
                            topLeft = Offset(size.width * 0.58f, 1f),
                            size = Size(24f, 20f),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 7f, cap = StrokeCap.Round)
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
}

private enum class BottomTab {
    Home,
    Profile
}

private data class ProfileTileData(
    val brush: Brush,
    val isCreateCard: Boolean = false
)

@Composable
private fun CreateButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(44.dp)
            .height(28.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .padding(end = 7.dp)
                .size(width = 44.dp, height = 28.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(Color(0xFF27F4F2))
        )
        Box(
            modifier = Modifier
                .padding(start = 7.dp)
                .size(width = 44.dp, height = 28.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(Color(0xFFFF315A))
        )
        Box(
            modifier = Modifier
                .size(width = 38.dp, height = 28.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(Color.White),
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
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = false) {}
                .navigationBarsPadding(),
            color = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            tonalElevation = 0.dp,
            shadowElevation = 10.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
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
                        modifier = Modifier
                            .weight(1f)
                            .clickable(onClick = onDismiss),
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
                    Button(
                        onClick = onPost,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Post")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun TikTokHomeScreenPreview() {
    TikTokUITheme(darkTheme = false, dynamicColor = false) {
        TikTokHomeScreen()
    }
}
