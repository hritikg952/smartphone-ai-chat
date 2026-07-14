package com.smartphoneaichat.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import com.smartphoneaichat.domain.model.Attachment
import com.smartphoneaichat.ui.theme.AccentBlue
import com.smartphoneaichat.ui.theme.AiBubbleBorder
import com.smartphoneaichat.ui.theme.DarkSurfaceVariant
import com.smartphoneaichat.ui.theme.TextPrimary
import com.smartphoneaichat.ui.theme.TextSecondary

/**
 * Bottom-anchored chat input bar with image-attach button, text field, and
 * send button.
 *
 * When an [attachment] is provided, a thumbnail row animates in above the
 * text field showing the attached file name with a remove button.
 *
 * Animations:
 * - [animateContentSize] on the outer container for smooth thumbnail appearance.
 * - [AnimatedVisibility] for the thumbnail row.
 */
@Composable
fun ChatInput(
    maxLength: Int = 4096,
    attachment: Attachment?,
    onSend: (String) -> Unit,
    onAttachImage: () -> Unit,
    onRemoveAttachment: () -> Unit,
    modifier: Modifier = Modifier
) {
    var text by rememberSaveable { mutableStateOf("") }
    var isFocused by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .animateContentSize()
    ) {
        // ── Attachment thumbnail row ──────────────────────────────
        AnimatedVisibility(
            visible = attachment != null,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut()
        ) {
            attachment?.let { att ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val imageUri = att.imageUri
                    val bitmap by produceState<Bitmap?>(null, imageUri) {
                        if (imageUri != null) {
                            value = withContext(Dispatchers.IO) {
                                val file = File(imageUri)
                                if (file.exists()) {
                                    BitmapFactory.decodeFile(imageUri)
                                } else {
                                    null
                                }
                            }
                        }
                    }
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap!!.asImageBitmap(),
                            contentDescription = "Captured image",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        ThumbnailPlaceholder()
                    }

                    Spacer(Modifier.width(10.dp))

                    Text(
                        text = att.fileName,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(onClick = onRemoveAttachment) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove attachment",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // ── Input row ─────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            // Image attach button
            IconButton(onClick = onAttachImage) {
                Icon(
                    imageVector = Icons.Default.AttachFile,
                    contentDescription = "Attach image",
                    tint = TextSecondary
                )
            }

            // Text field
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = {
                    Text(
                        text = "Type a message...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary
                    )
                },
                textStyle = TextStyle(
                    color = TextPrimary,
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentBlue.copy(alpha = 0.5f),
                    unfocusedBorderColor = AiBubbleBorder,
                    focusedContainerColor = DarkSurfaceVariant,
                    unfocusedContainerColor = DarkSurfaceVariant,
                    cursorColor = AccentBlue
                ),
                shape = RoundedCornerShape(24.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (text.isNotBlank()) {
                            onSend(text)
                            text = ""
                        }
                    }
                ),
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged { isFocused = it.isFocused },
                maxLines = 4
            )

            Spacer(Modifier.width(4.dp))

            // Send button
            IconButton(onClick = {
                if (text.isNotBlank()) {
                    onSend(text)
                    text = ""
                }
            }) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (text.isNotBlank()) AccentBlue else DarkSurfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = if (text.isNotBlank()) MaterialTheme.colorScheme.onPrimary
                        else TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        if (isFocused && text.isNotEmpty()) {
            Text(
                text = "${text.length}/$maxLength",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(end = 16.dp, bottom = 4.dp)
            )
        }
    }
}

@Composable
private fun ThumbnailPlaceholder() {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Image,
            contentDescription = null,
            tint = AccentBlue,
            modifier = Modifier.size(22.dp)
        )
    }
}
