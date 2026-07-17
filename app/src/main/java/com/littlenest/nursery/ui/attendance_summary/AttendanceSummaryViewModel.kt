package com.littlenest.nursery.ui.attendance_summary

import androidx.lifecycle.*
import kotlinx.coroutines.launch
import com.littlenest.nursery.network.RetrofitClient
import android.util.Log

class AttendanceSummaryViewModel : ViewModel() {

    private val _summary = MutableLiveData<AttendanceSummary>()
    val summary: LiveData<AttendanceSummary> = _summary

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    fun loadAttendance(
        token: String,
        apiKey: String,
        groupId: Int,
        date: String
    ) {
        _loading.value = true

        viewModelScope.launch {
            try {
                // 1️⃣ Get absent students
                val absentStudents =
                    RetrofitClient.instance.getAttendanceDetails(
                        "Bearer $token",
                        apiKey,
                        groupId,
                        date
                    )

                // 2️⃣ Fetch total students from API
                val totalStudentsResponse = RetrofitClient.instance.getTotalStudentsByGroup(
                    "Bearer $token",
                    apiKey,
                    groupId
                )
                val totalStudents = totalStudentsResponse.totalStudents

                // 3️⃣ Calculate present count
                val presentCount = totalStudents - absentStudents.size

                // 4️⃣ Post the summary
                _summary.postValue(
                    AttendanceSummary(
                        date = date,
                        presentCount = presentCount,
                        totalStudents = totalStudents,
                        absentStudents = absentStudents.map {
                            StudentAttendance(it.id, it.name, it.profilePicture)
                        }
                    )
                )

            } catch (e: retrofit2.HttpException) {
                val code = e.code()
                val errorBody = e.response()?.errorBody()?.string()

                Log.d("AttendanceAPI", "HTTP $code")
                Log.d("AttendanceAPI", "Error body: $errorBody")

                //_error.postValue("Server error ($code)")
            }
            catch (e: Exception) {
                Log.d("AttendanceAPI", "Unexpected error", e)
                //_error.postValue("Something went wrong")
            } finally {
                _loading.postValue(false)
            }
        }
    }
}
