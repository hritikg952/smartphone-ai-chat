package com.smartphoneaichat.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.smartphoneaichat.ui.theme.AccentBlue
import com.smartphoneaichat.ui.theme.TextSecondary

/**
 * Collapsible "Thinking..." accordion section displayed above an AI response.
 *
 * Simulates chain-of-thought / reasoning output. The user can tap the header
 * to expand or collapse the reasoning text.
 *
 * Animates its height via [animateContentSize] for a fluid expand/collapse.
 *
 * AI INTEGRATION NOTE:
 * [thinkingText] would come from the model's reasoning/thought field.
 * For example:
 *   - OpenAI o1: `response.choices[0].message.reasoning`
 *   - DeepSeek-R1: `response.choices[0].message.content` (before <｜end▁of▁thinking｜>)
 *   - Gemini: `response.candidates[0].finishReason` and intermediate tokens
 */
@Composable
fun ThinkingSection(
    thinkingText: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    isStreaming: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable(onClick = onToggle)
            .padding(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Psychology,
                contentDescription = "Thinking",
                tint = AccentBlue,
                modifier = Modifier.size(16.dp)
            )

            Spacer(Modifier.width(6.dp))

            Text(
                text = if (isStreaming) "Thinking..." else "Thought process",
                style = MaterialTheme.typography.labelMedium,
                color = AccentBlue
            )

            Spacer(Modifier.weight(1f))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                tint = TextSecondary,
                modifier = Modifier
                    .size(18.dp)
                    .rotate(if (isExpanded) 90f else 0f)
            )
        }

        if (isExpanded) {
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .heightIn(max = 200.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = thinkingText,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
