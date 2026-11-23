package com.example.izinmu.repositori

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiIzinMu {
    @POST("auth/login")
    suspend fun login(@Body body: LoginBody): ApiEnvelope<AuthDto>

    @GET("auth/me")
    suspend fun me(): ApiEnvelope<UserDto>

    @POST("auth/logout")
    suspend fun logout(@Body body: RefreshBody): retrofit2.Response<Unit>

    @GET("courses")
    suspend fun courses(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 100,
    ): ApiDaftar<CourseDto>

    @GET("leave-requests")
    suspend fun leaveRequests(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 100,
    ): ApiDaftar<LeaveRequestDto>

    @GET("leave-requests/{id}")
    suspend fun leaveRequest(@Path("id") id: String): ApiEnvelope<LeaveRequestDto>

    @Multipart
    @POST("leave-requests")
    suspend fun createLeaveRequest(
        @Part("courseId") courseId: RequestBody,
        @Part("category") category: RequestBody,
        @Part("classDate") classDate: RequestBody,
        @Part("reason") reason: RequestBody,
        @Part evidence: MultipartBody.Part,
    ): ApiEnvelope<LeaveRequestDto>

    @Multipart
    @PATCH("leave-requests/{id}")
    suspend fun updateLeaveRequest(
        @Path("id") id: String,
        @Part("category") category: RequestBody,
        @Part("classDate") classDate: RequestBody,
        @Part("reason") reason: RequestBody,
        @Part evidence: MultipartBody.Part?,
    ): ApiEnvelope<LeaveRequestDto>

    @DELETE("leave-requests/{id}")
    suspend fun deleteLeaveRequest(@Path("id") id: String): retrofit2.Response<Unit>

    @POST("leave-requests/{id}/decision")
    suspend fun decide(
        @Path("id") id: String,
        @Body body: DecisionBody,
    ): ApiEnvelope<LeaveRequestDto>

    @GET("admin/users")
    suspend fun adminUsers(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 100,
    ): ApiDaftar<UserDto>

    @POST("admin/users")
    suspend fun createUser(@Body body: CreateUserBody): ApiEnvelope<UserDto>

    @GET("admin/mappings")
    suspend fun mappings(
        @Query("kind") kind: String = "instructor",
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 100,
    ): ApiDaftar<MappingDto>

    @POST("admin/courses/{courseId}/instructors")
    suspend fun assignInstructor(
        @Path("courseId") courseId: String,
        @Body body: AssignInstructorBody,
    ): ApiEnvelope<MappingDto>

    @GET("admin/audit-logs")
    suspend fun auditLogs(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 100,
    ): ApiDaftar<AuditDto>

    @GET("meta/time")
    suspend fun serverTime(): ApiEnvelope<TimeDto>
}
