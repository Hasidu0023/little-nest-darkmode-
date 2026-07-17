package com.littlenest.nursery.ui.curriculumNew

import androidx.lifecycle.*
import kotlinx.coroutines.launch
import com.littlenest.nursery.network.RetrofitClient
import com.littlenest.nursery.utils.UiEvent
import com.littlenest.nursery.utils.parseError

class SubTopicViewModel : ViewModel() {

    private val _subTopics = MutableLiveData<List<SubTopic>>()
    val subTopics: LiveData<List<SubTopic>> = _subTopics

    val loading = MutableLiveData<Boolean>()
    val error = MutableLiveData<UiEvent<String>>()

    val createSuccess = MutableLiveData<Boolean>()
    val deleteSubTopicSuccess = MutableLiveData<Boolean>()
    val updateSuccess = MutableLiveData<Boolean>()


    fun fetchSubTopics(token: String, apiKey: String, curriculumId: Int) {
        viewModelScope.launch {
            try {
                loading.postValue(true)

                val response = RetrofitClient.instance.getSubTopics(
                    token,
                    apiKey,
                    curriculumId
                )

                if (response.isSuccessful) {
                    _subTopics.postValue(response.body()?.data ?: emptyList())
                } else {
                    val message = response.parseError()
                    error.postValue(UiEvent(message))
                }

            } catch (e: Exception) {
                error.postValue(UiEvent(e.message ?: "Failed"))
            } finally {
                loading.postValue(false)
            }
        }
    }


    fun createSubTopic(token: String, apiKey: String, name: String, curriculumId: Int) {

        viewModelScope.launch {
            try {
                loading.postValue(true)

                val request = SubTopicRequest(name, curriculumId)

                val response = RetrofitClient.instance.createSubTopic(
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
                error.postValue(UiEvent(e.message ?: "Failed to create"))
            } finally {
                loading.postValue(false)
            }
        }
    }

    fun deleteSubTopic(token: String, apiKey: String, subTopicId: Int) {

        viewModelScope.launch {
            try {
                loading.postValue(true)

                val response = RetrofitClient.instance.deleteSubTopic(
                    token,
                    apiKey,
                    subTopicId
                )

                if (response.isSuccessful) {
                    deleteSubTopicSuccess.postValue(true)
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

    fun updateSubTopic(
        token: String,
        apiKey: String,
        subTopicId: Int,
        name: String,
        curriculumId: Int
    ) {
        viewModelScope.launch {
            try {
                loading.postValue(true)

                val request = SubTopicRequest(name, curriculumId)

                val response = RetrofitClient.instance.updateSubTopic(
                    token,
                    apiKey,
                    subTopicId,
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
}