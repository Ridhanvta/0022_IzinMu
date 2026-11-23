package com.example.izinmu.repositori

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.annotation.StringRes
import com.example.izinmu.R
import com.example.izinmu.BuildConfig
import com.example.izinmu.model.AuditEntry
import com.example.izinmu.model.Course
import com.example.izinmu.model.LeaveCategory
import com.example.izinmu.model.LeaveRequest
import com.example.izinmu.model.LeaveStatus
import com.example.izinmu.model.MAX_EVIDENCE_BYTES
import com.example.izinmu.model.Role
import com.example.izinmu.model.User
import com.example.izinmu.util.formatTimestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class InputPengguna(
    val name: String,
    val identityNo: String,
    val email: String,
    val password: String,
    val role: Role,
    val courseIds: List<String>,
)

class KesalahanApi(
    override val message: String,
    val code: String? = null,
) : Exception(message)

interface RepositoriIzinMu {
    suspend fun login(email: String, password: String): User
    suspend fun sesiTersimpan(): User?
    suspend fun logout()
    suspend fun courses(): List<Course>
    suspend fun requests(): List<LeaveRequest>
    suspend fun requestDetail(id: String): LeaveRequest
    suspend fun simpanPengajuan(
        id: String?,
        courseId: String?,
        classDate: String?,
        category: LeaveCategory?,
        reason: String?,
        evidenceUri: String?,
    ): LeaveRequest
    suspend fun deleteRequest(id: String)
    suspend fun decideRequest(id: String, status: LeaveStatus, note: String): LeaveRequest
    suspend fun users(): List<User>
    suspend fun auditEntries(): List<AuditEntry>
    suspend fun tambahPengguna(input: InputPengguna): User
    suspend fun todayKey(): String
}

class RestRepositoriIzinMu(
    private val context: Context,
    private val sesi: SesiIzinMu = SesiIzinMu(context),
    private val api: ApiIzinMu = PembuatApi.buat(BuildConfig.API_BASE_URL, sesi),
) : RepositoriIzinMu {

    private fun teksRes(@StringRes id: Int, vararg args: Any): String = context.getString(id, *args)

    override suspend fun login(email: String, password: String): User = panggil {
        val auth = api.login(LoginBody(email.trim(), password)).data
            ?: throw KesalahanApi(teksRes(R.string.error_login_data))
        sesi.simpan(auth)
        auth.user.toUser()
    }

    override suspend fun sesiTersimpan(): User? {
        if (sesi.accessToken().isNullOrBlank()) return null
        return panggil {
            val data = api.me().data
            if (data == null) {
                sesi.hapus()
                null
            } else {
                data.toUser()
            }
        }
    }

    override suspend fun logout() {
        val refresh = sesi.refreshToken()
        if (!refresh.isNullOrBlank()) {
            runCatching { api.logout(RefreshBody(refresh)) }
        }
        sesi.hapus()
    }

    override suspend fun courses(): List<Course> = panggil {
        semuaHalaman { page -> api.courses(page = page, limit = 100) }
            .map { it.toCourse(context) }
            .distinctBy { it.id }
    }

    override suspend fun requests(): List<LeaveRequest> = panggil {
        semuaHalaman { page -> api.leaveRequests(page = page, limit = 100) }
            .map { it.toRequest(context) }
    }

    override suspend fun requestDetail(id: String): LeaveRequest = panggil {
        api.leaveRequest(id).data?.toRequest(context)
            ?: throw KesalahanApi(teksRes(R.string.error_request_missing))
    }

    override suspend fun simpanPengajuan(
        id: String?,
        courseId: String?,
        classDate: String?,
        category: LeaveCategory?,
        reason: String?,
        evidenceUri: String?,
    ): LeaveRequest = panggil {
        if (courseId.isNullOrBlank()) throw KesalahanApi(teksRes(R.string.error_course_required))
        if (classDate.isNullOrBlank()) throw KesalahanApi(teksRes(R.string.error_date_required))
        if (reason.isNullOrBlank()) throw KesalahanApi(teksRes(R.string.error_reason_required))
        val kategori = category ?: LeaveCategory.SAKIT
        val bukti = evidenceUri?.takeIf { it.isNotBlank() }?.let { partBukti(it) }

        val hasil = if (id == null) {
            if (bukti == null) throw KesalahanApi(teksRes(R.string.error_evidence_required))
            api.createLeaveRequest(
                courseId = teks(courseId),
                category = teks(kategori.toApi()),
                classDate = teks(classDate),
                reason = teks(reason),
                evidence = bukti,
            ).data
        } else {
            api.updateLeaveRequest(
                id = id,
                category = teks(kategori.toApi()),
                classDate = teks(classDate),
                reason = teks(reason),
                evidence = bukti,
            ).data
        }
        hasil?.toRequest(context) ?: throw KesalahanApi(teksRes(R.string.error_save_request))
    }

    override suspend fun deleteRequest(id: String) {
        panggil {
            val respons = api.deleteLeaveRequest(id)
            if (!respons.isSuccessful) {
                throw KesalahanApi(teksRes(R.string.error_delete_request, respons.code()))
            }
        }
    }

    override suspend fun decideRequest(
        id: String,
        status: LeaveStatus,
        note: String,
    ): LeaveRequest = panggil {
        val keputusan = when (status) {
            LeaveStatus.DISETUJUI -> "APPROVED"
            LeaveStatus.DITOLAK -> "REJECTED"
            LeaveStatus.DIAJUKAN -> throw KesalahanApi(teksRes(R.string.error_decision_status))
        }
        api.decide(id, DecisionBody(keputusan, note.ifBlank { null })).data?.toRequest(context)
            ?: throw KesalahanApi(teksRes(R.string.error_decision))
    }

    override suspend fun users(): List<User> = panggil {
        val daftar = semuaHalaman { page -> api.adminUsers(page = page, limit = 100) }
        val kodeKelas = runCatching {
            semuaHalaman { page -> api.mappings(kind = "instructor", page = page, limit = 100) }
                .groupBy { it.user?.userId }
                .mapValues { (_, items) -> items.mapNotNull { it.course?.courseCode } }
        }.getOrDefault(emptyMap())
        daftar.map { dto ->
            dto.toUser(courseCodes = kodeKelas[dto.userId].orEmpty())
        }
    }

    override suspend fun auditEntries(): List<AuditEntry> = panggil {
        semuaHalaman { page -> api.auditLogs(page = page, limit = 100) }.map { it.toAudit() }
    }

    override suspend fun tambahPengguna(input: InputPengguna): User = panggil {
        if (input.email.isBlank()) throw KesalahanApi(teksRes(R.string.error_email_required))
        if (input.password.length < 8) throw KesalahanApi(teksRes(R.string.error_password_length))
        if (input.role == Role.INSTRUKTUR && input.courseIds.isEmpty()) {
            throw KesalahanApi(teksRes(R.string.error_course_min_one))
        }
        val dibuat = api.createUser(
            CreateUserBody(
                identityNo = input.identityNo.trim(),
                fullName = input.name.trim(),
                email = input.email.trim(),
                password = input.password,
                role = input.role.toApi(),
            ),
        ).data ?: throw KesalahanApi(teksRes(R.string.error_create_user))
        val id = dibuat.userId ?: throw KesalahanApi(teksRes(R.string.error_user_id))
        for (courseId in input.courseIds.distinct()) {
            api.assignInstructor(courseId, AssignInstructorBody(id))
        }
        dibuat.toUser(courseCodes = input.courseIds)
    }

    override suspend fun todayKey(): String {
        val dariServer = runCatching { api.serverTime().data?.serverDate }.getOrNull()
        if (!dariServer.isNullOrBlank()) return dariServer
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        formatter.timeZone = TimeZone.getTimeZone("Asia/Jakarta")
        return formatter.format(Date())
    }

    private suspend fun <T> semuaHalaman(
        ambil: suspend (Int) -> ApiDaftar<T>,
    ): List<T> = withContext(Dispatchers.IO) {
        val hasil = mutableListOf<T>()
        var halaman = 1
        var totalHalaman = 1
        do {
            val jawaban = ambil(halaman)
            hasil += jawaban.data
            totalHalaman = jawaban.meta?.totalPages ?: 1
            halaman++
        } while (halaman <= totalHalaman && halaman <= 20)
        hasil
    }

    private suspend fun <T> panggil(block: suspend () -> T): T = withContext(Dispatchers.IO) {
        try {
            block()
        } catch (e: KesalahanApi) {
            throw e
        } catch (e: HttpException) {
            val pesan = bacaPesanError(e)
            if (e.code() == 401) sesi.hapus()
            throw KesalahanApi(pesan, e.code().toString())
        } catch (e: IOException) {
            throw KesalahanApi(teksRes(R.string.error_network))
        }
    }

    private fun bacaPesanError(e: HttpException): String {
        val mentah = runCatching { e.response()?.errorBody()?.string() }.getOrNull()
        val isi = runCatching {
            com.google.gson.Gson().fromJson(mentah, ApiErrorEnvelope::class.java)
        }.getOrNull()
        val pesan = isi?.error?.message
        if (!pesan.isNullOrBlank()) return pesan
        return when (e.code()) {
            401 -> teksRes(R.string.error_unauthorized)
            403 -> teksRes(R.string.error_forbidden)
            404 -> teksRes(R.string.error_not_found)
            409 -> teksRes(R.string.error_conflict)
            else -> teksRes(R.string.error_generic, e.code())
        }
    }

    private fun teks(nilai: String): RequestBody =
        nilai.toRequestBody("text/plain; charset=utf-8".toMediaType())

    private fun partBukti(uriString: String): MultipartBody.Part {
        val uri = Uri.parse(uriString)
        val resolver = context.contentResolver
        val mime = resolver.getType(uri) ?: "image/jpeg"
        if (mime != "image/jpeg" && mime != "image/jpg" && mime != "image/png") {
            throw KesalahanApi(teksRes(R.string.error_evidence_type))
        }
        val nama = resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor ->
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
            }
            ?: teksRes(R.string.evidence_file_name, System.currentTimeMillis())
        val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw KesalahanApi(teksRes(R.string.error_evidence_read))
        if (bytes.size > MAX_EVIDENCE_BYTES) {
            throw KesalahanApi(teksRes(R.string.error_evidence_size))
        }
        val body = bytes.toRequestBody(mime.toMediaType())
        return MultipartBody.Part.createFormData("evidence", nama, body)
    }
}

private fun String.toApi(): String = when (this) {
    "STUDENT" -> "STUDENT"
    else -> this
}

private fun Role.toApi(): String = when (this) {
    Role.MAHASISWA -> "STUDENT"
    Role.INSTRUKTUR -> "INSTRUCTOR"
    Role.ADMIN -> "ADMIN"
}

private fun LeaveCategory.toApi(): String = when (this) {
    LeaveCategory.SAKIT -> "SICK"
    LeaveCategory.AKADEMIK -> "ACADEMIC"
    LeaveCategory.MENDADAK -> "EMERGENCY"
    LeaveCategory.KEPERLUAN_KELUARGA -> "FAMILY"
}

private fun UserDto.toUser(courseCodes: List<String> = emptyList()): User {
    val peran = when (role.uppercase()) {
        "ADMIN", "ADMIN_TU", "ADMIN_TU_PRODI" -> Role.ADMIN
        "INSTRUCTOR", "LECTURER", "DOSEN" -> Role.INSTRUKTUR
        else -> Role.MAHASISWA
    }
    return User(
        id = userId?.toString() ?: identityNo,
        name = fullName.ifBlank { email ?: identityNo },
        identifier = identityNo,
        role = peran,
        secondary = email.orEmpty(),
        courseCodes = courseCodes,
    )
}

private fun CourseDto.toCourse(context: Context): Course = Course(
    id = courseId?.toString() ?: courseCode,
    code = courseCode,
    name = courseName,
    classDay = hariIndonesia(context, classDay),
    startTime = startTime.orEmpty(),
    endTime = endTime.orEmpty(),
    sks = sks ?: 0,
    semester = semester.orEmpty(),
)

private fun LeaveRequestDto.toRequest(context: Context): LeaveRequest {
    val mahasiswa = student?.toUser() ?: User(
        id = "",
        name = context.getString(R.string.role_mahasiswa),
        identifier = "-",
        role = Role.MAHASISWA,
    )
    val mataKuliah = course?.toCourse(context) ?: Course(
        id = "",
        code = "-",
        name = context.getString(R.string.fallback_course_missing),
        classDay = "-",
        startTime = "",
        endTime = "",
    )
    return LeaveRequest(
        id = leaveRequestId?.toString().orEmpty(),
        studentId = mahasiswa.id,
        studentName = mahasiswa.name,
        studentIdentifier = mahasiswa.identifier,
        course = mataKuliah,
        classDate = classDate,
        category = when (category.uppercase()) {
            "ACADEMIC", "AKADEMIK" -> LeaveCategory.AKADEMIK
            "EMERGENCY", "URGENT", "MENDADAK" -> LeaveCategory.MENDADAK
            "FAMILY", "KEPERLUAN_KELUARGA" -> LeaveCategory.KEPERLUAN_KELUARGA
            else -> LeaveCategory.SAKIT
        },
        reason = reason.orEmpty(),
        status = when (status.uppercase()) {
            "APPROVED", "DISETUJUI" -> LeaveStatus.DISETUJUI
            "REJECTED", "DITOLAK" -> LeaveStatus.DITOLAK
            else -> LeaveStatus.DIAJUKAN
        },
        submittedAt = formatTimestamp(submittedAt),
        note = instructorNote,
        evidenceName = evidenceName ?: if (hasEvidence || !evidencePath.isNullOrBlank()) context.getString(R.string.detail_evidence_name) else null,
        decidedBy = reviewedBy?.fullName,
        decidedAt = reviewedAt?.let(::formatTimestamp),
    )
}

private fun AuditDto.toAudit(): AuditEntry = AuditEntry(
    id = auditId?.toString().orEmpty(),
    actor = actorName ?: actor.orEmpty(),
    action = action.orEmpty(),
    detail = detail.orEmpty(),
    time = formatTimestamp(createdAt),
)

private fun hariIndonesia(context: Context, hari: String?): String {
    val daftar = context.resources.getStringArray(R.array.hari_indonesia)
    val urutan = listOf(
        "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY",
    )
    val indeks = urutan.indexOf(hari?.uppercase())
    return if (indeks >= 0) daftar[indeks] else hari.orEmpty()
}
