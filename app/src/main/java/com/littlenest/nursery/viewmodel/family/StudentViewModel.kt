package com.littlenest.nursery.viewmodel.family

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import com.littlenest.nursery.ui.student.Student
import com.littlenest.nursery.network.RetrofitClient
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class StudentViewModel : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _updateResult = MutableLiveData<Boolean>()
    val updateResult: LiveData<Boolean> get() = _updateResult

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    fun updateStudentWithImage(
        student: Student,
        imageUri: Uri?,
        token: String,
        apiKey: String,
        context: Context,
        studentId: Int
    ) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            try {
                val contentResolver = context.contentResolver
                var imagePart: MultipartBody.Part? = null

                // Convert Uri → File
                imageUri?.let {
                    val file = uriToFile(it, context)
                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                    imagePart = MultipartBody.Part.createFormData("profilePicture", file.name, requestFile)
                }

                // Prepare RequestBody for text fields
                fun toRequestBody(value: String?) =
                    value?.toRequestBody("text/plain".toMediaTypeOrNull())

                val response = RetrofitClient.instance.updateStudentMultipart(
                    token = token,
                    apiKey = apiKey,
                    studentId = studentId,
                    profilePicture = imagePart,
                    fullName = toRequestBody(student.extraData.fullName),
                    nickname = toRequestBody(student.extraData.nickname),
                    address = toRequestBody(student.extraData.address),
                    city = toRequestBody(student.extraData.city),
                    nativeLanguage = toRequestBody(student.extraData.nativeLanguage),
                    allergies = toRequestBody(student.extraData.allergies),
                    comment = toRequestBody(student.extraData.comment),
                    dateOfBirth = toRequestBody(student.extraData.dateOfBirth),
                    dropOffTime = toRequestBody(student.extraData.dropOffTime),
                    pickupTime = toRequestBody(student.extraData.pickupTime),
                    photoConsent = toRequestBody(student.extraData.photoConsent.toString())
                )

                if (response.isSuccessful) {
                    _updateResult.postValue(true)
                } else {
                    Log.e("UpdateError", "Error code: ${response.code()} - ${response.message()}")
                    _updateResult.postValue(false)
                }
            } catch (e: Exception) {
                Log.e("UpdateError", "Exception: ${e.message}", e)
                _updateResult.postValue(false)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    private fun uriToFile(uri: Uri, context: Context): File {
        val contentResolver = context.contentResolver
        val file = File.createTempFile("upload", ".jpg", context.cacheDir)

        contentResolver.openInputStream(uri)?.use { inputStream ->
            file.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        return file
    }
}
