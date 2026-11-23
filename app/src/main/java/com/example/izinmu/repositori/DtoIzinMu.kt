package com.example.izinmu.repositori

data class ApiEnvelope<T>(val data: T? = null)

data class ApiDaftar<T>(
    val data: List<T> = emptyList(),
    val meta: ApiMeta? = null,
)

data class ApiMeta(
    val page: Int = 1,
    val limit: Int = 20,
    val total: Int = 0,
    val totalPages: Int = 1,
)

data class ApiErrorEnvelope(val error: ApiErrorBody? = null)

data class ApiErrorBody(
    val code: String? = null,
    val message: String? = null,
)

data class LoginBody(val email: String, val password: String)

data class AuthDto(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String? = null,
    val expiresIn: Long? = null,
    val user: UserDto,
)

data class RefreshBody(val refreshToken: String)

data class UserDto(
    val userId: Long? = null,
    val identityNo: String = "",
    val fullName: String = "",
    val email: String? = null,
    val role: String = "STUDENT",
    val isActive: Boolean = true,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)

data class CourseDto(
    val courseId: Long? = null,
    val courseCode: String = "",
    val courseName: String = "",
    val classDay: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val sks: Int? = null,
    val semester: String? = null,
    val instructors: List<UserDto>? = null,
)

data class LeaveRequestDto(
    val leaveRequestId: Long? = null,
    val student: UserDto? = null,
    val course: CourseDto? = null,
    val category: String = "SICK",
    val classDate: String = "",
    val reason: String? = null,
    val status: String = "SUBMITTED",
    val submittedAt: String? = null,
    val reviewedAt: String? = null,
    val reviewedBy: UserDto? = null,
    val instructorNote: String? = null,
    val hasEvidence: Boolean = false,
    val evidenceName: String? = null,
    val evidencePath: String? = null,
    val updatedAt: String? = null,
)

data class DecisionBody(val decision: String, val note: String?)

data class CreateUserBody(
    val identityNo: String,
    val fullName: String,
    val email: String,
    val password: String,
    val role: String,
)

data class AssignInstructorBody(val instructorUserId: Long)

data class MappingDto(
    val mappingId: Long? = null,
    val course: CourseDto? = null,
    val user: UserDto? = null,
)

data class TimeDto(
    val serverTime: String? = null,
    val serverDate: String? = null,
    val timezone: String? = null,
)

data class AuditDto(
    val auditId: Long? = null,
    val actor: String? = null,
    val actorName: String? = null,
    val action: String? = null,
    val detail: String? = null,
    val createdAt: String? = null,
)
