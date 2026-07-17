package com.littlenest.nursery.viewmodel.family

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.littlenest.nursery.model.PostImage
import com.littlenest.nursery.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class AlbumViewModel : ViewModel() {

    private val _albumImages = MutableLiveData<List<PostImage>>()
    val albumImages: LiveData<List<PostImage>> = _albumImages

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    fun fetchAlbum(authToken: String, apiKey: String) {
        _isLoading.value = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.instance.getAlbumImages(
                    "Bearer $authToken",
                    apiKey
                )

                if (response.isSuccessful) {
                    val images = response.body() ?: emptyList()
                    _albumImages.postValue(images)
                    Log.d("AlbumViewModel", "Loaded ${images.size} album images")
                } else {
                    val error = response.errorBody()?.string() ?: "Unknown API error"
                    _message.postValue("Failed to load album: ${response.code()} - $error")
                    Log.e("AlbumViewModel", "Error: ${response.code()} - $error")
                }

            } catch (e: IOException) {
                _message.postValue("Network error: ${e.localizedMessage}")
                Log.e("AlbumViewModel", "Network error", e)
            } catch (e: HttpException) {
                _message.postValue("HTTP error: ${e.localizedMessage}")
                Log.e("AlbumViewModel", "HTTP error", e)
            } catch (e: Exception) {
                _message.postValue("Unexpected error: ${e.localizedMessage}")
                Log.e("AlbumViewModel", "Unexpected error", e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
}
