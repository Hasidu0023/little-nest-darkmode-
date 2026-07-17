package com.littlenest.nursery.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import com.littlenest.nursery.network.RetrofitClient

class UserListViewModel : ViewModel() {

    private val _users = MutableLiveData<List<UserItem>>()
    val users: LiveData<List<UserItem>> = _users

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadUsers(token: String, apiKey: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getChatUsers("Bearer $token", apiKey)
                _users.value = response
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}
