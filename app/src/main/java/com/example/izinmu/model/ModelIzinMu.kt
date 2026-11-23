package com.example.izinmu.model

import androidx.annotation.StringRes
import com.example.izinmu.R

const val MAX_EVIDENCE_BYTES = 5_242_880L

enum class Role(@StringRes val labelRes: Int) {
    MAHASISWA(R.string.role_mahasiswa),
    INSTRUKTUR(R.string.role_instruktur),
    ADMIN(R.string.role_admin),
}

enum class LeaveStatus(@StringRes val labelRes: Int) {
    DIAJUKAN(R.string.status_diajukan),
    DISETUJUI(R.string.status_disetujui),
    DITOLAK(R.string.status_ditolak),
}

enum class LeaveCategory(@StringRes val labelRes: Int) {
    SAKIT(R.string.kategori_sakit),
    AKADEMIK(R.string.kategori_akademik),
    MENDADAK(R.string.kategori_mendadak),
    KEPERLUAN_KELUARGA(R.string.kategori_keluarga),
}

data class User(
    val id: String,
    val name: String,
    val identifier: String,
    val role: Role,
    val studyProgram: String = "",
    val secondary: String = "",
    val courseCodes: List<String> = emptyList(),
)

data class Course(
    val id: String,
    val code: String,
    val name: String,
    val classDay: String,
    val startTime: String,
    val endTime: String,
    val sks: Int = 0,
    val semester: String = "",
)

data class LeaveRequest(
    val id: String,
    val studentId: String,
    val studentName: String,
    val studentIdentifier: String,
    val course: Course,
    val classDate: String,
    val category: LeaveCategory,
    val reason: String,
    val status: LeaveStatus,
    val submittedAt: String,
    val note: String? = null,
    val evidenceName: String? = null,
    val evidenceUri: String? = null,
    val decidedBy: String? = null,
    val decidedAt: String? = null,
)

data class AuditEntry(
    val id: String,
    val actor: String,
    val action: String,
    val detail: String,
    val time: String,
)
