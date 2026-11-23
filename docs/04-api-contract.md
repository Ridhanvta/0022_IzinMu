# 04 — Kontrak API IzinMu v1

Base path: `/api/v1`  
Format: JSON UTF-8; unggah bukti memakai `multipart/form-data`.  
Autentikasi: `Authorization: Bearer <access_token>`.

Dokumen ini adalah kesepakatan antara Android dan backend. Perubahan yang merusak kompatibilitas harus menaikkan versi API.

## 1. Konvensi umum

| Aspek | Aturan |
|---|---|
| ID | JSON number (`BIGINT` yang aman di JavaScript); bukan string |
| Tanggal | `YYYY-MM-DD` |
| Timestamp | ISO 8601 UTC, contoh `2026-11-23T03:12:00.000Z` |
| Zona bisnis | `Asia/Jakarta` untuk menentukan “hari ini” dan H-1 |
| Field | camelCase |
| Enum | Huruf besar, nilai tetap |
| Pagination | `page` (mulai 1), `limit` (1–100, default 20) |
| Sort | Tetap per endpoint; tidak ada parameter `sort` |
| Cache | Respons API data sensitif memakai `Cache-Control: no-store` |

## 2. Envelope respons

### 2.1 Sukses, satu objek

```json
{
  "data": {
    "leaveRequestId": 12
  }
}
```

### 2.2 Sukses, daftar

```json
{
  "data": [],
  "meta": {
    "page": 1,
    "limit": 20,
    "total": 0,
    "totalPages": 0
  }
}
```

### 2.3 Error

```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Data pengajuan tidak valid.",
    "details": [
      { "field": "classDate", "message": "Tanggal kuliah harus H-1 atau hari ini." }
    ]
  }
}
```

`details` opsional. `message` dapat ditampilkan ke pengguna; pesan teknis database tidak pernah dikirim.

## 3. Bentuk data

### 3.1 `UserRef`

```json
{
  "userId": 3,
  "identityNo": "20260001",
  "fullName": "Nama Mahasiswa",
  "role": "STUDENT"
}
```

Tidak memuat email, password, atau status akun.

### 3.2 `UserDetail`

```json
{
  "userId": 3,
  "identityNo": "20260001",
  "fullName": "Nama Mahasiswa",
  "email": "mahasiswa@example.ac.id",
  "role": "STUDENT",
  "isActive": false,
  "createdAt": "2026-11-20T08:00:00.000Z",
  "updatedAt": "2026-11-23T02:00:00.000Z"
}
```

Dipakai untuk diri sendiri dan untuk Admin.

### 3.3 `CourseRef`

```json
{
  "courseId": 1,
  "courseCode": "TI101",
  "courseName": "Pemrograman Mobile",
  "classDay": "MONDAY"
}
```

### 3.4 `CourseDetail`

`CourseRef` ditambah:

```json
{
  "instructors": [
    {
      "userId": 2,
      "identityNo": "I001",
      "fullName": "Nama Dosen",
      "role": "INSTRUCTOR"
    }
  ]
}
```

### 3.5 `LeaveRequestSummary`

```json
{
  "leaveRequestId": 12,
  "student": {
    "userId": 3,
    "identityNo": "20260001",
    "fullName": "Nama Mahasiswa",
    "role": "STUDENT"
  },
  "course": {
    "courseId": 1,
    "courseCode": "TI101",
    "courseName": "Pemrograman Mobile",
    "classDay": "MONDAY"
  },
  "category": "SICK",
  "classDate": "2026-11-24",
  "status": "SUBMITTED",
  "submittedAt": "2026-11-23T03:12:00.000Z",
  "reviewedAt": null,
  "hasEvidence": true
}
```

### 3.6 `LeaveRequestDetail`

`LeaveRequestSummary` ditambah:

```json
{
  "instructorNote": null,
  "reviewedBy": null,
  "updatedAt": "2026-11-23T03:12:00.000Z",
  "evidencePath": "/api/v1/leave-requests/12/evidence"
}
```

`reviewedBy` memakai `UserRef`. `evidencePath` bukan URL publik dan tetap memerlukan header autentikasi.

### 3.7 `Mapping`

```json
{
  "mappingId": 5,
  "course": {
    "courseId": 1,
    "courseCode": "TI101",
    "courseName": "Pemrograman Mobile",
    "classDay": "MONDAY"
  },
  "user": {
    "userId": 2,
    "identityNo": "I001",
    "fullName": "Nama Dosen",
    "role": "INSTRUCTOR"
  },
  "createdAt": "2026-11-20T08:00:00.000Z"
}
```

Untuk pemetaan mahasiswa, `user.role` bernilai `STUDENT`.

## 4. Endpoint publik

| Method | Path | Keterangan |
|---|---|---|
| `GET` | `/health` | Status backend dan database |
| `GET` | `/api/v1/meta/time` | Waktu server dan tanggal Jakarta |
| `POST` | `/api/v1/auth/register` | Registrasi mandiri mahasiswa |
| `POST` | `/api/v1/auth/login` | Login |
| `POST` | `/api/v1/auth/refresh` | Rotasi refresh token |
| `POST` | `/api/v1/auth/logout` | Cabut refresh token |

`/health` tidak memakai envelope `data`:

```json
{
  "status": "ok",
  "database": "up",
  "timestamp": "2026-11-23T03:12:00.000Z"
}
```

Jika database tidak dapat dijangkau: HTTP `503` dengan `status: "degraded"`, `database: "down"`.

## 5. Autentikasi

### 5.1 Registrasi mahasiswa

```http
POST /api/v1/auth/register
Content-Type: application/json
```

```json
{
  "identityNo": "20260001",
  "fullName": "Nama Mahasiswa",
  "email": "mahasiswa@example.ac.id",
  "password": "rahasia123"
}
```

Respons `201`:

```json
{
  "data": {
    "user": {
      "userId": 15,
      "identityNo": "20260001",
      "fullName": "Nama Mahasiswa",
      "email": "mahasiswa@example.ac.id",
      "role": "STUDENT",
      "isActive": false,
      "createdAt": "2026-11-23T03:12:00.000Z",
      "updatedAt": "2026-11-23T03:12:00.000Z"
    },
    "requiresActivation": true
  }
}
```

Aturan:

- Role selalu `STUDENT`.
- Akun dibuat `isActive = false`.
- Tidak ada token pada respons; mahasiswa harus menunggu aktivasi Admin.
- Email dinormalkan lowercase.
- `identityNo` unik.

Error: `VALIDATION_ERROR` (422), `EMAIL_ALREADY_USED` (409), `IDENTITY_NO_ALREADY_USED` (409), `RATE_LIMITED` (429).

### 5.2 Login

```http
POST /api/v1/auth/login
Content-Type: application/json
```

```json
{
  "email": "mahasiswa@example.ac.id",
  "password": "rahasia123"
}
```

Respons `200`:

```json
{
  "data": {
    "accessToken": "<jwt>",
    "refreshToken": "<opaque-token>",
    "tokenType": "Bearer",
    "expiresIn": 900,
    "user": {
      "userId": 15,
      "identityNo": "20260001",
      "fullName": "Nama Mahasiswa",
      "email": "mahasiswa@example.ac.id",
      "role": "STUDENT",
      "isActive": true,
      "createdAt": "2026-11-23T03:12:00.000Z",
      "updatedAt": "2026-11-23T03:12:00.000Z"
    }
  }
}
```

`expiresIn` adalah umur access token dalam detik. Refresh token berlaku 7 hari.

Error:

- `INVALID_CREDENTIALS` (401) — email tidak dikenal atau password salah; tidak dibedakan.
- `ACCOUNT_INACTIVE` (403) — akun belum/tidak aktif.
- `VALIDATION_ERROR` (422).
- `RATE_LIMITED` (429).

### 5.3 Refresh

```http
POST /api/v1/auth/refresh
Content-Type: application/json
```

```json
{
  "refreshToken": "<opaque-token>"
}
```

Respons `200` sama dengan login (`accessToken`, `refreshToken`, `tokenType`, `expiresIn`, `user`).

Aturan:

- Token lama dicabut dan diganti token baru dalam satu transaksi.
- Token yang sudah dicabut tidak dapat dipakai lagi.
- Jika token yang sudah dicabut dipakai kembali, server mencabut seluruh refresh token aktif pengguna dan mengembalikan `401 INVALID_REFRESH_TOKEN`. Klien harus login ulang.
- Access token lama tetap berlaku sampai kedaluwarsa alami; tidak ada daftar hitam access token.

Error: `INVALID_REFRESH_TOKEN` (401), `ACCOUNT_INACTIVE` (403), `VALIDATION_ERROR` (422), `RATE_LIMITED` (429).

### 5.4 Logout

```http
POST /api/v1/auth/logout
Content-Type: application/json
```

```json
{
  "refreshToken": "<opaque-token>"
}
```

Respons `204 No Content`.

Aturan:

- Mencabut refresh token berdasarkan hash.
- Idempotent: token yang sudah dicabut/tidak ditemukan tetap `204`.
- Access token tidak dapat dicabut; klien menghapusnya dari penyimpanan.

### 5.5 Profil sendiri

```http
GET /api/v1/auth/me
Authorization: Bearer <access_token>
```

Respons `200`:

```json
{
  "data": {
    "userId": 15,
    "identityNo": "20260001",
    "fullName": "Nama Mahasiswa",
    "email": "mahasiswa@example.ac.id",
    "role": "STUDENT",
    "isActive": true,
    "createdAt": "2026-11-23T03:12:00.000Z",
    "updatedAt": "2026-11-23T03:12:00.000Z"
  }
}
```

## 6. Waktu server

```http
GET /api/v1/meta/time
```

Respons `200`:

```json
{
  "data": {
    "serverTime": "2026-11-23T03:12:00.000Z",
    "serverDate": "2026-11-23",
    "timezone": "Asia/Jakarta"
  }
}
```

Aturan:

- `serverDate` adalah tanggal kalender di Asia/Jakarta.
- Endpoint publik, tetapi `Cache-Control: no-store`.
- Android memanggil endpoint ini sebelum menentukan tanggal yang dapat dipilih dan sebelum submit.
- Backend tetap menghitung ulang saat menerima pengajuan.

## 7. Mata kuliah

### 7.1 Daftar mata kuliah

```http
GET /api/v1/courses?q=&classDay=&page=1&limit=20
Authorization: Bearer <access_token>
```

Cakupan otomatis:

- `STUDENT`: hanya mata kuliah yang diikuti.
- `INSTRUCTOR`: hanya mata kuliah yang diampu.
- `ADMIN`: semua mata kuliah.

Filter:

- `q`: kode atau nama, maksimum 100 karakter.
- `classDay`: salah satu nama hari.
- `page`, `limit`.

Respons `200`:

```json
{
  "data": [
    {
      "courseId": 1,
      "courseCode": "TI101",
      "courseName": "Pemrograman Mobile",
      "classDay": "MONDAY"
    }
  ],
  "meta": {
    "page": 1,
    "limit": 20,
    "total": 1,
    "totalPages": 1
  }
}
```

### 7.2 Detail mata kuliah

```http
GET /api/v1/courses/{courseId}
Authorization: Bearer <access_token>
```

Respons `200`:

```json
{
  "data": {
    "courseId": 1,
    "courseCode": "TI101",
    "courseName": "Pemrograman Mobile",
    "classDay": "MONDAY",
    "instructors": [
      {
        "userId": 2,
        "identityNo": "I001",
        "fullName": "Nama Dosen",
        "role": "INSTRUCTOR"
      }
    ]
  }
}
```

Aturan akses:

- Mahasiswa harus terdaftar pada mata kuliah; jika tidak, `403 COURSE_NOT_ENROLLED`.
- Instruktur harus mengampu; jika tidak, `403 COURSE_NOT_TAUGHT`.
- Admin bebas.
- Jika ID tidak ada: `404 NOT_FOUND`.

## 8. Pengajuan izin

### 8.1 Daftar pengajuan

```http
GET /api/v1/leave-requests?status=&category=&courseId=&studentId=&from=&to=&q=&page=1&limit=20
Authorization: Bearer <access_token>
```

Cakupan:

- `STUDENT`: hanya miliknya.
- `INSTRUCTOR`: hanya pengajuan pada mata kuliah yang diampu.
- `ADMIN`: semua.

Filter:

| Parameter | Arti |
|---|---|
| `status` | `SUBMITTED`, `APPROVED`, `REJECTED` |
| `category` | `SICK`, `ACADEMIC`, `EMERGENCY`, `FAMILY` |
| `courseId` | Filter mata kuliah; harus dalam cakupan |
| `studentId` | Filter mahasiswa; Admin/Instruktur boleh, mahasiswa hanya dirinya |
| `from`, `to` | Rentang `classDate`, inklusif, `YYYY-MM-DD` |
| `q` | Cari nama/NIM mahasiswa, kode/nama mata kuliah |
| `page`, `limit` | Pagination |

Urutan default: `submittedAt DESC`.

Respons `200`: daftar `LeaveRequestSummary` + `meta`.

Aturan:

- Jika mahasiswa mengirim `studentId` yang bukan dirinya: `403 FORBIDDEN`.
- Jika `courseId` di luar cakupan: `403 COURSE_NOT_ENROLLED` / `403 COURSE_NOT_TAUGHT`.
- Instruktur yang memfilter mahasiswa di luar kelasnya mendapat daftar kosong, bukan data.
- `from > to`: `422 VALIDATION_ERROR`.

### 8.2 Detail pengajuan

```http
GET /api/v1/leave-requests/{id}
Authorization: Bearer <access_token>
```

Respons `200`: `LeaveRequestDetail`.

Aturan:

- Mahasiswa hanya miliknya.
- Instruktur hanya pada mata kuliah yang diampu.
- Admin bebas.
- Jika ada tetapi tidak berhak: `403 FORBIDDEN`.
- Jika tidak ada: `404 NOT_FOUND`.

### 8.3 Baca bukti foto

```http
GET /api/v1/leave-requests/{id}/evidence
Authorization: Bearer <access_token>
```

Respons `200`:

- Body biner gambar.
- `Content-Type: image/jpeg` atau `image/png` dari metadata objek R2.
- `Content-Disposition: inline`.
- `Cache-Control: private, no-store`.

Aturan:

- Memakai pemeriksaan akses yang sama dengan detail.
- Tidak ada URL R2 atau presigned URL di respons JSON.
- Jika objek hilang di R2: `404 EVIDENCE_NOT_FOUND`.
- Rentang byte (`Range`) tidak wajib pada versi ini.

### 8.4 Buat pengajuan

```http
POST /api/v1/leave-requests
Authorization: Bearer <access_token>
Content-Type: multipart/form-data
```

Field:

| Field | Tipe | Wajib | Aturan |
|---|---|---|---|
| `courseId` | text | ya | Mata kuliah yang diikuti |
| `category` | text | ya | Salah satu dari empat kategori |
| `classDate` | text | ya | `YYYY-MM-DD`, H-1 atau H menurut server |
| `evidence` | file | ya | Tepat satu, JPG/JPEG/PNG, maksimum 5 MB |

Contoh:

```bash
curl -X POST "$BASE_URL/api/v1/leave-requests" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -F "courseId=1" \
  -F "category=SICK" \
  -F "classDate=2026-11-24" \
  -F "evidence=@/path/photo.jpg;type=image/jpeg"
```

Respons `201`: `LeaveRequestDetail` dengan `status: "SUBMITTED"`.

Validasi server, berurutan:

1. Role harus `STUDENT` dan akun aktif.
2. `courseId` harus ada dan mahasiswa terdaftar.
3. `category` valid.
4. `classDate` valid dan berada pada H-1/H menurut tanggal server Jakarta.
5. Hari `classDate` harus sama dengan `courses.class_day`.
6. Belum ada pengajuan untuk `(student, course, classDate)`.
7. Tepat satu `evidence`; MIME/ekstensi/magic byte valid; ukuran ≤ 5 MB.
8. Unggah ke R2, lalu insert database.

Error:

- `VALIDATION_ERROR` (422)
- `COURSE_NOT_ENROLLED` (403)
- `CLASS_DATE_NOT_ALLOWED` (422)
- `CLASS_DAY_MISMATCH` (422)
- `DUPLICATE_REQUEST` (409)
- `EVIDENCE_REQUIRED` (422)
- `EVIDENCE_TYPE_NOT_ALLOWED` (415)
- `EVIDENCE_TOO_LARGE` (413)
- `PAYLOAD_TOO_LARGE` (413)
- `STORAGE_UNAVAILABLE` (502)

Jika unggah R2 berhasil tetapi insert gagal, backend menghapus objek tersebut. Jika penghapusan gagal, objek dicatat untuk pembersihan (TBD-10).

### 8.5 Ubah pengajuan

```http
PATCH /api/v1/leave-requests/{id}
Authorization: Bearer <access_token>
Content-Type: multipart/form-data
```

Field opsional (minimal satu):

| Field | Tipe | Aturan |
|---|---|---|
| `category` | text | Kategori valid |
| `classDate` | text | Jika berubah, harus lolos aturan H-1/H dan hari kuliah |
| `evidence` | file | Pengganti bukti; JPG/JPEG/PNG ≤ 5 MB |

`courseId` tidak boleh diubah. Jika dikirim dan berbeda: `422 COURSE_IMMUTABLE`.

Aturan:

- Hanya pemilik.
- Status harus `SUBMITTED`.
- Jika `classDate` tidak berubah, validasi jendela tidak diulang.
- Jika `classDate` berubah, aturan tanggal dan `class_day` berlaku.
- Bukti baru diunggah lebih dulu; setelah database diperbarui, bukti lama dihapus.
- Jika tidak ada field: `422 VALIDATION_ERROR`.

Respons `200`: `LeaveRequestDetail` terbaru.

Error:

- `FORBIDDEN` (403)
- `REQUEST_NOT_PENDING` (409)
- `COURSE_IMMUTABLE` (422)
- `CLASS_DATE_NOT_ALLOWED` (422)
- `CLASS_DAY_MISMATCH` (422)
- `DUPLICATE_REQUEST` (409)
- `EVIDENCE_TYPE_NOT_ALLOWED` (415)
- `EVIDENCE_TOO_LARGE` (413)
- `STORAGE_UNAVAILABLE` (502)

### 8.6 Hapus pengajuan

```http
DELETE /api/v1/leave-requests/{id}
Authorization: Bearer <access_token>
```

Respons `204 No Content`.

Aturan:

- Hanya pemilik.
- Status harus `SUBMITTED`.
- Baris database dihapus; objek R2 dihapus best effort setelahnya.
- Jika objek R2 gagal dihapus, operasi tetap sukses dan kegagalan dicatat.

Error: `FORBIDDEN` (403), `REQUEST_NOT_PENDING` (409), `NOT_FOUND` (404).

## 9. Keputusan instruktur

```http
POST /api/v1/leave-requests/{id}/decision
Authorization: Bearer <access_token>
Content-Type: application/json
```

```json
{
  "decision": "APPROVED",
  "note": "Bukti sesuai, disetujui."
}
```

Field:

| Field | Wajib | Aturan |
|---|---|---|
| `decision` | ya | `APPROVED` atau `REJECTED` |
| `note` | tidak | String; spasi di-trim; kosong menjadi `null` |

Respons `200`: `LeaveRequestDetail` dengan status final, `reviewedBy`, dan `reviewedAt`.

Aturan:

- Hanya `INSTRUCTOR` yang mengampu `course_id` pengajuan.
- Status harus `SUBMITTED`.
- `reviewed_by` diisi instruktur yang memutuskan.
- `reviewed_at` diisi `now()` server.
- Update atomik `WHERE status = 'SUBMITTED'`; jika tidak ada baris berubah, `409 REQUEST_NOT_PENDING`.
- Mahasiswa tidak dapat mengubah/menghapus setelah keputusan.
- Admin tidak dapat memutuskan.

Error:

- `FORBIDDEN` (403)
- `COURSE_NOT_TAUGHT` (403)
- `REQUEST_NOT_PENDING` (409)
- `VALIDATION_ERROR` (422)
- `NOT_FOUND` (404)

## 10. Admin — akun

Semua endpoint di bawah hanya untuk `ADMIN`.

### 10.1 Daftar akun

```http
GET /api/v1/admin/users?role=&isActive=&q=&page=1&limit=20
```

Respons `200`: daftar `UserDetail` + `meta`.

Filter:

- `role`: `ADMIN`, `STUDENT`, `INSTRUCTOR`.
- `isActive`: `true`/`false`.
- `q`: NIM/NIDN, nama, atau email.

### 10.2 Buat akun

```http
POST /api/v1/admin/users
Content-Type: application/json
```

```json
{
  "identityNo": "I001",
  "fullName": "Nama Dosen",
  "email": "dosen@example.ac.id",
  "password": "rahasia123",
  "role": "INSTRUCTOR"
}
```

Aturan:

- `role` hanya `ADMIN` atau `INSTRUCTOR`; `STUDENT` ditolak `422 VALIDATION_ERROR`.
- Akun langsung aktif (`isActive = true`).
- Password di-hash; tidak dikembalikan.
- Email/identity unik.

Respons `201`: `UserDetail`.

Error: `EMAIL_ALREADY_USED` (409), `IDENTITY_NO_ALREADY_USED` (409), `VALIDATION_ERROR` (422).

### 10.3 Detail akun

```http
GET /api/v1/admin/users/{id}
```

Respons `200`: `UserDetail`.

### 10.4 Ubah profil akun

```http
PATCH /api/v1/admin/users/{id}
Content-Type: application/json
```

Field opsional:

```json
{
  "identityNo": "I001",
  "fullName": "Nama Dosen Baru",
  "email": "dosen.baru@example.ac.id"
}
```

Aturan:

- `role` tidak dapat diubah: `422 ROLE_IMMUTABLE`.
- `password` tidak diubah melalui endpoint ini.
- `isActive` tidak diubah melalui endpoint ini; gunakan aktivasi/nonaktifkan.
- Email/identity unik.

Respons `200`: `UserDetail`.

### 10.5 Nonaktifkan akun

```http
DELETE /api/v1/admin/users/{id}
```

Respons `204 No Content`.

Aturan:

- Ini adalah **soft delete**: `is_active = FALSE`; baris tidak dihapus.
- Seluruh refresh token aktif pengguna dicabut.
- Admin tidak dapat menonaktifkan dirinya sendiri: `409 CANNOT_DEACTIVATE_SELF`.
- Jika target adalah Admin aktif terakhir: `409 LAST_ADMIN_PROTECTED`.
- Akun yang sudah nonaktif tetap `204` (idempotent).
- Pengajuan `SUBMITTED` pada kelas yang diampu tidak otomatis ditolak; Admin perlu memetakan instruktur pengganti agar pengajuan dapat diproses.

### 10.6 Aktifkan akun

```http
POST /api/v1/admin/users/{id}/activate
```

Respons `200`: `UserDetail` dengan `isActive: true`.

Aturan:

- Mengaktifkan akun nonaktif, termasuk mahasiswa hasil registrasi.
- Jika sudah aktif, tetap `200`.
- Tidak membuat token baru; pengguna harus login.

## 11. Admin — mata kuliah

Daftar dan detail mata kuliah untuk Admin memakai `GET /api/v1/courses` dan `GET /api/v1/courses/{courseId}` pada Bagian 7; Admin melihat seluruh data. Endpoint di bawah hanya untuk perubahan.

### 11.1 Buat mata kuliah

```http
POST /api/v1/admin/courses
Content-Type: application/json
```

```json
{
  "courseCode": "TI101",
  "courseName": "Pemrograman Mobile",
  "classDay": "MONDAY"
}
```

Respons `201`: `CourseDetail` (daftar instruktur kosong).

Aturan:

- `courseCode` dinormalkan uppercase dan unik.
- `classDay` salah satu nama hari.
- `courseName` non-blank.

Error: `COURSE_CODE_ALREADY_USED` (409), `VALIDATION_ERROR` (422).

### 11.2 Ubah mata kuliah

```http
PATCH /api/v1/admin/courses/{id}
Content-Type: application/json
```

Field opsional: `courseCode`, `courseName`, `classDay`.

Respons `200`: `CourseDetail`.

Aturan:

- Perubahan `classDay` tidak mengubah pengajuan lama.
- Kode unik.

### 11.3 Hapus mata kuliah

```http
DELETE /api/v1/admin/courses/{id}
```

Respons `204 No Content`.

Aturan:

- Hanya boleh jika tidak ada pemetaan instruktur/mahasiswa dan tidak ada pengajuan.
- Jika masih direferensikan: `409 COURSE_IN_USE` dengan `details` berisi jumlah referensi.
- Tidak ada soft delete untuk mata kuliah.

## 12. Admin — pemetaan

### 12.1 Daftar instruktur mata kuliah

```http
GET /api/v1/admin/courses/{courseId}/instructors
```

Respons `200`: daftar `Mapping` + `meta`.

### 12.2 Tambah instruktur

```http
POST /api/v1/admin/courses/{courseId}/instructors
Content-Type: application/json
```

```json
{
  "instructorUserId": 2
}
```

Respons `201`: `Mapping`.

Aturan:

- User harus ada, role `INSTRUCTOR`, dan aktif.
- Tidak boleh duplikat: `409 MAPPING_ALREADY_EXISTS`.
- Course harus ada.

### 12.3 Hapus instruktur

```http
DELETE /api/v1/admin/courses/{courseId}/instructors/{mappingId}
```

Respons `204 No Content`.

Aturan:

- `mappingId` harus milik `courseId`.
- Penghapusan tidak mengubah pengajuan lama; instruktur kehilangan akses pada permintaan berikutnya.
- Jika masih ada pengajuan `SUBMITTED`, Admin sebaiknya menambahkan instruktur pengganti sebelum atau sesudah penghapusan.

### 12.4 Daftar mahasiswa mata kuliah

```http
GET /api/v1/admin/courses/{courseId}/enrollments
```

Respons `200`: daftar `Mapping` + `meta`.

### 12.5 Tambah mahasiswa

```http
POST /api/v1/admin/courses/{courseId}/enrollments
Content-Type: application/json
```

```json
{
  "studentUserId": 3
}
```

Respons `201`: `Mapping`.

Aturan:

- User harus ada, role `STUDENT`, dan aktif.
- Tidak boleh duplikat: `409 MAPPING_ALREADY_EXISTS`.
- Mahasiswa nonaktif tidak dapat dipetakan sampai diaktifkan.
- Penghapusan pemetaan tidak menghapus pengajuan lama; mahasiswa tetap dapat membaca riwayat miliknya, tetapi tidak dapat membuat pengajuan baru untuk mata kuliah tersebut.

### 12.6 Hapus mahasiswa

```http
DELETE /api/v1/admin/courses/{courseId}/enrollments/{mappingId}
```

Respons `204 No Content`.

### 12.7 Pencarian pemetaan lintas mata kuliah

```http
GET /api/v1/admin/mappings?kind=instructor&q=&page=1&limit=20
```

`kind` wajib: `instructor` atau `student`.

Respons `200`: daftar `Mapping` + `meta`.

Kegunaan: Admin dapat mencari “di mata kuliah mana dosen X mengajar” atau “mata kuliah apa saja yang diambil mahasiswa Y”.

## 13. Admin — audit

Admin memakai endpoint yang sama:

- `GET /api/v1/leave-requests` — seluruh pengajuan, dengan filter.
- `GET /api/v1/leave-requests/{id}` — detail.
- `GET /api/v1/leave-requests/{id}/evidence` — bukti.

Admin **tidak** memiliki endpoint untuk:

- mengubah kategori/tanggal pengajuan,
- menghapus pengajuan,
- menyetujui/menolak,
- mengubah catatan instruktur.

## 14. Matriks akses

| Operasi | Mahasiswa | Instruktur | Admin |
|---|---|---|---|
| Registrasi mandiri | ya | tidak | tidak |
| Login/refresh/logout | ya | ya | ya |
| Lihat profil sendiri | ya | ya | ya |
| Lihat mata kuliah | yang diikuti | yang diampu | semua |
| Lihat pengajuan | miliknya | kelas yang diampu | semua |
| Buat pengajuan | ya | tidak | tidak |
| Ubah/hapus pengajuan | miliknya, `SUBMITTED` | tidak | tidak |
| Keputusan | tidak | kelas yang diampu | tidak |
| Kelola akun | tidak | tidak | ya |
| Kelola mata kuliah | tidak | tidak | ya |
| Kelola pemetaan | tidak | tidak | ya |
| Baca bukti | miliknya | kelas yang diampu | semua |

## 15. Validasi input

| Field | Aturan |
|---|---|
| `identityNo` | 1–30 karakter setelah trim |
| `fullName` | 1–150 karakter setelah trim |
| `email` | Format email, maksimum 254 karakter, lowercase |
| `password` | 8–72 karakter |
| `courseCode` | 1–20 karakter, uppercase |
| `courseName` | 1–150 karakter |
| `classDay` | `MONDAY` … `SUNDAY` |
| `category` | `SICK`, `ACADEMIC`, `EMERGENCY`, `FAMILY` |
| `status` | `SUBMITTED`, `APPROVED`, `REJECTED` |
| `classDate` | `YYYY-MM-DD` valid |
| `decision` | `APPROVED`, `REJECTED` |
| `note` | String; trim; boleh kosong |
| `page` | ≥ 1 |
| `limit` | 1–100; default 20 |
| `q` | Maksimum 100 karakter; trim |

Batas body:

- JSON: 1 MB.
- Multipart: 6 MB total (5 MB berkas + field).
- Hanya satu berkas `evidence`.

## 16. Aturan multipart

- Nama field: `courseId`, `category`, `classDate`, `evidence`.
- `evidence` harus berupa file; field teks tidak diterima.
- Ekstensi yang diterima: `.jpg`, `.jpeg`, `.png`.
- MIME yang diterima: `image/jpeg`, `image/png`.
- Backend memeriksa magic byte:
  - JPEG: `FF D8 FF`.
  - PNG: `89 50 4E 47 0D 0A 1A 0A`.
- Nama file asli tidak disimpan dan tidak dipakai sebagai object key.
- Object key: `leave-requests/{studentUserId}/{uuid}.{ext}`.
- Metadata R2 menyimpan `Content-Type` yang sudah divalidasi.

## 17. Kode error

| Kode | HTTP | Kapan |
|---|---|---|
| `BAD_REQUEST` | 400 | JSON/multipart tidak dapat diparse |
| `VALIDATION_ERROR` | 422 | Field tidak valid |
| `UNAUTHORIZED` | 401 | Header Authorization tidak ada/tidak valid |
| `TOKEN_EXPIRED` | 401 | Access token kedaluwarsa |
| `INVALID_CREDENTIALS` | 401 | Login gagal |
| `INVALID_REFRESH_TOKEN` | 401 | Refresh token tidak valid/dicabut |
| `ACCOUNT_INACTIVE` | 403 | Akun nonaktif |
| `FORBIDDEN` | 403 | Role/ownership tidak mengizinkan |
| `COURSE_NOT_ENROLLED` | 403 | Mahasiswa tidak terdaftar pada mata kuliah |
| `COURSE_NOT_TAUGHT` | 403 | Instruktur tidak mengampu mata kuliah |
| `NOT_FOUND` | 404 | Resource tidak ada |
| `EVIDENCE_NOT_FOUND` | 404 | Objek bukti tidak ada di R2 |
| `EMAIL_ALREADY_USED` | 409 | Email sudah dipakai |
| `IDENTITY_NO_ALREADY_USED` | 409 | NIM/NIDN sudah dipakai |
| `COURSE_CODE_ALREADY_USED` | 409 | Kode mata kuliah sudah dipakai |
| `MAPPING_ALREADY_EXISTS` | 409 | Pemetaan ganda |
| `DUPLICATE_REQUEST` | 409 | Pengajuan untuk tanggal yang sama sudah ada |
| `REQUEST_NOT_PENDING` | 409 | Pengajuan sudah final |
| `COURSE_IN_USE` | 409 | Mata kuliah masih direferensikan |
| `CANNOT_DEACTIVATE_SELF` | 409 | Admin menonaktifkan dirinya |
| `LAST_ADMIN_PROTECTED` | 409 | Akan menghilangkan Admin aktif terakhir |
| `ROLE_IMMUTABLE` | 422 | Percobaan mengubah role |
| `COURSE_IMMUTABLE` | 422 | Percobaan mengubah mata kuliah pengajuan |
| `CLASS_DATE_NOT_ALLOWED` | 422 | Tanggal di luar H-1/H |
| `CLASS_DAY_MISMATCH` | 422 | Tanggal bukan hari kuliah |
| `EVIDENCE_REQUIRED` | 422 | Bukti tidak ada |
| `EVIDENCE_TOO_LARGE` | 413 | Bukti > 5 MB |
| `EVIDENCE_TYPE_NOT_ALLOWED` | 415 | Bukan JPG/JPEG/PNG |
| `PAYLOAD_TOO_LARGE` | 413 | Body melebihi batas |
| `UNSUPPORTED_MEDIA_TYPE` | 415 | Content-Type tidak didukung |
| `RATE_LIMITED` | 429 | Terlalu banyak permintaan |
| `STORAGE_UNAVAILABLE` | 502 | R2 tidak dapat diakses |
| `SERVICE_UNAVAILABLE` | 503 | Database/backend tidak siap |
| `INTERNAL_ERROR` | 500 | Kesalahan tak terduga |

## 18. Status HTTP

| Status | Penggunaan |
|---|---|
| `200` | Baca/ubah sukses |
| `201` | Resource dibuat |
| `204` | Hapus/logout tanpa body |
| `400` | Body tidak dapat diparse |
| `401` | Autentikasi gagal |
| `403` | Terautentikasi tetapi tidak berhak |
| `404` | Resource tidak ada |
| `409` | Konflik state/duplikasi |
| `413` | Body/berkas terlalu besar |
| `415` | Tipe media tidak didukung |
| `422` | Validasi field gagal |
| `429` | Rate limit |
| `500` | Kesalahan server |
| `502` | Ketergantungan eksternal (R2) gagal |
| `503` | Layanan/database tidak tersedia |

## 19. Keamanan dan cache

- Semua endpoint data memakai `Cache-Control: no-store`.
- Bukti memakai `Cache-Control: private, no-store`.
- Tidak ada token di query string.
- Tidak ada presigned URL R2.
- CORS tidak diaktifkan untuk browser; aplikasi Android bukan browser.
- Rate limit awal:
  - `POST /auth/login`, `/auth/register`: 10 permintaan/menit/IP.
  - `POST /auth/refresh`: 30 permintaan/menit/IP.
  - `POST/PATCH /leave-requests`: 20 permintaan/menit/pengguna.
- `429` menyertakan `Retry-After` (detik).
- Log tidak memuat password, token, cookie, atau isi berkas.
- `Authorization` tidak boleh dicatat.

## 20. Contoh alur

### 20.1 Mahasiswa membuat pengajuan

1. `GET /meta/time` → `serverDate = 2026-11-22` (Minggu).
2. `GET /courses` → mata kuliah `TI101`, `classDay = MONDAY`.
3. Tanggal yang dapat dipilih: `2026-11-22` (H, Minggu) tidak cocok dengan `classDay`; `2026-11-23` (H-1, Senin) valid. Tanggal `2026-11-24` (Selasa) di luar jendela H-1/H.
4. `POST /leave-requests` multipart dengan `classDate=2026-11-23`.
5. Respons `201` dengan status `SUBMITTED`.
6. `GET /leave-requests/{id}/evidence` untuk pratinjau.

### 20.2 Instruktur memutuskan

1. `GET /leave-requests?status=SUBMITTED` → hanya kelas yang diampu.
2. `GET /leave-requests/{id}` dan bukti.
3. `POST /leave-requests/{id}/decision` dengan `APPROVED`/`REJECTED`.
4. Respons detail final; mahasiswa tidak dapat mengubah lagi.

### 20.3 Admin mengaudit

1. `GET /admin/users?role=STUDENT&isActive=false` → aktivasi.
2. `POST /admin/users/{id}/activate`.
3. `GET /admin/mappings?kind=student&q=20260001`.
4. `GET /leave-requests?status=APPROVED&from=2026-11-01&to=2026-11-30`.
5. `GET /leave-requests/{id}/evidence`.

## 21. Catatan kompatibilitas

- Menambah field pada respons adalah perubahan kompatibel; klien harus mengabaikan field yang tidak dikenal.
- Menghapus/mengganti nama field, mengubah tipe, atau mengubah arti status adalah perubahan merusak.
- Perubahan merusak memerlukan `/api/v2` atau versi aplikasi baru yang disepakati.
- Kontrak ini belum menghasilkan file OpenAPI; implementasi Tahap 3 harus mengikuti dokumen ini dan diuji dengan integration test.
