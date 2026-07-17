package com.littlenest.nursery.ui.teacher

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.littlenest.nursery.model.RegisterResponse
import com.littlenest.nursery.network.RetrofitClient
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import okhttp3.MultipartBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import org.json.JSONArray
import okhttp3.RequestBody.Companion.toRequestBody


class TeacherViewModel : ViewModel() {

    private val _teachers = MutableLiveData<List<Teacher>>()
    val teachers: LiveData<List<Teacher>> get() = _teachers

    private val _registrationResult = MutableLiveData<Result<RegisterResponse>>()
    val registrationResult: LiveData<Result<RegisterResponse>> = _registrationResult

    private val _updateResult = MutableLiveData<Result<Unit>>()
    val updateResult: LiveData<Result<Unit>> = _updateResult

    private val _teacherById = MutableLiveData<SingleTeacher>()
    val teacherById: LiveData<SingleTeacher> get() = _teacherById

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    //Register new Teacher
    fun registerTeacher(request: RegisterRequestTeacher, token: String, apiKey: String) {
        _loading.value = true
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.registerTeacher("Bearer $token", apiKey, request)
                if (response.isSuccessful && response.body() != null) {
                    _registrationResult.postValue(Result.success(response.body()!!))
                } else {
                    //val errorBody = response.errorBody()?.string()

                    _registrationResult.postValue(
                        Result.failure(Exception("Registration failed: ${response.code()} - ${response.message()}"))
                    )
                }
            } catch (e: Exception) {
                _registrationResult.postValue(Result.failure(e))
            } finally {
                _loading.postValue(false)
            }
        }
    }

    // update teacher without image update
    fun updateTeacher(teacherId: Int, request: UpdateTeacherRequest, token: String, apiKey: String) {
        _loading.value = true
        //Log.d("updateteacher reqyest", "$request")
        viewModelScope.launch {
            try {
                //Log.d("updateTeacherPayload", "Request: $request")
                val response = RetrofitClient.instance.updateTeacher(teacherId, "Bearer $token", apiKey, request)
                if (response.isSuccessful) {
                    //Log.d("updateteacher response", "$response")
                    //Log.d("updateTeacher", response.body()!!.message)
                    _updateResult.value = Result.success(Unit)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.d("updateteacher error", "$errorBody")
                    // If the backend returns JSON with { message: "..." }, parse it
                    val backendMessage = try {
                        JSONObject(errorBody ?: "").optString("message", response.message())
                    } catch (e: Exception) {
                        response.message()
                    }

                    _updateResult.value = Result.failure(Exception(backendMessage))

                //_updateResult.value = Result.failure(Exception("Failed: ${response.errorBody()}"))
                }
            } catch (e: Exception) {
                _updateResult.value = Result.failure(e)
            } finally {
                _loading.value = false
            }
        }
    }

    fun fetchTeachers(token: String, apiKey: String) {
        _loading.value = true
        _error.value = null

        RetrofitClient.instance.getTeachers("Bearer $token", apiKey)
            .enqueue(object : Callback<TeacherResponse> {
                override fun onResponse(call: Call<TeacherResponse>, response: Response<TeacherResponse>) {
                    _loading.value = false
                    if (response.isSuccessful && response.body() != null) {
                        _teachers.value = response.body()!!.teachers
                    } else {
                        _error.value = "Failed to load teachers"
                    }
                }

                override fun onFailure(call: Call<TeacherResponse>, t: Throwable) {
                    _loading.value = false
                    _error.value = t.localizedMessage ?: "Unknown error"
                }
            })
    }


    fun deleteTeacher(token: String, apiKey: String, teacherId: Int) {
        _loading.value = true
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.deleteTeacher(
                    "Bearer $token",
                    apiKey,
                    teacherId
                )
                if (response.isSuccessful) {
                    // Remove deleted student from list
                    _teachers.value = _teachers.value?.filter { it.teacherId != teacherId }
                } else {
                    _error.postValue("Failed to delete student: ${response.code()}")
                }
            } catch (e: Exception) {
                _error.postValue("Error deleting student: ${e.localizedMessage}")
            } finally {
                _loading.postValue(false)
            }
        }
    }

    fun fetchTeacherById(
        teacherId: Int,
        token: String,
        apiKey: String
    ) {
        _loading.value = true
        _error.value = null

        Log.d("fetchTeacherById", "Fetching teacher id=$teacherId")

        RetrofitClient.instance
            .getTeacherById(
                teacherId,
                "Bearer $token",
                apiKey
            )
            .enqueue(object : Callback<SingleTeacherByIdResponse> {

                override fun onResponse(
                    call: Call<SingleTeacherByIdResponse>,
                    response: Response<SingleTeacherByIdResponse>
                ) {
                    _loading.value = false

                    if (response.isSuccessful && response.body() != null) {
                        _teacherById.value = response.body()!!.teacher
                        Log.d("fetchTeacherById", "Success")
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e("fetchTeacherById", "Error: $errorBody")
                        _error.value = "Failed to load teacher"
                    }
                }

                override fun onFailure(
                    call: Call<SingleTeacherByIdResponse>,
                    t: Throwable
                ) {
                    _loading.value = false
                    Log.e("fetchTeacherById", "Failure", t)
                    _error.value = t.localizedMessage ?: "Network error"
                }
            })
    }

//    fun updateTeacherWithImage(
//        teacherId: Int,
//        request: UpdateTeacherRequest,
//        imagePart: MultipartBody.Part?,
//        token: String,
//        apiKey: String
//    ) {
//        _loading.value = true
//
//        viewModelScope.launch {
//            try {
//                val response = RetrofitClient.instance.updateTeacherWithImage(
//                    teacherId = teacherId,
//                    token = "Bearer $token",
//                    apiKey = apiKey,
//                    data = request,
//                    profilePicture = imagePart
//                )
//
//                if (response.isSuccessful) {
//                    _updateResult.postValue(Result.success(Unit))
//                } else {
//                    val error = response.errorBody()?.string()
//                    Log.e("updateTeacherWithImage", error ?: "Unknown error")
//                    _updateResult.postValue(Result.failure(Exception("Update failed")))
//                }
//            } catch (e: Exception) {
//                _updateResult.postValue(Result.failure(e))
//            } finally {
//                _loading.postValue(false)
//            }
//        }
//    }


    fun updateTeacherWithImage(
        teacherId: Int,
        request: UpdateTeacherRequest,
        imagePart: MultipartBody.Part?,
        token: String,
        apiKey: String
    ) {
        _loading.value = true

        viewModelScope.launch {
            try {

                // 🔥 Convert fields
                val username = request.username.toRequestBody("text/plain".toMediaTypeOrNull())
                val password = request.password?.toRequestBody("text/plain".toMediaTypeOrNull())
                val gender = request.gender.toRequestBody("text/plain".toMediaTypeOrNull())
                val name = request.name.toRequestBody("text/plain".toMediaTypeOrNull())
                val nurseryId = request.nurseryId.toString()
                    .toRequestBody("text/plain".toMediaTypeOrNull())

                // ✅ CRITICAL FIX → send array properly
                val groupParts = request.assignedGroups.map {
                    MultipartBody.Part.createFormData(
                        "assignedGroups[]",   // 🔥 MUST MATCH BACKEND
                        it.toString()
                    )
                }

                val response = RetrofitClient.instance.updateTeacherWithImage(
                    teacherId = teacherId,
                    token = "Bearer $token",
                    apiKey = apiKey,
                    username = username,
                    password = password,
                    gender = gender,
                    name = name,
                    nurseryId = nurseryId,
                    assignedGroups = groupParts,
                    profilePicture = imagePart
                )

                if (response.isSuccessful) {
                    _updateResult.postValue(Result.success(Unit))
                } else {
                    val error = response.errorBody()?.string()
                    Log.e("updateTeacherWithImage", error ?: "Unknown error")

                    val message = try {
                        JSONObject(error ?: "").optString("message", "Update failed")
                    } catch (e: Exception) {
                        "Update failed"
                    }

                    _updateResult.postValue(Result.failure(Exception(message)))
                }

            } catch (e: Exception) {
                Log.e("Teacher Update", "Error", e)
                _updateResult.postValue(Result.failure(e))
            } finally {
                _loading.postValue(false)
            }
        }
    }

}
