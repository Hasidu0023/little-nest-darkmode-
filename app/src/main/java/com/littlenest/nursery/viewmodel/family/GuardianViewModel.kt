package com.littlenest.nursery.viewmodel.family

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.littlenest.nursery.model.Guardian
import com.littlenest.nursery.model.GuardianRequest
import com.littlenest.nursery.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import kotlinx.coroutines.withContext

class GuardianViewModel : ViewModel() {

    // --- Guardian list LiveData ---
    private val _guardians = MutableLiveData<List<Guardian>>()
    val guardians: LiveData<List<Guardian>> = _guardians

    // --- Loading & message states ---
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message


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
                    _message.postValue(response.body()?.message ?: "Guardians fetched successfully")
                    //Log.i("GuardianViewModel", "Guardians fetched: ${guardiansList.size}")
                } else {
                    val error = response.errorBody()?.string() ?: "Unknown API error"
                    _message.postValue("Failed to load guardians: ${response.code()} - $error")
                    //Log.e("GuardianViewModel", "Error ${response.code()} - $error")
                }

            } catch (e: IOException) {
                _message.postValue("Network error: ${e.localizedMessage}")
                //Log.e("GuardianViewModel", "Network error", e)
            } catch (e: HttpException) {
                _message.postValue("HTTP error: ${e.localizedMessage}")
                //Log.e("GuardianViewModel", "HTTP error", e)
            } catch (e: Exception) {
                _message.postValue("Unexpected error: ${e.localizedMessage}")
               // Log.e("GuardianViewModel", "Unexpected error", e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    /**
     * Add guardian using studentId from SharedPreferences (passed by Fragment)
     */
    fun addGuardian(studentId: Int, guardianData: GuardianRequest, token: String, apiKey: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoading.postValue(true)
                Log.d("addGuardian", "Adding guardian for studentId=$studentId")

                val response = RetrofitClient.instance.addGuardian(
                    token,
                    apiKey,
                    studentId,
                    guardianData
                )

                if (response.isSuccessful) {
                    _message.postValue("Guardian added successfully")
                    Log.i("addGuardian", "Guardian added successfully")
                    withContext(Dispatchers.Main) {
                        fetchGuardians(token, apiKey)
                    }
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                    _message.postValue("Failed to add guardian: ${response.code()} - $errorMsg")
                    Log.e("addGuardian", "Error ${response.code()} - $errorMsg")
                }
            } catch (e: Exception) {
                _message.postValue("Error: ${e.localizedMessage}")
                Log.e("addGuardian", "Unexpected error", e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }


    fun updateGuardian(guardianId: Int, guardianData: GuardianRequest, token: String, apiKey: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoading.postValue(true)
                val response = RetrofitClient.instance.editGuardian(token, apiKey, guardianId, guardianData)
                if (response.isSuccessful) {
                    _message.postValue("Guardian updated successfully")
                    withContext(Dispatchers.Main) {
                        fetchGuardians(token, apiKey)
                    }
                }  else {
                    _message.postValue("Failed: ${response.code()} - ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                _message.postValue("Error: ${e.localizedMessage}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun deleteGuardian(guardianId: Int, token: String, apiKey: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoading.postValue(true)
                val response = RetrofitClient.instance.deleteGuardian(token, apiKey, guardianId)

                if (response.isSuccessful) {
                    _message.postValue("Guardian deleted successfully")
                    withContext(Dispatchers.Main) {
                        fetchGuardians(token, apiKey) // refresh list
                    }
                } else {
                    _message.postValue("Failed to delete guardian: ${response.code()}")
                }
            } catch (e: Exception) {
                _message.postValue("Error: ${e.localizedMessage}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun clearMessage() {
        _message.value = ""
    }
}
