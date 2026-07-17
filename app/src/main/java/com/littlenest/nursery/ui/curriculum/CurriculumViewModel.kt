package com.littlenest.nursery.viewmodel.curriculum

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.littlenest.nursery.network.RetrofitClient
import kotlinx.coroutines.launch
import com.littlenest.nursery.ui.curriculum.CurriculumRequest
import com.littlenest.nursery.ui.curriculum.CurriculumResponse
import com.littlenest.nursery.ui.curriculum.GenericResponse
import com.littlenest.nursery.ui.curriculum.GroupedCurriculumResponse

class CurriculumViewModel : ViewModel() {

    private val _curriculumList = MutableLiveData<List<CurriculumResponse>>()
    val curriculumList: LiveData<List<CurriculumResponse>> = _curriculumList

    private val _response = MutableLiveData<GenericResponse?>()
    val response: LiveData<GenericResponse?> get() = _response

    private val _curriculums = MutableLiveData<GroupedCurriculumResponse>()
    val curriculums: LiveData<GroupedCurriculumResponse> = _curriculums

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun addCurriculum(token: String, apiKey: String, request: CurriculumRequest) {
        viewModelScope.launch {
            try {
                val res = RetrofitClient.instance.createCurriculum(token, apiKey, request)
                if (res.isSuccessful) {
                    _response.postValue(res.body())
                } else {
                    _error.postValue(res.errorBody()?.string() ?: "Unknown error")
                }
            } catch (e: Exception) {
                _error.postValue(e.message)
            }
        }
    }


    fun fetchCurriculums(token: String, apiKey: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = RetrofitClient.instance.getCurriculum(token, apiKey)
                if (response.isSuccessful) {
                    _curriculums.value = response.body()
                } else {
                    _error.value = "Error: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun updateCurriculum(
        token: String,
        apiKey: String,
        curriculumId: Int,
        request: CurriculumRequest
    ) {
        _loading.value = true
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.updateCurriculum(
                    "Bearer $token",
                    apiKey,
                    curriculumId,
                    request
                )

                if (response.isSuccessful) {
                    // Refresh curriculum list or update state
                    fetchCurriculums("Bearer $token", apiKey)
                } else {
                    _error.postValue("Failed to update: ${response.code()}")
                }
            } catch (e: Exception) {
                _error.postValue("Error updating: ${e.localizedMessage}")
            } finally {
                _loading.postValue(false)
            }
        }
    }


    fun deleteCurriculum(token: String, apiKey: String, curriculumId: Int) {
        _loading.value = true
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.deleteCurriculum(
                    "Bearer $token",
                    apiKey,
                    curriculumId
                )
                if (response.isSuccessful) {
                    // Remove from the grouped curriculums
                    val current = _curriculums.value
                    if (current != null) {
                        val updatedMap = current.groupedCurriculums.mapValues { (_, curriculums) ->
                            curriculums.filter { it.id != curriculumId }
                        }
                        _curriculums.value = GroupedCurriculumResponse(updatedMap)
                    }
                } else {
                    _error.postValue("Failed to delete curriculum: ${response.code()}")
                }
            } catch (e: Exception) {
                _error.postValue("Error deleting curriculum: ${e.localizedMessage}")
            } finally {
                _loading.postValue(false)
            }
        }
    }


}
