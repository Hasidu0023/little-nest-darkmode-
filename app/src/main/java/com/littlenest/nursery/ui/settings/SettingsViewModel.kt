package com.littlenest.nursery.ui.settings

import android.net.Uri
import androidx.lifecycle.*
import com.littlenest.nursery.network.RetrofitClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import kotlinx.coroutines.launch
import java.io.File


class SettingsViewModel : ViewModel() {

    // Nursery LiveData
    private val _nursery = MutableLiveData<Nursery?>()
    val nursery: LiveData<Nursery?> get() = _nursery

    // Success/Error messages
    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> get() = _successMessage

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    // Fetch nursery details
    fun fetchNursery(token: String, apiKey: String, nurseryId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getNurseryById(token, apiKey, nurseryId)
                _nursery.postValue(response.nursery)
            } catch (e: Exception) {
                e.printStackTrace()
                _nursery.postValue(null)
                _error.postValue("Failed to fetch nursery details")
            }
        }
    }

    // Update nursery
//    fun updateNursery(
//        nurseryId: Int,
//        name: String,
//        description: String,
//        email: String,
//        address: String,
//        language: String,
//        imageUri: Uri?,
//        token: String,
//        apiKey: String
//    ) {
//        viewModelScope.launch {
//            try {
//                val imagePart: MultipartBody.Part? = imageUri?.let { uri ->
//                    val file = File(uri.path ?: "")
//                    MultipartBody.Part.createFormData(
//                        "image",
//                        file.name,
//                        file.asRequestBody("image/*".toMediaTypeOrNull())
//                    )
//                }
//
//                val response = RetrofitClient.instance.updateNursery(
//                    nurseryId = nurseryId,
//                    token = token,
//                    apiKey = apiKey,
//                    name = name.toRequestBody(),
//                    description = description.toRequestBody(),
//                    nurseryEmail = email.toRequestBody(),
//                    address = address.toRequestBody(),
//                    language = language.toRequestBody(),
//                    image = imagePart
//                )
//
//                _successMessage.postValue(response.nursery.name + " updated successfully")
//
//            } catch (e: Exception) {
//                e.printStackTrace()
//                _error.postValue("Error: ${e.localizedMessage}")
//            }
//        }
//    }



    fun updateNursery(
        nurseryId: Int,
        request: UpdateNurseryRequest,
        token: String,
        apiKey: String
    ) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.updateNursery(
                    nurseryId,
                    "Bearer $token",
                    apiKey,
                    request
                )

                _successMessage.postValue("${response.nursery.name} updated successfully")

            } catch (e: Exception) {
                _error.postValue(e.localizedMessage)
            }
        }
    }

    fun updateNurseryWithImage(
        nurseryId: Int,
        name: String,
        description: String,
        email: String,
        address: String,
        language: String,
        imagePart: MultipartBody.Part?,
        token: String,
        apiKey: String
    ) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.updateNurseryWithImage(
                    nurseryId = nurseryId,
                    token = "Bearer $token",
                    apiKey = apiKey,
                    name = name.toRequestBody(),
                    description = description.toRequestBody(),
                    email = email.toRequestBody(),
                    address = address.toRequestBody(),
                    language = language.toRequestBody(),
                    image = imagePart
                )

                _successMessage.postValue("${response.nursery.name} updated successfully")

            } catch (e: Exception) {
                _error.postValue(e.localizedMessage)
            }
        }
    }

}
