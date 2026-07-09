package com.smartphoneaichat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.smartphoneaichat.domain.model.AVAILABLE_MODELS
import com.smartphoneaichat.domain.model.ChatRole
import com.smartphoneaichat.ui.components.ChatBubble
import com.smartphoneaichat.ui.components.ChatInput
import com.smartphoneaichat.ui.components.ModelLoaderDialog
import com.smartphoneaichat.ui.components.ModelSelectorDialog
import com.smartphoneaichat.ui.components.NotificationHost
import com.smartphoneaichat.ui.components.SidebarContent
import com.smartphoneaichat.ui.theme.AccentBlue
import com.smartphoneaichat.ui.theme.AccentGreen
import com.smartphoneaichat.ui.theme.AccentOrange
import com.smartphoneaichat.ui.theme.AccentRed
import com.smartphoneaichat.ui.theme.DarkBackground
import com.smartphoneaichat.ui.theme.DarkSurfaceVariant
import com.smartphoneaichat.ui.theme.TextPrimary
import com.smartphoneaichat.ui.theme.TextSecondary
import com.smartphoneaichat.presentation.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: ChatViewModel) {
    val state by viewModel.state.collectAsState()

    val activeConversation = state.activeConversation
    val messages = activeConversation?.messages ?: emptyList()
    val conversationTitle = activeConversation?.title ?: "AI Chat"

    val listState = rememberLazyListState()
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    LaunchedEffect(state.isSidebarOpen) {
        if (state.isSidebarOpen) drawerState.open() else drawerState.close()
    }

    LaunchedEffect(drawerState.currentValue) {
        if ((drawerState.currentValue == DrawerValue.Closed) && state.isSidebarOpen) {
            viewModel.closeSidebar()
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    NotificationHost(
        notificationManager = viewModel.notifications,
        snackbarHostState = snackbarHostState,
    ) {
        Scaffold(
            contentWindowInsets = WindowInsets.systemBars.add(WindowInsets.ime),
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState) { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = Color(0xFF2C2C2E),
                        contentColor = Color.White,
                    )
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    gesturesEnabled = true,
                    drawerContent = {
                        SidebarContent(
                            conversations = state.conversations,
                            activeConversationId = state.activeConversationId,
                            onSelectConversation = { viewModel.selectConversation(it) },
                            onNewConversation = { viewModel.newConversation() },
                            onDeleteConversation = { viewModel.deleteConversation(it) }
                        )
                    },
                    content = {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(DarkBackground)
                        ) {
                            TopAppBar(
                                title = {
                                    Column {
                                        Text(
                                            text = conversationTitle,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = TextPrimary,
                                            maxLines = 1
                                        )
                                        if (state.activeModelDisplayName != null) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .clip(CircleShape)
                                                        .background(AccentGreen)
                                                )
                                                Spacer(Modifier.width(6.dp))
                                                Text(
                                                    text = state.activeModelDisplayName ?: "",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = AccentGreen
                                                )
                                            }
                                        }
                                    }
                                },
                                navigationIcon = {
                                    IconButton(onClick = { viewModel.toggleSidebar() }) {
                                        Icon(
                                            imageVector = Icons.Default.Menu,
                                            contentDescription = "Open sidebar",
                                            tint = TextSecondary
                                        )
                                    }
                                },
                                actions = {
                                    var showModelMenu by remember { mutableStateOf(value = false) }
                                    Box {
                                        IconButton(onClick = { showModelMenu = true }) {
                                            Icon(
                                                imageVector = Icons.Default.Settings,
                                                contentDescription = "Model options",
                                                tint = if (state.activeModelId != null) AccentBlue else TextSecondary
                                            )
                                        }
                                        DropdownMenu(
                                            expanded = showModelMenu,
                                            onDismissRequest = { showModelMenu = false }
                                        ) {
                                            Text(
                                                text = "Models",
                                                style = MaterialTheme.typography.labelLarge,
                                                color = TextSecondary,
                                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                            )
                                            HorizontalDivider(color = DarkSurfaceVariant)
                                            AVAILABLE_MODELS.forEach { model ->
                                                val isActive = model.id == state.activeModelId
                                                val isDownloaded = model.id in state.downloadedModelIds
                                                ModelDropdownItem(
                                                    modelDisplayName = model.displayName,
                                                    isActive = isActive,
                                                    isDownloaded = isDownloaded,
                                                    onDownload = {
                                                        showModelMenu = false
                                                        viewModel.downloadModel(model.id)
                                                    },
                                                    onLoad = {
                                                        showModelMenu = false
                                                        viewModel.loadModel(model.id)
                                                    },
                                                    onUnload = {
                                                        showModelMenu = false
                                                        viewModel.unloadModel()
                                                    }
                                                ) {
                                                    showModelMenu = false
                                                    viewModel.confirmDeleteModel(model.id)
                                                }
                                                if (model != AVAILABLE_MODELS.last()) {
                                                    HorizontalDivider(
                                                        color = DarkSurfaceVariant.copy(alpha = 0.5f),
                                                        modifier = Modifier.padding(horizontal = 16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = DarkBackground
                                )
                            )

                            LazyColumn(
                                state = listState,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                items(messages, key = { it.id.value }) { message ->
                                    ChatBubble(
                                        message = message,
                                        isThinkingExpanded = state.thinkingExpandedIds.contains(message.id),
                                        onToggleThinking = { viewModel.toggleThinkingExpanded(message.id) }
                                    )
                                }
                            }

                            val lastMessage = messages.lastOrNull()
                            val pendingAttachment = if (
                                (lastMessage?.role == ChatRole.USER) &&
                                lastMessage.text.value.isEmpty() &&
                                (lastMessage.attachment != null)
                            ) lastMessage.attachment else null

                            ChatInput(
                                attachment = pendingAttachment,
                                onSend = { text -> viewModel.sendMessage(text) },
                                onAttachImage = { viewModel.attachImage() },
                                onRemoveAttachment = { viewModel.removePendingAttachment() }
                            )
                        }
                    }
                )
            }
        }
    }

    // ── Dialogs ───────────────────────────────────────────────────

    if (state.isModelLoading || state.modelLoadProgress > 0f) {
        ModelLoaderDialog(
            progress = state.modelLoadProgress,
            phase = state.modelLoadPhase,
            onCancel = { viewModel.cancelModelDownload() },
        ) { }
    }

    if (state.showModelSelector) {
        ModelSelectorDialog(
            models = state.modelSelectorModels,
            activeModelId = state.activeModelId,
            onSelect = { modelId -> viewModel.selectModelFromSelector(modelId) },
            onDismiss = { viewModel.dismissModelSelector() }
        )
    }

    if (state.showDeleteConfirmation) {
        val targetId = state.deleteTargetModelId
        val modelInfo = targetId?.let { com.smartphoneaichat.domain.model.modelInfoById(it) }
        AlertDialog(
            onDismissRequest = { viewModel.cancelDeleteModel() },
            containerColor = DarkSurfaceVariant,
            title = {
                Text(
                    text = "Delete Model",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "Remove ${modelInfo?.displayName ?: "model"} from your device. " +
                            "You will need to download it again to use it.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteModel() }) {
                    Text("Delete", color = AccentRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelDeleteModel() }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun ModelDropdownItem(
    modelDisplayName: String,
    isActive: Boolean,
    isDownloaded: Boolean,
    onDownload: () -> Unit,
    onLoad: () -> Unit,
    onUnload: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isActive -> AccentGreen
                                isDownloaded -> AccentBlue
                                else -> TextSecondary.copy(alpha = 0.4f)
                            }
                        )
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = modelDisplayName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = when {
                    isActive -> "Active in memory"
                    isDownloaded -> "Downloaded on device"
                    else -> "Not downloaded"
                },
                style = MaterialTheme.typography.labelSmall,
                color = when {
                    isActive -> AccentGreen
                    isDownloaded -> AccentBlue
                    else -> TextSecondary.copy(alpha = 0.6f)
                }
            )

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                when {
                    isActive -> {
                        ActionChip("Unload", AccentOrange, onUnload)
                    }
                    isDownloaded -> {
                        ActionChip("Load", AccentBlue, onLoad)
                        ActionChip("Delete", AccentRed, onDelete)
                    }
                    else -> {
                        ActionChip("Download", AccentBlue, onDownload)
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionChip(text: String, color: Color, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
        modifier = Modifier.height(28.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}
