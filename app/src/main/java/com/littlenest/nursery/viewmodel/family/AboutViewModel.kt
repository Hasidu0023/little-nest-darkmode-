package com.littlenest.nursery.viewmodel.family

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.littlenest.nursery.model.Guardian
import com.littlenest.nursery.ui.student.Student
import com.littlenest.nursery.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class AboutViewModel : ViewModel() {

    // --- Student details LiveData ---
    private val _studentDetails = MutableLiveData<Student?>()
    val studentDetails: LiveData<Student?> = _studentDetails

    // --- Guardian list LiveData ---
    private val _guardians = MutableLiveData<List<Guardian>>()
    val guardians: LiveData<List<Guardian>> = _guardians

    // --- Loading & message states ---
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message


    /**
     * Fetches the detailed information for a specific student.
     */
    fun fetchStudentDetails(studentId: Int, authToken: String, apiKey: String) {
        if (studentId == 0) {
            _message.value = "Invalid Student ID."
            return
        }

        _isLoading.value = true
        Log.d("AboutViewModel", "Fetching student details for ID: $studentId")

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.instance.getStudentById(studentId, authToken, apiKey).execute()
                _isLoading.postValue(false)

                if (response.isSuccessful) {
                    val student = response.body()?.student
                    _studentDetails.postValue(student)
                    Log.i("AboutViewModel", "Student details fetched successfully: ${student?.extraData?.fullName}")
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    _message.postValue("Failed to fetch student details: ${response.code()} - $errorBody")
                    Log.e("AboutViewModel", "API Error: ${response.code()}, Body: $errorBody")
                }
            } catch (e: Exception) {
                _isLoading.postValue(false)
                _message.postValue("An unexpected error occurred: ${e.localizedMessage}")
                Log.e("AboutViewModel", "Coroutine Exception", e)
            }
        }
    }

    /**
     * Fetches guardian list for the logged-in student
     */
    fun fetchGuardians(authToken: String, apiKey: String) {
        _isLoading.value = true
        Log.d("AboutViewModel", "Fetching guardians for current student")

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.instance.getGuardiansForStudent(authToken, apiKey)

                if (response.isSuccessful) {
                    val guardiansList = response.body()?.studentProfile?.guardians ?: emptyList()
                    _guardians.postValue(guardiansList)
                    //_message.postValue(response.body()?.message ?: "Guardians fetched successfully")
                    //Log.i("AboutViewModel", "Guardians fetched: ${guardiansList.size}")
                } else {
                    val error = response.errorBody()?.string() ?: "Unknown API error"
                    _message.postValue("Failed to load guardians: ${response.code()} - $error")
                    //Log.e("AboutViewModel", "Error ${response.code()} - $error")
                }

            } catch (e: IOException) {
                _message.postValue("Network error: ${e.localizedMessage}")
                //Log.e("AboutViewModel", "Network error", e)
            } catch (e: HttpException) {
                _message.postValue("HTTP error: ${e.localizedMessage}")
                //Log.e("AboutViewModel", "HTTP error", e)
            } catch (e: Exception) {
                _message.postValue("Unexpected error: ${e.localizedMessage}")
                //Log.e("AboutViewModel", "Unexpected error", e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun clearMessage() {
        _message.value = ""
    }
}
