package com.littlenest.nursery.viewmodel.student

import androidx.lifecycle.*
import com.littlenest.nursery.ui.student.Student
import com.littlenest.nursery.network.RetrofitClient
import com.littlenest.nursery.model.RegisterRequest
import com.littlenest.nursery.model.RegisterResponse
import kotlinx.coroutines.launch
import android.util.Log

class StudentViewModel : ViewModel() {

    private val _students = MutableLiveData<List<Student>>()
    val students: LiveData<List<Student>> = _students

    private val _studentDetail = MutableLiveData<Student>()
    val studentDetail: LiveData<Student> = _studentDetail

    private val _registrationResult = MutableLiveData<Result<RegisterResponse>>()
    val registrationResult: LiveData<Result<RegisterResponse>> = _registrationResult

    private val _updateResult = MutableLiveData<Result<Unit>>()
    val updateResult: LiveData<Result<Unit>> = _updateResult

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error


    //Register new student
    fun registerStudent(request: RegisterRequest, token: String, apiKey: String) {
        _loading.value = true
        viewModelScope.launch {
            try {
               // val apiService = RetrofitClient.instance.apiServiceWithAuth(token, apiKey)
                val response = RetrofitClient.instance.registerStudent("Bearer $token", apiKey, request)
                if (response.isSuccessful && response.body() != null) {
                    _registrationResult.postValue(Result.success(response.body()!!))
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.d("registerStudentError","${errorBody}")
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


    fun fetchStudents(token: String, apiKey: String) {
        _loading.value = true

        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getStudents("Bearer $token", apiKey)
                if (response.isSuccessful && response.body() != null) {
                    _students.postValue(response.body()!!.students)
                } else {
                    _error.postValue("Failed to load students: ${response.code()}")
                }
            } catch (e: Exception) {
                _error.postValue("Error: ${e.localizedMessage}")
            } finally {
                _loading.postValue(false)
            }
        }
    }

    // fetch single student by ID (detail page)
    fun fetchStudentById(studentId: Int, token: String, apiKey: String) {
        viewModelScope.launch {
            _loading.postValue(true)
            try {
                val response = RetrofitClient.instance.getStudentById2(studentId, "Bearer $token", apiKey)
                if (response.isSuccessful && response.body() != null) {
                    _studentDetail.postValue(response.body()!!.student)
                } else {
                    _error.postValue("Failed to load student details: ${response.code()}")
                }
            } catch (e: Exception) {
                _error.postValue("Error: ${e.localizedMessage}")
            } finally {
                _loading.postValue(false)
            }
        }
    }


    fun deleteStudent(token: String, apiKey: String, studentId: Int) {
        _loading.value = true
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.deleteStudent(
                    "Bearer $token",
                    apiKey,
                    studentId
                )
                if (response.isSuccessful) {
                    // Remove deleted student from list
                    _students.value = _students.value?.filter { it.studentId != studentId }
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

    // update student without image update
    fun updateStudent(studentId: Int, request: RegisterRequest, token: String, apiKey: String) {
        _loading.value = true
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.updateStudent(studentId, "Bearer $token", apiKey, request)
                if (response.isSuccessful) {
                    _updateResult.value = Result.success(Unit)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.d("updateStudentError","${errorBody}")
                    _updateResult.value = Result.failure(Exception("Failed: ${response.message()}"))
                }
            } catch (e: Exception) {
                _updateResult.value = Result.failure(e)
            } finally {
                _loading.value = false
            }
        }
    }


    fun fetchStudentsByGroup(
        token: String,
        apiKey: String,
        groupId: Int
    ) {
        _loading.value = true

        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getStudentsByGroup(
                    groupId,
                    "Bearer $token",
                    apiKey,
                )

                if (response.isSuccessful && response.body() != null) {
                    _students.postValue(response.body()!!.students)
                } else {
                    _error.postValue("Failed to load students")
                }
            } catch (e: Exception) {
                _error.postValue(e.localizedMessage)
            } finally {
                _loading.postValue(false)
            }
        }
    }


}
