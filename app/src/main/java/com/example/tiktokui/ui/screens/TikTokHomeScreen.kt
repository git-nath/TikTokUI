package com.example.tiktokui.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubble
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tiktokui.ui.theme.TikTokAccent
import com.example.tiktokui.ui.theme.TikTokUITheme

@Composable
fun TikTokHomeScreen(modifier: Modifier = Modifier) {
    var showComments by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        HomeFeedBackground()
        HomeFeedOverlay(
            onCommentsClick = { showComments = true }
        )

        if (showComments) {
            TikTokCommentsScreen(
                onDismissRequest = { showComments = false },
                showStandaloneBackdrop = false
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
private fun HomeFeedOverlay(onCommentsClick: () -> Unit) {
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
                .navigationBarsPadding()
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
private fun BottomNavBar(modifier: Modifier = Modifier) {
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
            NavItem(icon = Icons.Outlined.Home, label = "Home", selected = true)
            NavItem(icon = Icons.Outlined.Search, label = "Discover")
            CreateButton()
            NavItem(icon = Icons.Outlined.ChatBubble, label = "Inbox")
            NavItem(icon = Icons.Outlined.PersonOutline, label = "Me")
        }
    }
}

@Composable
private fun NavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean = false
) {
    Column(
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
private fun CreateButton() {
    Box(
        modifier = Modifier
            .width(44.dp)
            .height(28.dp),
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

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun TikTokHomeScreenPreview() {
    TikTokUITheme(darkTheme = false, dynamicColor = false) {
        TikTokHomeScreen()
    }
}
