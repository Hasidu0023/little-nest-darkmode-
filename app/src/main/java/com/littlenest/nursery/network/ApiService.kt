package com.littlenest.nursery.network

import com.littlenest.nursery.ui.attendance.AbsenceResponse
import com.littlenest.nursery.ui.event.Event
import com.littlenest.nursery.ui.event.EventResponseRespond
import com.littlenest.nursery.ui.event.EventSummaryResponse
import com.littlenest.nursery.ui.student.GetStudentResponse
import com.littlenest.nursery.model.LoginRequest
import com.littlenest.nursery.model.LoginResponse
import com.littlenest.nursery.ui.student.StudentResponse
import com.littlenest.nursery.model.RegisterRequest
import com.littlenest.nursery.model.RegisterResponse
import com.littlenest.nursery.ui.teacher.TeacherResponse
import com.littlenest.nursery.ui.teacher.SingleTeacherByIdResponse
import com.littlenest.nursery.ui.teacher.RegisterRequestTeacher
import com.littlenest.nursery.ui.teacher.UpdateTeacherResponse
import com.littlenest.nursery.ui.teacher.UpdateTeacherRequest
import com.littlenest.nursery.ui.journal.JournalPost
import com.littlenest.nursery.ui.attendance.AbsenceRequest
import com.littlenest.nursery.ui.attendance.AbsenceEntry
import com.littlenest.nursery.ui.attendance_summary.StudentAttendance
import com.littlenest.nursery.ui.attendance_summary.TotalStudentsResponse
import com.littlenest.nursery.model.GuardianResponse
import com.littlenest.nursery.model.Guardian
import com.littlenest.nursery.model.GuardianRequest
import com.littlenest.nursery.ui.student.MessageResponse
import com.littlenest.nursery.ui.settings.NurseryResponse
import com.littlenest.nursery.ui.settings.UpdateNurseryRequest
import com.littlenest.nursery.model.PostImage
import com.littlenest.nursery.model.Message
import com.littlenest.nursery.model.MarkReadResponse
import com.littlenest.nursery.ui.chat.ChatListItem
import com.littlenest.nursery.ui.chat.UserItem
import com.littlenest.nursery.ui.group.GetGroupResponse
import com.littlenest.nursery.ui.group.CreateGroupRequest
import com.littlenest.nursery.ui.group.Group
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import com.littlenest.nursery.ui.curriculum.CurriculumRequest
import com.littlenest.nursery.ui.curriculum.GenericResponse
import com.littlenest.nursery.ui.curriculum.UpdateCurriculumResponse
import com.littlenest.nursery.ui.curriculum.GroupedCurriculumResponse
import com.littlenest.nursery.ui.event.EventCreateRequest
import com.littlenest.nursery.ui.curriculumNew.StandardResponse
import com.littlenest.nursery.ui.curriculumNew.CurriculumNewResponse
import com.littlenest.nursery.ui.curriculumNew.SubTopicResponse
import com.littlenest.nursery.ui.curriculumNew.ActivityResponse
import com.littlenest.nursery.ui.curriculumNew.SubTopicRequest
import com.littlenest.nursery.ui.curriculumNew.ActivityRequest
import com.littlenest.nursery.ui.curriculumNew.CurriculumNewRequest
import com.littlenest.nursery.ui.curriculumNew.CurriculumByIdResponse



interface ApiService {

    // ------------------ AUTH ------------------
    @POST("/api/login")
    fun loginUser(
        @Body loginRequest: LoginRequest)
    : Call<LoginResponse>

    @POST("/api/register")
    suspend fun registerStudent(
        @Header("Authorization") token: String,
        @Header("x-api-key") apiKey: String,
        @Body registerRequest: RegisterRequest
    ): Response<RegisterResponse>


    @POST("/api/register")
    suspend fun registerTeacher(
        @Header("Authorization") token: String,
        @Header("x-api-key") apiKey: String,
        @Body registerRequest: RegisterRequestTeacher
    ): Response<RegisterResponse>

    // ------------------ STUDENT ENDPOINTS ------------------

    @GET("api/students/group/{groupId}")
    suspend fun getStudentsByGroup(
        @Path("groupId") groupId: Int,
        @Header("Authorization") token: String,
        @Header("x-api-key") apiKey: String
    ): Response<StudentResponse>

    @GET("api/students")
    suspend fun getStudents(
        @Header("Authorization") token: String,
        @Header("x-api-key") apiKey: String
    ): Response<StudentResponse>

    //student fragment for list students
    @GET("api/students/{studentId}")
    suspend fun getStudentById2(
        @Path("studentId") studentId: Int,
        @Header("Authorization") token: String,
        @Header("x-api-key") apiKey: String
    ): Response<GetStudentResponse>

    @GET("api/students/{studentId}")
     fun getStudentById(
        @Path("studentId") studentId: Int,
        @Header("Authorization") token: String,
        @Header("x-api-key") apiKey: String
    ): Call<GetStudentResponse>

    @DELETE("/api/students/{studentId}")
    suspend fun deleteStudent(
        @Header("Authorization") token: String,
        @Header("x-api-key") apiKey: String,
        @Path("studentId") studentId: Int,
    ): Response<Unit>

    @Multipart
    @PUT("api/students/{studentId}")
    suspend fun updateStudentMultipart(
        @Header("Authorization") token: String,
        @Header("x-api-key") apiKey: String,
        @Path("studentId") studentId: Int,
        @Part profilePicture: MultipartBody.Part?,
        @Part("extraData[fullName]") fullName: RequestBody?,
        @Part("extraData[nickname]") nickname: RequestBody?,
        @Part("extraData[address]") address: RequestBody?,
        @Part("extraData[city]") city: RequestBody?,
        @Part("extraData[nativeLanguage]") nativeLanguage: RequestBody?,
        @Part("extraData[allergies]") allergies: RequestBody?,
        @Part("extraData[comment]") comment: RequestBody?,
        @Part("extraData[dateOfBirth]") dateOfBirth: RequestBody?,
        @Part("extraData[dropOffTime]") dropOffTime: RequestBody?,
        @Part("extraData[pickupTime]") pickupTime: RequestBody?,
        @Part("extraData[photoConsent]") photoConsent: RequestBody?
    ): Response<MessageResponse>

    @PUT("api/students/{studentId}")
    suspend fun updateStudent(
        @Path("studentId") studentId: Int,
        @Header("Authorization") token: String,
        @Header("x-api-key") apiKey: String,
        @Body request: RegisterRequest
    ): Response<Unit>

    // ------------------ GUARDIANS ENDPOINTS ------------------
    @GET("api/me/guardians")
    suspend fun getGuardiansForStudent(
        @Header("Authorization") authToken: String,
        @Header("x-api-key") apiKey: String
    ): Response<GuardianResponse>

    @POST("api/students/{studentId}/guardians")
    suspend fun addGuardian(
        @Header("Authorization") token: String,
        @Header("x-api-key") apiKey: String,
        @Path("studentId") studentId: Int,
        @Body guardianData: GuardianRequest
    ): Response<Guardian>

    @PUT("api/guardians/{guardianId}")
    suspend fun editGuardian(
        @Header("Authorization") token: String,
        @Header("x-api-key") apiKey: String,
        @Path("guardianId") guardianId: Int,
        @Body guardianData: GuardianRequest
    ): Response<Guardian>

    @DELETE("api/guardians/{guardianId}")
    suspend fun deleteGuardian(
        @Header("Authorization") token: String,
        @Header("x-api-key") apiKey: String,
        @Path("guardianId") guardianId: Int
    ): Response<Unit>


    // ------------------ NURSERY ENDPOINTS ------------------
    @GET("api/nursery/{id}")
    suspend fun getNurseryById(
        @Header("Authorization") token: String,
        @Header("x-api-key") apiKey: String,
        @Path("id") id: Int
    ): NurseryResponse

//    @Multipart
//    @PUT("api/nursery/{id}")
//    suspend fun updateNursery(
//        @Path("id") nurseryId: Int,
//        @Header("Authorization") token: String,
//        @Header("x-api-key") apiKey: String,
//        @Part("name") name: RequestBody,
//        @Part("description") description: RequestBody,
//        @Part("nursery_email") nurseryEmail: RequestBody,
//        @Part("address") address: RequestBody,
//        @Part("language") language: RequestBody,
//        @Part image: MultipartBody.Part? = null
//    ): NurseryResponse


    @PUT("api/nursery/{id}")
    suspend fun updateNursery(
        @Path("id") nurseryId: Int,
        @Header("Authorization") token: String,
        @Header("x-api-key") apiKey: String,
        @Body request: UpdateNurseryRequest
    ): NurseryResponse

    @Multipart
    @PUT("api/nursery/{id}")
    suspend fun updateNurseryWithImage(
        @Path("id") nurseryId: Int,
        @Header("Authorization") token: String,
        @Header("x-api-key") apiKey: String,

        @Part("name") name: RequestBody,
        @Part("description") description: RequestBody,
        @Part("nursery_email") email: RequestBody,
        @Part("address") address: RequestBody,
        @Part("language") language: RequestBody,

        @Part image: MultipartBody.Part?
    ): NurseryResponse


    // ------------------ TEACHERS ------------------
    @GET("api/teachers/{id}")
    fun getTeacherById(
        @Path("id") teacherId: Int,
        @Header("Authorization") token: String,
        @Header("x-api-key") apiKey: String
    ): Call<SingleTeacherByIdResponse>

    @GET("api/teachers")
    fun getTeachers(
        @Header("Authorization") token: String,
        @Header("x-api-key") apiKey: String
    ): Call<TeacherResponse>

    @PUT("api/teachers/{teacherId}")
    suspend fun updateTeacher(
        @Path("teacherId") teacherId: Int,
        @Header("Authorization") token: String,
        @Header("x-api-key") apiKey: String,
        @Body request: UpdateTeacherRequest
    ): Response<UpdateTeacherResponse>


    @Multipart
    @PUT("api/teachers/{teacherId}")
    suspend fun updateTeacherWithImage(
        @Path("teacherId") teacherId: Int,
        @Header("Authorization") token: String,
        @Header("x-api-key") apiKey: String,

        @Part("username") username: RequestBody,
        @Part("password") password: RequestBody?,
        @Part("gender") gender: RequestBody,
        @Part("name") name: RequestBody,
        @Part("nurseryId") nurseryId: RequestBody,
        @Part assignedGroups: List<MultipartBody.Part>, // ✅ IMPORTANT FIX
        @Part profilePicture: MultipartBody.Part?
    ): Response<GenericResponse>



    @DELETE("/api/teachers/{teacherId}")
    suspend fun deleteTeacher(
        @Header("Authorization") token: String,
        @Header("x-api-key") apiKey: String,
        @Path("teacherId") teacherId: Int,
    ): Response<Unit>

    // ------------------ GROUPS ------------------
    @GET("api/groups")
    suspend fun getGroups(
        @Header("Authorization") token: String,
        @Header("x-api-key") apiKey: String
    ): Response<GetGroupResponse>


    @POST("/api/create-group")
    suspend fun createGroup(
        @Header("Authorization") token: String,
        @Header("x-api-key") apiKey: String,
        @Body request: CreateGroupRequest
    ): Group

    @DELETE("/api/group/{id}")
    suspend fun deleteGroup(
        @Header("Authorization") token: String,
        @Header("x-api-key") apiKey: String,
        @Path("id") groupId: Int
    ): Response<Unit>

    @PUT("/api/group/{id}")
    suspend fun updateGroup(
        @Header("Authorization") token: String,
        @Header("x-api-key") apiKey: String,
        @Path("id") id: Int,
        @Body request: CreateGroupRequest
    ): Group

    // ------------------ FAMILY POSTS ------------------

    @Multipart
    @POST("api/posts")
    suspend fun createPost(
        @Header("Authorization") token: String,
        @Header("x-api-key") apiKey: String,

        @Part("description") description: RequestBody,
        @Part("groupId") groupId: RequestBody,
        @Part("curriculumId") curriculumId: RequestBody?,
        @Part("subTopicId") subTopicId: RequestBody?,                 // ✅ NEW
        @Part("selectedActivities") selectedActivities: RequestBody?, // ✅ NEW
        @Part("taggedStudents") taggedStudents: RequestBody,
        @Part images: List<MultipartBody.Part>
    ): Response<Unit>

    @Multipart
    @PUT("api/posts/{id}")
    suspend fun updatePost(
        @Path("id") postId: Int,
        @Header("Authorization") token: String,
        @Header("x-api-key") apiKey: String,

        @Part("description") description: RequestBody,
        @Part("groupId") groupId: RequestBody,
        @Part("curriculumId") curriculumId: RequestBody?,
        @Part("subTopicId") subTopicId: RequestBody?,          // ✅ NEW
        @Part("selectedActivities") selectedActivities: RequestBody, // ✅ NEW
        @Part("taggedStudents") taggedStudents: RequestBody,

        @Part("existingImages") existingImages: RequestBody, // ✅ ADD THIS
        @Part images: List<MultipartBody.Part> // new/updated images
    ): Response<Unit>

    @GET("api/posts/tagged")
    suspend fun getTaggedPosts(
        @Header("Authorization") token: String,
        @Header("x-api-key") apiKey: String
    ): Response<List<JournalPost>>

    @GET("api/posts/")
    suspend fun getPostsByTeacherGroup(
        @Header("Authorization") token: String,
        @Header("x-api-key") apiKey: String
    ): Response<List<JournalPost>>

    @GET("api/posts/{id}")
    suspend fun getPostById(
        @Header("Authorization") token: String,
        @Header("x-api-key") apiKey: String,
        @Path("id") postId: Int
    ): Response<JournalPost>

    @DELETE("api/posts/{id}")
    suspend fun deletePost(
        @Header("Authorization") token: String,
        @Header("x-api-key") apiKey: String,
        @Path("id") postId: Int
    ): Response<Unit>


    // ------------------ EVENTS ------------------
    @GET("api/event/student")
    suspend fun getEvents(
        @Header("Authorization") token: String,
        @Header("x-api-key") apiKey: String,
        @Query("status") status: String
    ): List<Event>

    @GET("api/event/teacher")
    suspend fun getEventsForTeacher(
        @Header("Authorization") token: String,
        @Header("x-api-key") apiKey: String,
        @Query("status") status: String
    ): List<Event>

    @PATCH("api/event/{eventId}/respond")
    suspend fun respondToEvent(
        @Header("Authorization") token: String,
        @Header("x-api-key") apiKey: String,
        @Path("eventId") eventId: Int,
        @Body body: Map<String, String>
    ): EventResponseRespond

    //fetch all the events for admin
    @GET("api/event/admin")
    suspend fun getAdminEvents(
        @Header("Authorization") token: String,
        @Header("x-api-key") apiKey: String,
        @Query("status") status: String
    ): List<Event>

    //get event summary by id
    @GET("api/event/{eventId}/summary")
    suspend fun getEventSummary(
        @Path("eventId") eventId: Int,
        @Header("Authorization") token: String,
        @Header("x-api-key") apiKey: String
    ): EventSummaryResponse

    @DELETE("/api/event/{id}")
    suspend fun deleteEvent(
        @Header("Authorization") token: String,
        @Header("x-api-key") apiKey: String,
        @Path("id") eventId: Int
    ): Response<Unit>

    @PUT("/api/event/{id}")
    suspend fun updateEvent(
        @Header("Authorization") token: String,
        @Header("x-api-key") apiKey: String,
        @Path("id") id: Int,
        @Body request: EventCreateRequest
    ): Response<Unit>

    @POST("/api/event")
    suspend fun createEvent(
        @Header("Authorization") token: String,
        @Header("x-api-key") apiKey: String,
        @Body event: EventCreateRequest
    ): Response<Event>

    // ------------------ ABSENCE ------------------
    @POST("api/absence")
    suspend fun markAbsence(
        @Header("Authorization") auth: String,
        @Header("x-api-key") apiKey: String,
        @Body body: AbsenceRequest
    ): Response<AbsenceResponse>

    @GET("/api/absence/student/{studentId}")
    suspend fun getStudentAbsences(
        @Header("Authorization") auth: String,
        @Header("x-api-key") apiKey: String,
        @Path("studentId") studentId: Int
    ): Response<List<AbsenceEntry>>

    //attendance summary
    @GET("api/absence/details")
    suspend fun getAttendanceDetails(
        @Header("Authorization") token: String,
        @Header("x-api-key") apiKey: String,
        @Query("groupId") groupId: Int,
        @Query("date") date: String
    ): List<StudentAttendance>

    //get total sutdents by group
    @GET("api/groups/{groupId}/student-count")
    suspend fun getTotalStudentsByGroup(
        @Header("Authorization") token: String,
        @Header("x-api-key") apiKey: String,
        @Path("groupId") groupId: Int
    ): TotalStudentsResponse

    // ------------------ 📸 ALBUM ENDPOINT ------------------
    @GET("api/posts/albums")
    suspend fun getAlbumImages(
        @Header("Authorization") token: String,
        @Header("x-api-key") apiKey: String
    ): Response<List<PostImage>>


    // ------------------ 📸 MESSAGE ENDPOINT ------------------
    // Send a message (text or file)
    @Multipart
    @POST("api/messages/")
    suspend fun sendMessage(
        @Header("Authorization") token: String,
        @Header("x-api-key") apiKey: String,
        @Part("receiverId") receiverId: Int? = null,
        @Part("groupId") groupId: Int? = null,
        @Part("messageText") messageText: RequestBody?,
        @Part file: MultipartBody.Part? = null
    ): Response<Message>

    // Get 1:1 conversation
    @GET("/api/messages/{partnerId}")
    suspend fun getConversation(
        @Header("Authorization") token: String,
        @Header("x-api-key") apiKey: String,
        @Path("partnerId") partnerId: Int
    ): Response<List<Message>>

    // Get group messages
    @GET("/api/messages/group/{groupId}")
    suspend fun getGroupMessages(
        @Header("Authorization") token: String,
        @Header("x-api-key") apiKey: String,
        @Path("groupId") groupId: Int
    ): Response<List<Message>>

    @GET("/api/chat-list")
    suspend fun getChatList(
        @Header("Authorization") token: String,
        @Header("x-api-key") apiKey: String
    ): Response<List<ChatListItem>>

    @GET("/api/messages/chat-users")
    suspend fun getChatUsers(
        @Header("Authorization") token: String,
        @Header("x-api-key") apiKey: String
    ): List<UserItem>

    @PUT("/api/messages/read/{messageId}")
    suspend fun markMessageAsRead(
        @Header("Authorization") token: String,
        @Header("x-api-key") apiKey: String,
        @Path("messageId") messageId: Int
    ): Response<MarkReadResponse>

//    @PUT("messages/read/{messageId}")
//    suspend fun markMessageAsRead(
//        @Header("Authorization") token: String,
//        @Header("x-api-key") apiKey: String,
//        @Path("messageId") messageId: Int
//    ): ResponseBody



    // ------------------CURRICULUM ENDPOINT ------------------
    @POST("/api/curriculum")
    suspend fun createCurriculum(
        @Header("Authorization") token: String,
        @Header("x-api-key") apiKey: String,
        @Body request: CurriculumRequest
    ): Response<GenericResponse>

    @GET("/api/curriculum/mine")
    suspend fun getCurriculum(
        @Header("Authorization") token: String,
        @Header("x-api-key") apiKey: String
    ): Response<GroupedCurriculumResponse>

    @PUT("/api/curriculum/{id}")
    suspend fun updateCurriculum(
        @Header("Authorization") authHeader: String,
        @Header("x-api-key") apiKey: String,
        @Path("id") id: Int,
        @Body updatedData: CurriculumRequest
    ): Response<UpdateCurriculumResponse>

    @DELETE("/api/curriculum/{id}")
    suspend fun deleteCurriculum(
        @Header("Authorization") token: String,
        @Header("x-api-key") apiKey: String,
        @Path("id") curriculumId: Int
    ): Response<GenericResponse>



    // ------------------ CURRICULUM NEW ------------------

    // Standards
    @GET("/api/standards")
    suspend fun getStandards(
        @Header("Authorization") token: String,
        @Header("x-api-key") apiKey: String
    ): Response<StandardResponse>

    // Curriculums
    @GET("api/curriculums")
    suspend fun getCurriculums(
        @Header("Authorization") token: String,
        @Header("x-api-key") apiKey: String,
        @Query("standardId") standardId: Int
    ): Response<CurriculumNewResponse>

    @POST("/api/curriculums")
    suspend fun createCurriculumNew(
        @Header("Authorization") token: String,
        @Header("x-api-key") apiKey: String,
        @Body request: CurriculumNewRequest
    ): Response<GenericResponse>

    @DELETE("/api/curriculums/{id}")
    suspend fun deleteCurriculumNew(
        @Header("Authorization") token: String,
        @Header("x-api-key") apiKey: String,
        @Path("id") curriculumId: Int
    ): Response<GenericResponse>

    @PUT("/api/curriculums/{id}")
    suspend fun updateCurriculum(
        @Header("Authorization") token: String,
        @Header("x-api-key") apiKey: String,
        @Path("id") curriculumId: Int,
        @Body request: CurriculumNewRequest
    ): Response<GenericResponse>

    //subTopics
    @GET("/api/subtopics")
    suspend fun getSubTopics(
        @Header("Authorization") token: String,
        @Header("x-api-key") apiKey: String,
        @Query("curriculumId") curriculumId: Int
    ): Response<SubTopicResponse>

    @POST("/api/subtopics")
    suspend fun createSubTopic(
        @Header("Authorization") token: String,
        @Header("x-api-key") apiKey: String,
        @Body request: SubTopicRequest
    ): Response<GenericResponse>

    @DELETE("/api/subtopics/{id}")
    suspend fun deleteSubTopic(
        @Header("Authorization") token: String,
        @Header("x-api-key") apiKey: String,
        @Path("id") subTopicId: Int
    ): Response<GenericResponse>

    @PUT("/api/subtopics/{id}")
    suspend fun updateSubTopic(
        @Header("Authorization") token: String,
        @Header("x-api-key") apiKey: String,
        @Path("id") subTopicId: Int,
        @Body request: SubTopicRequest
    ): Response<GenericResponse>


    //Activities
    @GET("/api/activities")
    suspend fun getActivities(
        @Header("Authorization") token: String,
        @Header("x-api-key") apiKey: String,
        @Query("subTopicId") subTopicId: Int
    ): Response<ActivityResponse>

    @POST("/api/activities")
    suspend fun createActivity(
        @Header("Authorization") token: String,
        @Header("x-api-key") apiKey: String,
        @Body request: ActivityRequest
    ): Response<GenericResponse>

    @DELETE("/api/activities/{id}")
    suspend fun deleteActivity(
        @Header("Authorization") token: String,
        @Header("x-api-key") apiKey: String,
        @Path("id") activityId: Int
    ): Response<GenericResponse>

    @PUT("/api/activities/{id}")
    suspend fun updateActivity(
        @Header("Authorization") token: String,
        @Header("x-api-key") apiKey: String,
        @Path("id") activityId: Int,
        @Body request: ActivityRequest
    ): Response<GenericResponse>

    //get whole tree - get curriculum by id
    @GET("api/curriculums/{id}")
    suspend fun getCurriculumById(
        @Header("Authorization") token: String,
        @Header("x-api-key") apiKey: String,
        @Path("id") id: Int
    ): Response<CurriculumByIdResponse>
}
