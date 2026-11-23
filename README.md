# IzinMu — Sistem Perizinan Perkuliahan

Aplikasi Android untuk pengajuan izin ketidakhadiran mahasiswa per mata kuliah, peninjauan oleh Instruktur, dan pengelolaan data oleh Admin TU Prodi.

Proyek ini disusun bertahap untuk memenuhi Tugas Akhir mata kuliah Pemrograman Aplikasi Mobile (PAM). SRS, diagram, dan keputusan teknis diperlakukan sebagai acuan; kode yang belum diimplementasikan tidak diklaim selesai.

## Status

| Tahap | Isi | Status |
|---|---|---|
| 1 | Audit SRS, diagram, pedoman, dan proyek awal | Selesai |
| 2 | SRS v0.3, diagram, arsitektur, kontrak API, skema database | Selesai |
| 3 | Backend: database, migrasi, auth, RBAC, health | Berikutnya |
| 4 | Android: fondasi, sesi, navigasi, UI per role | Belum |
| 5 | Fitur CRUDS, unggah/baca bukti, keputusan | Belum |
| 6 | Integrasi R2, deployment Cloud Run, pengujian | Belum |

**Kondisi saat ini:** aplikasi Android masih berupa template Compose (`Greeting`). Belum ada backend, autentikasi, API, unggah foto, atau fitur perizinan yang berjalan. Dokumen dan migrasi database sudah disiapkan untuk Tahap 3.

## Dokumen

- [Audit awal](docs/01-audit.md)
- [Arsitektur](docs/02-architecture.md)
- [Skema database](docs/03-database.md)
- [Kontrak API](docs/04-api-contract.md)
- [Keputusan arsitektur](docs/05-decisions.md)
- SRS v0.3: `../SRS Sistem Perizinan Perkuliahan v0.3.docx`
- Diagram: `../Diagram/`

## Stack

### Android

- Kotlin 2.2.10, Java 21
- Jetpack Compose, Material 3
- MVVM, `StateFlow`
- `androidx.navigation:navigation-compose`
- OkHttp, Retrofit, Kotlin Serialization
- Coil
- Tanpa Room

### Backend (rencana Tahap 3)

- Node.js 24, Express 5, JavaScript ESM
- PostgreSQL 15+ (Supabase)
- `pg`, `jsonwebtoken`, `bcrypt`
- Cloudflare R2 melalui S3-compatible API
- Cloud Run + Secret Manager

## Struktur repositori

```text
IzinMu/
├── Diagram/                 # Sumber dan PNG Use Case, Activity, ERD
├── SRS Sistem Perizinan Perkuliahan v0.3.docx
├── TugasAkhir/
│   ├── app/                 # Aplikasi Android
│   ├── backend/
│   │   └── migrations/      # 001_init.up.sql / .down.sql
│   ├── docs/                # Audit, arsitektur, database, API, keputusan
│   ├── gradle/
│   ├── README.md
│   └── ...
└── materi/                  # Materi perkuliahan (referensi)
```

## Menjalankan Android

Prasyarat:

- Android Studio terbaru
- JDK 21
- Android SDK 37 (compileSdk/targetSdk)
- Perangkat/emulator Android 7.0+ (`minSdk 24`)

Build:

```bash
cd TugasAkhir
./gradlew assembleDebug
```

Hasil: `app/build/outputs/apk/debug/app-debug.apk`.

Menjalankan di perangkat:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Saat ini aplikasi hanya menampilkan layar template; belum ada alur login atau data.

## Backend

Backend belum diimplementasikan. Pada Tahap 3 akan tersedia:

```bash
cd backend
npm install
cp .env.example .env        # isi nilai lokal; jangan commit
npm run migrate
npm run dev
```

Variabel yang direncanakan (nama saja, tanpa nilai):

```text
NODE_ENV
PORT
DATABASE_URL
JWT_SECRET
JWT_ACCESS_TTL_SECONDS
JWT_REFRESH_TTL_SECONDS
APP_TIMEZONE
R2_ENDPOINT
R2_ACCESS_KEY_ID
R2_SECRET_ACCESS_KEY
R2_BUCKET
R2_REGION
MAX_EVIDENCE_BYTES
```

Tidak ada kredensial di repository. Jangan menaruh `DATABASE_URL`, `JWT_SECRET`, atau kunci R2 di APK, kode sumber, atau file yang di-commit.

## Database

Migrasi awal membuat enam tabel:

- `users`
- `courses`
- `course_instructors`
- `course_enrollments`
- `leave_requests`
- `refresh_tokens`

Detail kolom, constraint, indeks, dan kebijakan hapus ada di [docs/03-database.md](docs/03-database.md). Migrasi telah diuji pada PostgreSQL 15 lokal: up, constraint utama, dan down berhasil.

## Aturan produk

- Role: `STUDENT`, `INSTRUCTOR`, `ADMIN`.
- Status: `SUBMITTED` (Diajukan), `APPROVED` (Disetujui), `REJECTED` (Ditolak).
- Kategori: `SICK`, `ACADEMIC`, `EMERGENCY`, `FAMILY`.
- Pengajuan hanya H-1 sampai H menurut waktu server Asia/Jakarta dan harus sesuai hari kuliah.
- Bukti foto wajib: JPG/JPEG/PNG, maksimum 5 MB.
- Mahasiswa hanya dapat mengubah/menghapus pengajuan saat `SUBMITTED`.
- Instruktur hanya melihat/memutuskan mata kuliah yang diampu.
- Admin mengelola akun, mata kuliah, dan pemetaan; audit bersifat baca.
- Satu pengajuan per mahasiswa, mata kuliah, dan tanggal kuliah.

## Kepatuhan Pedoman PAM

| Butir | Rencana |
|---|---|
| README | Dokumen ini |
| Minimal 30 commit | Diusahakan bertahap per tahap/fitur |
| Navigator Compose | `navigation-compose` (ADR-007) |
| MVVM | ViewModel + StateFlow + repository |
| Minimal 4 entitas | 6 tabel |
| 5 fitur CRUDS | Akun, mata kuliah, pemetaan, pengajuan, keputusan |
| Tanpa Room | Tidak ada Room/database lokal |
| Simpan/baca file lokal | Cache foto sementara + pemilih kamera/galeri |

## Keamanan

- Backend adalah otoritas autentikasi, role, ownership, pemetaan, dan validasi tanggal.
- Access token 15 menit; refresh token 7 hari, disimpan sebagai hash, dirotasi.
- Bukti foto berada di bucket R2 privat; dibaca melalui backend, bukan URL publik.
- Password di-hash bcrypt; tidak pernah dikembalikan API.
- Log tidak memuat token, password, atau isi berkas.
- `.env`, `local.properties`, dan artefak build tidak di-commit.

## Roadmap

1. **Tahap 3:** backend Express, koneksi Supabase, migration runner, register/login/refresh/logout, middleware auth/RBAC, health, CLI admin.
2. **Tahap 4:** Android session store, API client, navigasi per role, layar login/register/pending, kerangka home.
3. **Tahap 5:** daftar/detail pengajuan, form + kamera/galeri, edit/hapus, keputusan instruktur, CRUD admin, pemetaan.
4. **Tahap 6:** R2, deployment Cloud Run, integration test, dokumentasi akhir dan demo.

## Lisensi dan atribusi

Proyek akademik. Materi, merek, dan layanan pihak ketiga mengikuti ketentuan masing-masing.
