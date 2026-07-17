package com.littlenest.nursery.ui.journal

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.littlenest.nursery.network.RetrofitClient
import kotlinx.coroutines.launch
import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import org.json.JSONArray
import okhttp3.RequestBody

class JournalViewModel : ViewModel() {

    private val _journalPosts = MutableLiveData<List<JournalPost>>()
    val journalPosts: LiveData<List<JournalPost>> = _journalPosts

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _success = MutableLiveData<String>()
    val success: LiveData<String> = _success

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    val selectedPost = MutableLiveData<JournalPost>()

    // Calls Retrofit -> fetches data from API -> updates LiveData
    fun loadJournalPosts(token: String?, apiKey: String) {
        if (token.isNullOrEmpty()) {
            Log.e("JournalViewModel1", "Token is null or empty, skipping API call")
            return
        }

        viewModelScope.launch {
            try {
                Log.d("JournalViewModel2 response", "03")
                val response = RetrofitClient.instance.getTaggedPosts(
                    "Bearer $token",
                    apiKey
                )
                Log.d("JournalViewModel2 response", "$response")
                if (response.isSuccessful) {
                    val body = response.body()
                    _journalPosts.postValue(response.body() ?: emptyList())
                } else {
                    // log error or handle response error
                    Log.e("JournalViewModel4", "API error: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("JournalViewModel5", "Exception: ${e.message}", e)
            }
        }
    }



    fun loadJournalPostsByTeacherGroup(token: String?, apiKey: String) {
        if (token.isNullOrEmpty()) {
            Log.e("JournalViewModel1", "Token is null or empty, skipping API call")
            return
        }
        viewModelScope.launch {
            try {
                Log.d("JournalViewModelQ", "03")
                val response = RetrofitClient.instance.getPostsByTeacherGroup(
                    "Bearer $token",
                    apiKey
                )
                Log.d("JournalViewModelQ response", "$response")
                if (response.isSuccessful) {
                    val body = response.body()
                    _journalPosts.postValue(response.body() ?: emptyList())
                } else {
                    // log error or handle response error
                    Log.e("JournalViewModelQ", "API error: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("JournalViewModelQ", "Exception: ${e.message}", e)
            }
        }
    }

    fun createPost(
        context: Context,
        token: String,
        apiKey: String,
        description: String,
        groupId: Int,
        curriculumId: Int?,
        subTopicId: Int?,                 // ✅ NEW
        selectedActivitiesIds: List<Int>, // ✅ NEW
        taggedStudentIds: List<Int>,
        images: List<Uri>,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // ✅ Tagged students JSON
                val taggedJson = JSONArray(taggedStudentIds).toString()

                // ✅ Activities JSON
                val activitiesJson = JSONArray(selectedActivitiesIds).toString()

                // ✅ Convert images to Multipart
                val imageParts = images.mapIndexed { index, uri ->
                    val file = File(context.cacheDir, "image_$index.jpg")

                    context.contentResolver.openInputStream(uri)?.use { input ->
                        file.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }

                    MultipartBody.Part.createFormData(
                        "images",
                        file.name,
                        file.asRequestBody("image/*".toMediaTypeOrNull())
                    )
                }

                // ✅ Request bodies
                val descriptionBody =
                    description.toRequestBody("text/plain".toMediaTypeOrNull())

                val groupIdBody =
                    groupId.toString().toRequestBody("text/plain".toMediaTypeOrNull())

                val curriculumIdBody =
                    curriculumId?.toString()
                        ?.toRequestBody("text/plain".toMediaTypeOrNull())

                val subTopicIdBody =
                    subTopicId?.toString()
                        ?.toRequestBody("text/plain".toMediaTypeOrNull())

                val activitiesBody =
                    activitiesJson.toRequestBody("application/json".toMediaTypeOrNull())

                val taggedStudentsBody =
                    taggedJson.toRequestBody("application/json".toMediaTypeOrNull())

                // ✅ API call
                val response = RetrofitClient.instance.createPost(
                    token = "Bearer $token",
                    apiKey = apiKey,

                    description = descriptionBody,
                    groupId = groupIdBody,
                    curriculumId = curriculumIdBody,

                    subTopicId = subTopicIdBody,                 // ✅ NEW
                    selectedActivities = activitiesBody,         // ✅ NEW

                    taggedStudents = taggedStudentsBody,
                    images = imageParts
                )

                if (response.isSuccessful) {
                    _success.postValue("Post created")
                } else {
                    _error.postValue("Failed: ${response.code()}")
                    onError("Failed: ${response.code()}")
                }

            } catch (e: Exception) {
                _error.postValue("Error: ${e.message}")
                onError(e.message ?: "Unexpected error")
            }
        }
    }

    fun updatePost(
        context: Context,
        postId: Int,
        token: String,
        apiKey: String,
        description: String,
        groupId: Int,
        curriculumId: Int?,
        subTopicId: Int?,                 // ✅ NEW
        selectedActivitiesIds: List<Int>, // ✅ NEW
        taggedStudentIds: List<Int>,
        newImages: List<Uri>,          // ✅ ONLY new images
        existingImages: List<String>,  // ✅ already saved images
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // ✅ JSON conversions
                val taggedJson = JSONArray(taggedStudentIds).toString()
                val activitiesJson = JSONArray(selectedActivitiesIds).toString()
                val existingImagesJson = JSONArray(existingImages).toString()

                // ✅ Convert images
                // ✅ ONLY convert NEW images
                val imageParts = newImages.mapIndexed { index, uri ->
                    val file = File(context.cacheDir, "image_$index.jpg")

                    context.contentResolver.openInputStream(uri)?.use { input ->
                        file.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }

                    MultipartBody.Part.createFormData(
                        "images",
                        file.name,
                        file.asRequestBody("image/*".toMediaTypeOrNull())
                    )
                }


                // ✅ Request bodies
                val descriptionBody =
                    description.toRequestBody("text/plain".toMediaTypeOrNull())

                val groupIdBody =
                    groupId.toString().toRequestBody("text/plain".toMediaTypeOrNull())

                val curriculumIdBody =
                    curriculumId?.toString()
                        ?.toRequestBody("text/plain".toMediaTypeOrNull())

                val subTopicIdBody =
                    subTopicId?.toString()
                        ?.toRequestBody("text/plain".toMediaTypeOrNull())

                val activitiesBody =
                    activitiesJson.toRequestBody("application/json".toMediaTypeOrNull())

                val taggedStudentsBody =
                    taggedJson.toRequestBody("application/json".toMediaTypeOrNull())

                val existingImagesBody =
                    existingImagesJson.toRequestBody("application/json".toMediaTypeOrNull())

                // ✅ API CALL (UPDATED)
                val response = RetrofitClient.instance.updatePost(
                    postId = postId,
                    token = "Bearer $token",
                    apiKey = apiKey,

                    description = descriptionBody,
                    groupId = groupIdBody,
                    curriculumId = curriculumIdBody,

                    subTopicId = subTopicIdBody,           // ✅ NEW
                    selectedActivities = activitiesBody,   // ✅ NEW

                    taggedStudents = taggedStudentsBody,

                    existingImages = existingImagesBody, // ✅ IMPORTANT
                    images = imageParts                 // ✅ ONLY NEW FILES
                )

                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    Log.d("editjournalerror", "$error")
                    onError("Failed: ${response.code()}")
                }

            } catch (e: Exception) {
                Log.d("editjournalerror2", "$e.message")
                onError(e.message ?: "Unknown error")
            }
        }
    }

    fun loadPostById(token: String?, apiKey: String, postId: Int) {
        if (token.isNullOrEmpty()) return

        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getPostById(
                    "Bearer $token",
                    apiKey,
                    postId
                )
                if (response.isSuccessful) {
                    selectedPost.postValue(response.body())
                }
            } catch (e: Exception) {
                Log.e("EditPost", e.message ?: "error")
            }
        }
    }


    fun deletePost(
        token: String?,
        apiKey: String,
        postId: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (token.isNullOrEmpty()) {
            onError("Invalid token")
            return
        }

        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.deletePost(
                    token = "Bearer $token",
                    apiKey = apiKey,
                    postId = postId
                )

                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    onError("Delete failed: ${response.code()}")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Unknown error")
            }
        }
    }



    //helper functions
    fun String.toRequestBody(): RequestBody =
        RequestBody.create("text/plain".toMediaTypeOrNull(), this)

    fun Int.toRequestBody(): RequestBody = this.toString().toRequestBody()

    fun List<String>.toStringListJson(): RequestBody {
        val json = JSONArray(this).toString()
        return json.toRequestBody("application/json".toMediaTypeOrNull())
    }

    fun List<Int>.toIntListJson(): RequestBody {
        val json = JSONArray(this).toString()
        return json.toRequestBody("application/json".toMediaTypeOrNull())
    }

    fun Uri.toMultipart(context: Context, partName: String): MultipartBody.Part {
        val file = File(context.cacheDir, "image_${System.currentTimeMillis()}.jpg")

        context.contentResolver.openInputStream(this)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        }

        val reqFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(partName, file.name, reqFile)
    }

}