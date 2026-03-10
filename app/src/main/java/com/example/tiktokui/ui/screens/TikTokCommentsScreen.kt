package com.example.tiktokui.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tiktokui.ui.theme.TikTokAccent
import com.example.tiktokui.ui.theme.TikTokLike
import com.example.tiktokui.ui.theme.TikTokScrim
import com.example.tiktokui.ui.theme.TikTokUITheme

@Composable
fun TikTokCommentsScreen(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit = {},
    showStandaloneBackdrop: Boolean = true,
    comments: List<CommentUiModel> = sampleComments
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .then(
                if (showStandaloneBackdrop) {
                    Modifier.background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF141414), Color(0xFF0B0B0B))
                        )
                    )
                } else {
                    Modifier.background(Color.Black.copy(alpha = 0.42f))
                }
            )
    ) {
        if (showStandaloneBackdrop) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(TikTokScrim)
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .statusBarsPadding()
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                tonalElevation = 0.dp,
                shadowElevation = 10.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    DragHandle()
                    CommentsHeader(
                        totalComments = "45.2K",
                        onDismissRequest = onDismissRequest
                    )
                    SortRow()
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.65f))
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        items(comments) { comment ->
                            CommentRow(comment = comment)
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f))
                    CommentComposer()
                }
            }
        }
    }
}

@Composable
private fun DragHandle() {
    Box(
        modifier = Modifier
            .padding(top = 8.dp, bottom = 10.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(width = 42.dp, height = 4.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.9f))
        )
    }
}

@Composable
private fun CommentsHeader(totalComments: String, onDismissRequest: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        Text(
            text = "$totalComments comments",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            ),
            modifier = Modifier.align(Alignment.Center)
        )

        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onDismissRequest) {
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = "Collapse comments"
                )
            }
            IconButton(onClick = onDismissRequest) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Close comments"
                )
            }
        }
    }
}

@Composable
private fun SortRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SortChip(label = "Top", selected = true)
        SortChip(label = "Newest", selected = false)
    }
}

@Composable
private fun SortChip(label: String, selected: Boolean) {
    Surface(
        color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surfaceVariant,
        shape = CircleShape
    ) {
        Text(
            text = label,
            color = if (selected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun CommentRow(comment: CommentUiModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        AvatarBubble(seedColor = comment.avatarColor, initials = comment.initials)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = comment.username,
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                )
                Text(
                    text = comment.timestamp,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = comment.message,
                style = MaterialTheme.typography.bodyLarge.copy(
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Medium
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = {},
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "Reply",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
                if (comment.pinnedReply != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape
                    ) {
                        Text(
                            text = comment.pinnedReply,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = if (comment.likedByAuthor) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                contentDescription = "Like comment",
                tint = if (comment.likedByAuthor) TikTokLike else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = comment.likes,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
private fun AvatarBubble(seedColor: Color, initials: String) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(seedColor.copy(alpha = 0.9f), seedColor.copy(alpha = 0.55f))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            color = Color.White,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
private fun CommentComposer() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AvatarBubble(seedColor = TikTokAccent, initials = "Y")
        Spacer(modifier = Modifier.width(12.dp))
        Surface(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = CircleShape
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Add comment...",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Send,
                    contentDescription = "Send comment",
                    tint = TikTokAccent
                )
            }
        }
    }
}

@Immutable
data class CommentUiModel(
    val username: String,
    val timestamp: String,
    val message: String,
    val likes: String,
    val initials: String,
    val avatarColor: Color,
    val likedByAuthor: Boolean = false,
    val pinnedReply: String? = null
)

private val sampleComments = listOf(
    CommentUiModel(
        username = "amara.j",
        timestamp = "2h",
        message = "The transition into the chorus is unreal. I replayed it five times already.",
        likes = "18.4K",
        initials = "AJ",
        avatarColor = Color(0xFFFF7A59),
        likedByAuthor = true,
        pinnedReply = "View 214 replies"
    ),
    CommentUiModel(
        username = "noahcreates",
        timestamp = "1h",
        message = "Camera work, lighting, styling... everything about this clip feels expensive.",
        likes = "5,832",
        initials = "NC",
        avatarColor = Color(0xFF5468FF)
    ),
    CommentUiModel(
        username = "liz.in.motion",
        timestamp = "58m",
        message = "Anyone else need the outfit details because this look is too good to ignore?",
        likes = "1,204",
        initials = "LM",
        avatarColor = Color(0xFF12B886),
        pinnedReply = "Liked by creator"
    ),
    CommentUiModel(
        username = "kevobeats",
        timestamp = "31m",
        message = "This is the kind of sound design that makes scrolling stop instantly.",
        likes = "967",
        initials = "KB",
        avatarColor = Color(0xFFFFC145)
    ),
    CommentUiModel(
        username = "rae.s",
        timestamp = "11m",
        message = "The comments section passed the vibe check. We all heard the same thing.",
        likes = "412",
        initials = "RS",
        avatarColor = Color(0xFFB65CFF),
        likedByAuthor = true
    )
)

@Preview(showBackground = true, backgroundColor = 0xFF101010)
@Composable
private fun TikTokCommentsScreenPreview() {
    TikTokUITheme(darkTheme = false, dynamicColor = false) {
        TikTokCommentsScreen()
    }
}
