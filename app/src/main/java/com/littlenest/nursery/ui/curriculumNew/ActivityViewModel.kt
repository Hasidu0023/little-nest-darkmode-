package com.littlenest.nursery.ui.curriculumNew

import androidx.lifecycle.*
import kotlinx.coroutines.launch
import com.littlenest.nursery.network.RetrofitClient
import com.littlenest.nursery.utils.UiEvent
import com.littlenest.nursery.utils.parseError

class ActivityViewModel : ViewModel() {

    private val _activities = MutableLiveData<List<Activity>>()
    val activities: LiveData<List<Activity>> = _activities

    val loading = MutableLiveData<Boolean>()
    val error = MutableLiveData<UiEvent<String>>()

    val createSuccess = MutableLiveData<Boolean>()
    val deleteSuccess = MutableLiveData<Boolean>()
    val updateSuccess = MutableLiveData<Boolean>()

    fun fetchActivities(token: String, apiKey: String, subTopicId: Int) {
        viewModelScope.launch {
            try {
                loading.postValue(true)

                val response = RetrofitClient.instance.getActivities(
                    token,
                    apiKey,
                    subTopicId
                )

                if (response.isSuccessful) {
                    _activities.postValue(response.body()?.data ?: emptyList())
                } else {
                    val message = response.parseError()
                    error.postValue(UiEvent(message))
                }

            } catch (e: Exception) {
                error.postValue(UiEvent(e.message ?: "Failed to load activities"))
            } finally {
                loading.postValue(false)
            }
        }
    }

    fun createActivity(token: String, apiKey: String, name: String, description: String, subTopicId: Int) {

        viewModelScope.launch {
            try {
                loading.postValue(true)

                val request = ActivityRequest(name, description, subTopicId)

                val response = RetrofitClient.instance.createActivity(
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
                loading.postValue(false)
            }
        }
    }

    fun deleteActivity(token: String, apiKey: String, activityId: Int) {

        viewModelScope.launch {
            try {
                loading.postValue(true)

                val response = RetrofitClient.instance.deleteActivity(
                    token,
                    apiKey,
                    activityId
                )

                if (response.isSuccessful) {
                    deleteSuccess.postValue(true)
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

    fun updateActivity(
        token: String,
        apiKey: String,
        activityId: Int,
        name: String,
        description: String,
        subTopicId: Int
    ) {
        viewModelScope.launch {
            try {
                loading.postValue(true)

                val request = ActivityRequest(name, description, subTopicId)

                val response = RetrofitClient.instance.updateActivity(
                    token,
                    apiKey,
                    activityId,
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