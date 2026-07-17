package com.littlenest.nursery.ui.event

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.littlenest.nursery.network.RetrofitClient
import kotlinx.coroutines.launch
import android.util.Log

class EventViewModel : ViewModel() {

    private val _events = MutableLiveData<List<Event>>()
    val events: LiveData<List<Event>> get() = _events

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _successMessage = MutableLiveData<String>()
    val successMessage: LiveData<String> get() = _successMessage

    /**
     * Loads events based on the specified status ("future" or "past")
     * by querying the backend API.
     */
    fun loadEvents(token: String?, apiKey: String, status: String) {
        if (token.isNullOrEmpty()) {
            Log.w("EventViewModel", "Token is null or empty, cannot load events.")
            return
        }

        viewModelScope.launch {
            try {
                // Directly call Retrofit here (no repository layer)
                val response = RetrofitClient.instance.getEvents(
                    "Bearer $token",
                    apiKey,
                    status // Pass the required status (future/past) events parameter for backend filtering
                )
                Log.d("EventViewModel", "API Response: ${response} events loaded for status: $status")
                _events.postValue(response)
            } catch (e: Exception) {
                Log.e("EventViewModel", "Error fetching events for status '$status'", e)
                e.printStackTrace()
                _events.postValue(emptyList())
            }
        }
    }


    /**
     * Loads events based on the specified status ("future" or "past")
     * by querying the backend API.
     */
    fun loadEventsforTeacher(token: String?, apiKey: String, status: String) {
        if (token.isNullOrEmpty()) {
            Log.w("EventViewModel", "Token is null or empty, cannot load events.")
            return
        }

        viewModelScope.launch {
            try {
                // Directly call Retrofit here (no repository layer)
                val response = RetrofitClient.instance.getEventsForTeacher(
                    "Bearer $token",
                    apiKey,
                    status // Pass the required status (future/past) events parameter for backend filtering
                )
                Log.d("EventViewModel", "API Response: ${response} events loaded for status: $status")
                _events.postValue(response)
            } catch (e: Exception) {
                Log.e("EventViewModel", "Error fetching events for status '$status'", e)
                e.printStackTrace()
                _events.postValue(emptyList())
            }
        }
    }



    /**
     * Admin - Fetch all Events
     * Loads events based on the specified status ("future" or "past")
     * by querying the backend API.
     */
    fun loadAdminEvents(token: String?, apiKey: String, status: String) {
        if (token.isNullOrEmpty()) {
            Log.w("EventViewModel", "Token is null or empty, cannot load events.")
            return
        }

        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getAdminEvents(
                    "Bearer $token",
                    apiKey,
                    status // Pass the required status (future/past) events parameter for backend filtering
                )
                Log.d("EventViewModel", "API Response: ${response} events loaded for status: $status")
                _events.postValue(response)
            } catch (e: Exception) {
                Log.e("EventViewModel", "Error fetching events for status '$status'", e)
                e.printStackTrace()
                _events.postValue(emptyList())
            }
        }
    }

    fun updateEvent(eventId: Int, request: EventCreateRequest, token: String, apiKey: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.updateEvent(token, apiKey, eventId, request)
                if (response.isSuccessful) {
                    _events.value = _events.value?.filter { it.id != eventId }
                } else {
                    _error.postValue("Failed to update event: ${response.code()}")
                }
            } catch (e: Exception) {
                _error.postValue(e.message)
            }
        }
    }

    fun deleteEvent(eventId: Int, token: String, apiKey: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.deleteEvent("Bearer $token" , apiKey, eventId)
                if (response.isSuccessful) {
                    _events.value = _events.value?.filter { it.id != eventId }
                }else{
                    _error.postValue("Failed to delete event: ${response.code()}")
                }
            } catch (e: Exception) {
                _error.postValue(e.message)
            }
        }
    }

    fun createEvent(
        token: String,
        apiKey: String,
        request: EventCreateRequest,
        status: String // "future" or "past" so list refreshes
    ) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.createEvent(
                    "Bearer $token",
                    apiKey,
                    request
                )
                if (response.isSuccessful) {
                    _successMessage.postValue("Event created successfully")
                    loadAdminEvents(token, apiKey, status)
                } else {
                    _error.postValue("Failed: ${response.message()}")
                }

            } catch (e: Exception) {
                _error.postValue(e.message)
            }
        }
    }

}
