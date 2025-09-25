package com.raouf_developer.chatappwithwebssocketcmp.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raouf_developer.chatappwithwebssocketcmp.model.ChatMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.ExperimentalTime


@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = koinViewModel<ChatViewModel>(),
) {
    var serverAddress by remember { mutableStateOf("192.168.1.101") }
    var username by remember { mutableStateOf("") }
    var messageText by remember { mutableStateOf("") }
    val connectionStatus by viewModel.connectionStatus.collectAsState()
    val currentUsername by viewModel.username.collectAsState()
    val isConnecting by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding() ,
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        ) {
            AnimatedContent(
                targetState = connectionStatus,
                transitionSpec = {
                    if (targetState) {
                        slideInHorizontally(
                            initialOffsetX = { it },
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                        ) + fadeIn(animationSpec = tween(300)) togetherWith
                                slideOutHorizontally(
                                    targetOffsetX = { -it },
                                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                                ) + fadeOut(animationSpec = tween(300))
                    } else {
                        slideInHorizontally(
                            initialOffsetX = { -it },
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                        ) + fadeIn(animationSpec = tween(300)) togetherWith
                                slideOutHorizontally(
                                    targetOffsetX = { it },
                                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                                ) + fadeOut(animationSpec = tween(300))
                    }
                },
                label = "connection_transition"
            ) { connected ->
                if (!connected) {
                    ConnectionScreen(
                        serverAddress = serverAddress,
                        username = username,
                        isConnecting = isConnecting,
                        onServerAddressChange = { serverAddress = it },
                        onUsernameChange = { username = it },
                        onConnect = { viewModel.connect(serverAddress, username) }
                    )
                } else {
                    ChatInterface(
                        currentUsername = currentUsername,
                        messages = viewModel.messages.collectAsState().value,
                        messageText = messageText,
                        onMessageChange = { messageText = it },
                        onSendMessage = {
                            viewModel.sendMessage(messageText)
                            messageText = ""
                        },
                        onDisconnect = viewModel::disconnect
                    )
                }
            }

            viewModel.errorMessage.collectAsState().value?.let { error ->
                ErrorSnackbar(
                    message = error,
                    onDismiss = viewModel::clearError
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionScreen(
    serverAddress: String,
    username: String,
    isConnecting: Boolean,
    onServerAddressChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onConnect: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 48.dp)
        ) {
            Surface(
                modifier = Modifier
                    .size(100.dp)
                    .shadow(
                        12.dp,
                        CircleShape,
                        spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    ),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = null,
                        modifier = Modifier.size(50.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Chat App CMP",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Real-time chat using WebSockets with Spring Boot.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(20.dp),
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                OutlinedTextField(
                    value = serverAddress,
                    onValueChange = onServerAddressChange,
                    label = { Text("Server IP Address") },
                    leadingIcon = {
                        Icon(Icons.Default.Dns, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isConnecting,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                    )
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = onUsernameChange,
                    label = { Text("Your Username") },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Go
                    ),
                    keyboardActions = KeyboardActions(
                        onGo = {
                            if (serverAddress.isNotBlank() && username.isNotBlank()) {
                                onConnect()
                            }
                        }
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isConnecting,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                    )
                )

                Button(
                    onClick = {
                        onConnect()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = serverAddress.isNotBlank() && username.isNotBlank() && !isConnecting,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 2.dp,
                        pressedElevation = 0.dp
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.0f),
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.0f)
                                    )
                                ),
                                shape = RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isConnecting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Connecting...", color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "Connect",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatInterface(
    currentUsername: String,
    messages: List<ChatMessage>,
    messageText: String,
    onMessageChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onDisconnect: () -> Unit,
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            scope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.03f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 4.dp,
            tonalElevation = 2.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(42.dp),
                        shadowElevation = 2.dp
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                            MaterialTheme.colorScheme.primaryContainer
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Chat,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            "Chat Room",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            " ${messages.size} messages",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onDisconnect,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f)
                    ),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Disconnect",
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Box(
            modifier = Modifier.weight(1f)
        ) {
            if (messages.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No messages yet!",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        "Be the first to send a message.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 12.dp,
                        bottom = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    reverseLayout = true
                ) {
                    items(messages.reversed()) { message ->
                        MessageBubble(
                            message = message,
                            isOwnMessage = isMyMessage(message, currentUsername)
                        )
                    }
                }
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 8.dp),
            tonalElevation = 4.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = onMessageChange,
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    placeholder = {
                        Text(
                            "Type your message...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    minLines = 1,
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(
                        imeAction = if (messageText.contains('\n')) ImeAction.Default else ImeAction.Send,
                        capitalization = KeyboardCapitalization.Sentences
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (messageText.trim().isNotBlank()) {
                                onSendMessage()
                                scope.launch {
                                    delay(50)
                                    listState.animateScrollToItem(0)
                                }
                            }
                        }
                    ),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        cursorColor = MaterialTheme.colorScheme.primary,
                    ),
                    leadingIcon = {
                        IconButton(
                            onClick = { /*  */ },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Attach file",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.width(12.dp))

                AnimatedContent(
                    targetState = messageText.trim().isNotBlank(),
                    transitionSpec = {
                        scaleIn(tween(150)) togetherWith scaleOut(tween(150))
                    },
                    label = "send_button"
                ) { hasText ->
                    FloatingActionButton(
                        onClick = {
                            if (messageText.trim().isNotBlank()) {
                                onSendMessage()
                                scope.launch {
                                    delay(50)
                                    listState.animateScrollToItem(0)
                                }
                            }
                        },
                        modifier = Modifier.size(52.dp),
                        containerColor = if (hasText) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                        },
                        elevation = FloatingActionButtonDefaults.elevation(
                            defaultElevation = if (hasText) 6.dp else 2.dp,
                            pressedElevation = if (hasText) 8.dp else 4.dp
                        ),
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = if (hasText) Icons.AutoMirrored.Filled.Send else Icons.Default.Mic,
                            contentDescription = if (hasText) "Send message" else "Record voice message",
                            tint = if (hasText) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
fun MessageBubble(
    message: ChatMessage,
    isOwnMessage: Boolean,
) {
    val bubbleColor = if (isOwnMessage) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = if (isOwnMessage) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }

    fun formatTime(timestamp: Long): String {
        val instant = Instant.fromEpochMilliseconds(timestamp)
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val hour = localDateTime.hour.toString().padStart(2, '0')
        val minute = localDateTime.minute.toString().padStart(2, '0')
        return "$hour:$minute"
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(
            initialOffsetX = { if (isOwnMessage) it else -it },
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        ) + fadeIn(animationSpec = tween(300))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = if (isOwnMessage) 48.dp else 0.dp,
                    end = if (isOwnMessage) 0.dp else 48.dp
                ),
            horizontalAlignment = if (isOwnMessage) Alignment.End else Alignment.Start
        ) {

            AnimatedVisibility(
                visible = !isOwnMessage,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 4.dp, start = 8.dp)
                ) {

                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.size(24.dp),
                        shadowElevation = 1.dp
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                                            MaterialTheme.colorScheme.secondaryContainer
                                        )
                                    )
                                )
                        ) {
                            Text(
                                text = message.sender.take(1).uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = message.sender,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = if (isOwnMessage) Arrangement.End else Arrangement.Start
            ) {
                Surface(
                    shape = RoundedCornerShape(
                        topStart = if (isOwnMessage) 18.dp else 4.dp,
                        topEnd = 18.dp,
                        bottomStart = 18.dp,
                        bottomEnd = if (isOwnMessage) 4.dp else 18.dp,
                    ),
                    color = bubbleColor,
                    shadowElevation = if (isOwnMessage) 2.dp else 1.dp,
                    modifier = Modifier
                        .widthIn(min = 48.dp, max = 280.dp)
                        .animateContentSize()
                ) {
                    Column(
                        modifier = Modifier.padding(
                            horizontal = 16.dp,
                            vertical = 12.dp
                        )
                    ) {
                        Text(
                            text = message.content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor,
                            lineHeight = 20.sp
                        )

                        if (isOwnMessage) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = formatTime(message.timestamp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = textColor.copy(alpha = 0.8f)
                                )

                                Spacer(modifier = Modifier.width(6.dp))

                                Icon(
                                    imageVector = Icons.Default.DoneAll,
                                    contentDescription = "Message read",
                                    modifier = Modifier.size(14.dp),
                                    tint = textColor.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }

            if (!isOwnMessage) {
                Text(
                    text = formatTime(message.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                    modifier = Modifier.padding(
                        start = 16.dp,
                        top = 4.dp
                    )
                )
            }
        }
    }
}

@Composable
fun ErrorSnackbar(
    message: String,
    onDismiss: () -> Unit,
) {
    LaunchedEffect(message) {
        delay(5000)
        onDismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.errorContainer,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = message,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

fun isMyMessage(message: ChatMessage, currentUsername: String): Boolean {
    return message.sender == currentUsername
}