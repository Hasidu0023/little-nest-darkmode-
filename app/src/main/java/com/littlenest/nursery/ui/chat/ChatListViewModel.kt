package com.littlenest.nursery.ui.chat

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.littlenest.nursery.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class ChatListViewModel : ViewModel() {

    private val _chatList = MutableLiveData<List<ChatListItem>>()
    val chatList: LiveData<List<ChatListItem>> = _chatList

    fun loadChats(token: String, apiKey: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getChatList(
                    "Bearer $token",
                    apiKey
                )

                if (response.isSuccessful && response.body() != null) {
                    val chats = response.body()!!
                        .sortedByDescending { it.createdAt } // newest first
                        .map {
                            it.copy(createdAt = formatTime(it.createdAt))
                        }
                    _chatList.value = chats
                } else {
                    println("Error fetching chat list: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun formatTime(isoTime: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val date = sdf.parse(isoTime)
            val localFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            if (date != null) localFormat.format(date) else ""
        } catch (e: Exception) {
            isoTime
        }
    }


//    fun markMessageAsRead(messageId: Int, token: String, apiKey: String) {
//        viewModelScope.launch {
//            try {
//                val response = withContext(Dispatchers.IO) {
//                    RetrofitClient.instance.markMessageAsRead(
//                        token = "Bearer $token",
//                        apiKey = apiKey,
//                        messageId = messageId
//                    )
//                }
//
//                Log.d("ChatListVM", "markMessageAsRead() messageId=$messageId response=$response")
//
//                if (response.isSuccessful) {
//                    _chatList.value = _chatList.value?.map { chat ->
//                        Log.d("ChatListVM", "Checking chat messageId=${chat.messageId}")
//                        if (chat.messageId == messageId) chat.copy(isRead = true) else chat
//                    } ?: emptyList() // ensure non-nullable
//                } else {
//                    val errBody = try { response.errorBody()?.string() } catch (e: Exception) { "unknown" }
//                    Log.e("ChatListVM", "Failed to mark as read: code=${response.code()} body=$errBody")
//                }
//            } catch (e: Exception) {
//                Log.e("ChatListVM", "Exception in markMessageAsRead", e)
//            }
//        }
//    }


    fun markMessageAsRead(token: String, apiKey: String, messageId: Int) {
        viewModelScope.launch (Dispatchers.IO) {
            try {
                val response = RetrofitClient.instance.markMessageAsRead("Bearer $token", apiKey, messageId)
                if (response.isSuccessful) {
                    val message = response.body()?.message ?: "No message returned"
                    Log.d("ChatListVM", "Message read status: $message")
                } else {
                    Log.e("ChatListVM", "Failed: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("ChatListVM", "Exception in markMessageAsRead", e)
            }
        }
    }
}
