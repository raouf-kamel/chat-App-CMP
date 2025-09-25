package com.raouf_developer.chatappwithwebssocketcmp.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.raouf_developer.chatappwithwebssocketcmp.model.ChatMessage
import com.raouf_developer.chatappwithwebssocketcmp.network.ChatService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChatViewModel(private val chatService: ChatService) : ViewModel() {
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _connectionStatus = MutableStateFlow(false)
    val connectionStatus: StateFlow<Boolean> = _connectionStatus

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username

    init {
        viewModelScope.launch {
            chatService.incomingMessages.collect { message ->
                _messages.update { it + message }
            }
        }
    }
    fun connect(serverAddress: String, username: String) {
        viewModelScope.launch {
            try {
                _username.value = username
                _connectionStatus.value = true

                while (_connectionStatus.value) {
                    try {
                        chatService.connect(serverAddress, username)
                        break
                    } catch (e: Exception) {
                        _errorMessage.value = "Retrying connection..."
                        delay(2000)
                    }
                }

            } catch (e: Exception) {
                _errorMessage.value = "Connection failed: ${e.message}"
                _connectionStatus.value = false
            }
        }
    }

    fun connectOld(serverAddress: String, username: String) {
        viewModelScope.launch {
            try {
                _username.value = username
                _connectionStatus.value = true
                chatService.connect(serverAddress, username)
            } catch (e: Exception) {
                _errorMessage.value = "Connection failed: ${e.message}"
                _connectionStatus.value = false
            }
        }
    }
    fun sendMessage(content: String) {
        viewModelScope.launch {
            try {
                val chatMessage = ChatMessage(
                    type = "CHAT",
                    content = content,
                    sender = _username.value
                )
               // _messages.update { it + chatMessage } //to ui
                chatService.sendMessage(chatMessage) // to server
            } catch (e: Exception) {
                _errorMessage.value = "Failed to send message: ${e.message}"
            }
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            chatService.disconnect(_username.value)
            _connectionStatus.value = false
            _messages.value = emptyList()
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}

