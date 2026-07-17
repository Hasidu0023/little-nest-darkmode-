package com.littlenest.nursery.ui.curriculumNew

import androidx.lifecycle.*
import kotlinx.coroutines.launch
import com.littlenest.nursery.network.RetrofitClient
import android.util.Log

class StandardViewModel : ViewModel() {

    private val _standards = MutableLiveData<List<Standard>>()
    val standards: LiveData<List<Standard>> = _standards

    val loading = MutableLiveData<Boolean>()
    val error = MutableLiveData<String>()

    fun fetchStandards(token: String, apiKey: String) {
        viewModelScope.launch {
            try {
                loading.postValue(true)

                val response = RetrofitClient.instance.getStandards(token, apiKey)

                Log.d("API_DEBUG", "Code: ${response.code()}")

                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d("API_DEBUG", "Body: $body")

                    _standards.postValue(body?.data ?: emptyList())
                } else {
                    val errorMsg = response.errorBody()?.string()
                    Log.e("API_ERROR", "ErrorBody: $errorMsg")

                    error.postValue("Error: ${response.code()}")
                }

            } catch (e: Exception) {
                Log.e("API_ERROR", "Exception: ${e.message}", e)
                error.postValue(e.message ?: "Failed to load standards")
            } finally {
                loading.postValue(false)
            }
        }
    }
}