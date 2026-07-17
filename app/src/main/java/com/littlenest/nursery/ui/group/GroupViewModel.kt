package com.littlenest.nursery.viewmodel.group

import androidx.lifecycle.*
import com.littlenest.nursery.ui.group.Group
import com.littlenest.nursery.ui.group.CreateGroupRequest
import com.littlenest.nursery.network.RetrofitClient
import com.littlenest.nursery.ui.student.Student
import kotlinx.coroutines.launch
import com.littlenest.nursery.utils.UiEvent

class GroupViewModel : ViewModel() {

    private val _groups = MutableLiveData<List<Group>>()
    val groups: LiveData<List<Group>> = _groups

    private val _createdGroup = MutableLiveData<Group?>()
    val createdGroup: LiveData<Group?> = _createdGroup

    private val _updatedGroup = MutableLiveData<Group?>()
    val updatedGroup: LiveData<Group?> = _updatedGroup

    private val _students = MutableLiveData<List<Student>>()
    val students: LiveData<List<Student>> = _students

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _message = MutableLiveData<UiEvent<String>>()
    val message: LiveData<UiEvent<String>> = _message

    fun fetchGroups(token: String, apiKey: String) {
        _loading.value = true

        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getGroups("Bearer $token", apiKey)
                if (response.isSuccessful && response.body() != null) {
                    _groups.postValue(response.body()!!.groups)
                } else {
                    _error.postValue("Failed to load groups: ${response.code()}")
                }
            } catch (e: Exception) {
                _error.postValue("Error: ${e.localizedMessage}")
            } finally {
                _loading.postValue(false)
            }
        }
    }


    fun createGroup(token: String, apiKey: String, name: String, description: String) {
        viewModelScope.launch {
            try {
                val request = CreateGroupRequest(name, description)
                val response = RetrofitClient.instance.createGroup(
                    "Bearer $token",
                    apiKey,
                    request
                )
                _createdGroup.postValue(response)
            } catch (e: Exception) {
                _error.postValue(e.message)
            }
        }
    }

    fun deleteGroup(token: String, apiKey: String, groupId: Int) {
        _loading.value = true
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.deleteGroup(
                    "Bearer $token",
                    apiKey,
                    groupId
                )
                if (response.isSuccessful) {
                    // Remove deleted group from current list
                    _groups.value = _groups.value?.filter { it.id != groupId }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val message = org.json.JSONObject(errorBody ?: "")
                        .optString("message", "Failed to delete group")

                   _message.postValue(UiEvent(message))
                    //_error.postValue("Failed to delete group: ${response.code()}")
                }
            } catch (e: Exception) {
                _error.postValue("Error deleting group: ${e.localizedMessage}")
            } finally {
                _loading.postValue(false)
            }
        }
    }

    fun updateGroup(token: String, apiKey: String, id: Int, name: String, description: String) {
        viewModelScope.launch {
            try {
                val request = CreateGroupRequest(name, description)
                val response = RetrofitClient.instance.updateGroup(
                    "Bearer $token",
                    apiKey,
                    id,
                    request
                )
                _updatedGroup.postValue(response)
            } catch (e: Exception) {
                _error.postValue(e.message)
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
