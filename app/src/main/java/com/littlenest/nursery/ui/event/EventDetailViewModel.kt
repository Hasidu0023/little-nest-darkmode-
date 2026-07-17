package com.littlenest.nursery.ui.event

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.littlenest.nursery.network.RetrofitClient
import kotlinx.coroutines.launch

class EventDetailViewModel : ViewModel() {

    private val _response = MutableLiveData<EventResponseRespond>()
    val response: LiveData<EventResponseRespond> get() = _response

    private val _summary = MutableLiveData<EventSummaryResponse>()
    val summary: LiveData<EventSummaryResponse> = _summary

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    fun respondToEvent(token: String?, apiKey: String, eventId: Int, status: String) {
        if (token.isNullOrEmpty()) {
            _error.postValue("Missing token")
            return
        }

        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.respondToEvent(
                    "Bearer $token",
                    apiKey,
                    eventId,
                    mapOf("status" to status)
                )
                _response.postValue(response)
            } catch (e: Exception) {
                _error.postValue("Error: ${e.localizedMessage}")
            }
        }
    }


    fun loadSummary(eventId: Int, token: String, apiKey: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getEventSummary(eventId, token, apiKey)
                _summary.postValue(response)
            } catch (e: Exception) {
                _error.postValue(e.message)
            }
        }
    }
}