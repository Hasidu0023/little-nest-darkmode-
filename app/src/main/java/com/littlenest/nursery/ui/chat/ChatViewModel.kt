package com.littlenest.nursery.ui.chat

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.littlenest.nursery.model.Message
import com.littlenest.nursery.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class ChatViewModel : ViewModel() {

    private val _messages = MutableLiveData<List<Message>>(emptyList())
    val messages: LiveData<List<Message>> get() = _messages

    /**
     * Add a new message locally (from Socket.IO or after sending)
     */
    fun addMessage(message: Message) {
        val currentList = _messages.value?.toMutableList() ?: mutableListOf()
        currentList.add(message)
        _messages.postValue(currentList)
    }

    /**
     * Load one-on-one conversation
     */
    fun loadConversation(partnerId: Int, token: String, apiKey: String) {
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.instance.getConversation(token, apiKey, partnerId)
                }
                if (response.isSuccessful) {
                    val messages = response.body() ?: emptyList()
                    _messages.postValue(messages)
                } else {
                    Log.e("ChatViewModel", "Failed to load conversation: ${response.code()} - ${response.errorBody()?.string()}")
                    _messages.postValue(emptyList())
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error loading conversation", e)
                _messages.postValue(emptyList())
            }
        }
    }

    /**
     * Load group messages
     */
    fun loadGroupMessages(groupId: Int, token: String, apiKey: String) {
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.instance.getGroupMessages(token, apiKey, groupId)
                }

                if (response.isSuccessful) {
                    val messages = response.body() ?: emptyList()
                    _messages.postValue(messages)
                } else {
                    Log.e("ChatViewModel", "Failed to load group messages: ${response.code()} - ${response.errorBody()?.string()}")
                    _messages.postValue(emptyList())
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error loading group messages", e)
                _messages.postValue(emptyList())
            }
        }
    }


    /**
     * Send message to a user or group
     * token and apiKey must be passed from the Fragment
     */
    fun sendMessage(
        token: String,
        apiKey: String,
        receiverId: Int? = null,
        groupId: Int? = null,
        messageText: String? = null,
        filePart: MultipartBody.Part? = null
    ) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.sendMessage(
                    token = "Bearer $token",
                    apiKey = apiKey,
                    receiverId = receiverId,
                    groupId = groupId,
                    messageText = messageText?.toRequestBody("text/plain".toMediaTypeOrNull()),
                    file = filePart
                )

                if (response.isSuccessful) {
                    val sentMessage = response.body()
                    if (sentMessage != null) {
                        SocketManager.emitMessage(sentMessage, receiverId, groupId)
                        _messages.value = _messages.value.orEmpty() + sentMessage
                    } else {
                        Log.e("ChatViewModel", "Message body null after successful response")
                    }
                } else {
                    Log.e("ChatViewModel", "Send message failed: ${response.code()} - ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error sending message", e)
            }
        }
    }
}
