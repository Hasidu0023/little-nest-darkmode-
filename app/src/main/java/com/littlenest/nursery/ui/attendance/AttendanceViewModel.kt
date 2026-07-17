package com.littlenest.nursery.ui.attendance

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.littlenest.nursery.network.RetrofitClient
import kotlinx.coroutines.launch
import org.json.JSONObject

class AttendanceViewModel : ViewModel() {

    // Status LiveData
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    private val _absenceRecord = MutableLiveData<AbsenceDetail?>()
    val absenceRecord: LiveData<AbsenceDetail?> = _absenceRecord


    /**
     * Calls the API to mark absence for a specific date.
     * @param date The date string in "YYYY-MM-DD" format.
     * @param authToken Auth token for the header.
     * @param apiKey API key for the header.
     */
    fun markAbsence(date: String, authToken: String, apiKey: String) {
        if (_isLoading.value == true) return

        _isLoading.value = true
        _message.value = "Marking absence for $date..."

        viewModelScope.launch {
            try {
                val requestBody = AbsenceRequest(date = date)

                val response = RetrofitClient.instance.markAbsence(
                    auth = authToken,
                    apiKey = apiKey,
                    body = requestBody
                )

                if (response.isSuccessful) {
                    val absenceResponse = response.body()
                    if (absenceResponse != null) {
                        _absenceRecord.value = absenceResponse.absence
                        // Success message from the API response body
                        _message.value = absenceResponse.message
                    } else {
                        _message.value = "Marked absence, but the server response was empty."
                    }
                } else {
                    val errorBodyString = response.errorBody()?.string()
                    var userErrorMessage: String = "Failed to mark absence. Status code: ${response.code()}"

                    if (!errorBodyString.isNullOrEmpty()) {
                        try {
                            // Attempt to parse the error body JSON: {"message": "Your actual error here"}
                            val jsonObject = JSONObject(errorBodyString)
                            if (jsonObject.has("message")) {
                                // Extract only the value of the "message" key
                                userErrorMessage = jsonObject.getString("message")
                            } else {
                                // Fallback: if JSON is valid but key is missing, show the raw body
                                userErrorMessage = errorBodyString
                            }
                        } catch (e: Exception) {
                            // Network or parsing failed, display the raw string if it's not JSON
                            // or keep the default generic message.
                            userErrorMessage = errorBodyString
                        }
                    }
                    // This will now display the extracted message, e.g., "This date is already marked."
                    _message.value = userErrorMessage
                }
            } catch (e: Exception) {
                // For a network-level failure (e.g., no internet connection)
                _message.value = "Network error: Could not connect to the server."
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Clears the message LiveData after consumption.
     */
    fun clearMessage() {
        _message.value = ""
    }
}