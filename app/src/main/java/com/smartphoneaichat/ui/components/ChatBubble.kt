package com.smartphoneaichat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.smartphoneaichat.domain.model.ChatRole
import com.smartphoneaichat.domain.model.Message
import com.smartphoneaichat.ui.theme.AccentBlue
import com.smartphoneaichat.ui.theme.AiBubble
import com.smartphoneaichat.ui.theme.UserBubble

/**
 * Renders a single chat bubble — user or AI — with appropriate alignment,
 * coloring, and optional "Thinking..." section for AI messages.
 *
 * Includes an animated avatar icon beside each bubble.
 */
@Composable
fun ChatBubble(
    message: Message,
    isThinkingExpanded: Boolean,
    onToggleThinking: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isUser = message.role == ChatRole.USER
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val bubbleColor = if (isUser) UserBubble else AiBubble
    val bubbleShape = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = if (isUser) 16.dp else 4.dp,
        bottomEnd = if (isUser) 4.dp else 16.dp
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = alignment
    ) {
        Row(
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Bottom
        ) {
            // Avatar (AI only — shown on the left)
            if (!isUser) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(AccentBlue.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = "AI",
                        tint = AccentBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
            }

            // Bubble content
            Column(
                modifier = Modifier.widthIn(max = 300.dp)
            ) {
                // "Thinking..." section for AI messages with reasoning text
                if (!isUser && message.thinkingText.isNotBlank()) {
                    ThinkingSection(
                        thinkingText = message.thinkingText,
                        isExpanded = isThinkingExpanded,
                        onToggle = onToggleThinking,
                        isStreaming = message.isStreaming,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                // The main text bubble
                Box(
                    modifier = Modifier
                        .clip(bubbleShape)
                        .background(bubbleColor)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    if (message.isStreaming && message.text.value.isEmpty()) {
                        // Streaming indicator — pulsing dots via simple text
                        Text(
                            text = "Thinking...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = message.text.value,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Avatar (user — shown on the right)
            if (isUser) {
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(UserBubble.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "You",
                        tint = UserBubble,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
