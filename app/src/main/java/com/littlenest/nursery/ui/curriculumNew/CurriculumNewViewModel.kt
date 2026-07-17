package com.littlenest.nursery.ui.curriculumNew

import androidx.lifecycle.*
import kotlinx.coroutines.launch
import com.littlenest.nursery.network.RetrofitClient
import com.littlenest.nursery.utils.UiEvent
import com.littlenest.nursery.utils.parseError

class CurriculumNewViewModel : ViewModel() {

    private val _curriculums = MutableLiveData<List<Curriculum>>()
    val curriculums: LiveData<List<Curriculum>> = _curriculums

    val loading = MutableLiveData<Boolean>()
    val error = MutableLiveData<UiEvent<String>>()

    val createLoading = MutableLiveData<Boolean>()
    val createSuccess = MutableLiveData<Boolean>()

    val deleteCurriculumSuccess = MutableLiveData<Boolean>()
    val updateSuccess = MutableLiveData<Boolean>()

    val curriculumDetailLoading = MutableLiveData<Boolean>()
    val curriculumDetail = MutableLiveData<CurriculumDetail>()

    fun fetchCurriculums(token: String, apiKey: String, standardId: Int) {
        viewModelScope.launch {
            try {
                loading.postValue(true)

                val response = RetrofitClient.instance
                    .getCurriculums(token, apiKey, standardId)

                if (response.isSuccessful) {
                    _curriculums.postValue(response.body()?.data ?: emptyList())
                } else {
                    val message = response.parseError()
                    error.postValue(UiEvent(message))
                }

            } catch (e: Exception) {
                error.postValue(UiEvent(e.message ?: "Failed to load curriculums"))
            } finally {
                loading.postValue(false)
            }
        }
    }

    fun createCurriculum(
        token: String,
        apiKey: String,
        mainTopic: String,
        standardId: Int
    ) {
        viewModelScope.launch {
            try {
                createLoading.postValue(true)

                val request = CurriculumNewRequest(mainTopic, standardId)

                val response = RetrofitClient.instance.createCurriculumNew(
                    token,
                    apiKey,
                    request
                )

                if (response.isSuccessful) {
                    createSuccess.postValue(true)
                } else {
                    val message = response.parseError()
                    error.postValue(UiEvent(message))
                }

            } catch (e: Exception) {
                error.postValue(UiEvent(e.message ?: "Failed"))
            } finally {
                createLoading.postValue(false)
            }
        }
    }

    fun deleteCurriculumNew(token: String, apiKey: String, curriculumId: Int) {

        viewModelScope.launch {
            try {
                loading.postValue(true)

                val response = RetrofitClient.instance.deleteCurriculumNew(
                    token,
                    apiKey,
                    curriculumId
                )

                if (response.isSuccessful) {
                    deleteCurriculumSuccess.postValue(true)
                } else {
                    val message = response.parseError()
                    error.postValue(UiEvent(message))
                }

            } catch (e: Exception) {
                error.postValue(UiEvent(e.message ?: "Delete failed"))
            } finally {
                loading.postValue(false)
            }
        }
    }

    fun updateCurriculum(
        token: String,
        apiKey: String,
        curriculumId: Int,
        mainTopic: String,
        standardId: Int
    ) {
        viewModelScope.launch {
            try {
                loading.postValue(true)

                val request = CurriculumNewRequest(mainTopic, standardId)

                val response = RetrofitClient.instance.updateCurriculum(
                    token,
                    apiKey,
                    curriculumId,
                    request
                )

                if (response.isSuccessful) {
                    updateSuccess.postValue(true)
                } else {
                    val message = response.parseError()
                    error.postValue(UiEvent(message))
                }

            } catch (e: Exception) {
                error.postValue(UiEvent(e.message ?: "Update failed"))
            } finally {
                loading.postValue(false)
            }
        }
    }


    fun fetchCurriculumById(token: String, apiKey: String, curriculumId: Int) {
        viewModelScope.launch {
            try {
                curriculumDetailLoading.postValue(true)

                val response = RetrofitClient.instance.getCurriculumById(
                    token,
                    apiKey,
                    curriculumId
                )

                if (response.isSuccessful) {
                    curriculumDetail.postValue(response.body()?.data)
                } else {
                    val message = response.parseError()
                    error.postValue(UiEvent(message))
                }

            } catch (e: Exception) {
                error.postValue(UiEvent(e.message ?: "Failed to load curriculum"))
            } finally {
                curriculumDetailLoading.postValue(false)
            }
        }
    }
}