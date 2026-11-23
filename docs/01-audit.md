# 01 — Audit Awal Proyek IzinMu

Tanggal audit: 23 November 2026  
Baseline: SRS v0.2 (sebelum revisi), tiga diagram, Pedoman Project Akhir PAM 2026, dan template Android `TugasAkhir`.

## 1. Ruang lingkup audit

Audit membaca:

- `SRS Sistem Perizinan Perkuliahan v0.2.docx`
- `Diagram/UseCase.puml`, `Diagram/Activity.puml`, `Diagram/ERD.mmd`, dan PNG hasil ekspor
- `PEDOMAN PROJECT AKHIR PAM 2026.pdf`
- Proyek Android `TugasAkhir` beserta konfigurasi Gradle dan riwayat Git
- `materi/Pertemuan9` sebagai contoh pola perkuliahan, bukan sumber kebenaran proyek

Tidak ada file proyek yang diubah selama audit.

## 2. Kondisi awal

| Artefak | Kondisi |
|---|---|
| SRS v0.2 | 26 kebutuhan fungsional, 9 TBD, 5 tabel inti, 3 lampiran diagram |
| Diagram | Sumber PlantUML/Mermaid dan PNG tersedia; PNG identik dengan gambar di SRS |
| Pedoman | Wajib README, minimal 30 commit, Navigator Compose, MVVM, minimal 4 entitas, 5 fitur CRUDS, tanpa Room, simpan/baca file lokal |
| Android | Template Jetpack Compose; hanya `MainActivity` dengan `Greeting` dan tiga file tema |
| Backend | Belum ada |
| Migrasi database | Belum ada |
| Dokumentasi API | Belum ada |
| README | Belum ada |
| Rahasia | Tidak ditemukan; `local.properties` hanya berisi `sdk.dir` dan di-ignore Git |
| Git | 1 commit, branch `main`, remote `0022_IzinMu` |

## 3. Temuan yang perlu diputuskan

| ID | Temuan | Keputusan |
|---|---|---|
| A-01 | Permintaan pengguna menyebut H-1 sampai H, sedangkan SRS v0.2 dan Activity Diagram hanya mengizinkan hari H | SRS dinaikkan ke v0.3; aturan menjadi H-1 sampai H |
| A-02 | Activity Diagram menyebut MySQL | Diganti menjadi PostgreSQL Supabase |
| A-03 | ERD menyebut “Cloud Storage” | Diganti menjadi Cloudflare R2 |
| A-04 | Kamus data menyebut `instructor_user_id`/`student_user_id` sebagai FK `courses` | Diperbaiki menjadi FK `users` |
| A-05 | Refresh token diwajibkan, tetapi tidak ada tabel penyimpanannya | Ditambahkan tabel `refresh_tokens` |
| A-06 | Zona waktu, batas foto, dan masa berlaku JWT masih TBD | Ditetapkan Asia/Jakarta, 5 MB, access 15 menit, refresh 7 hari |
| A-07 | Aturan satu pengajuan per tanggal kuliah masih TBD | Ditetapkan UNIQUE `(student_user_id, course_id, class_date)` |
| A-08 | Cakupan audit Admin belum eksplisit | Admin memperoleh akses baca ke seluruh pengajuan dan bukti |
| A-09 | Istilah “Navigator Compose” tidak sama dengan nama dependensi materi | Memakai `androidx.navigation:navigation-compose` |
| A-10 | Admin pertama tidak punya mekanisme pembuatan | Disiapkan CLI one-off pada Tahap 3 |

## 4. Yang belum ada dan menjadi pekerjaan tahap berikutnya

- Backend Express, konfigurasi environment, koneksi PostgreSQL, migration runner, validasi, logging, error handler.
- Autentikasi JWT, refresh token rotation, RBAC, pemeriksaan ownership dan pemetaan.
- Modul API akun, mata kuliah, pemetaan, pengajuan, bukti foto, keputusan.
- Seluruh fitur Android, termasuk sesi aman, navigasi, repository, ViewModel, dan state layar.
- Integrasi Cloudflare R2 dan endpoint baca bukti privat.
- Dockerfile, deployment Cloud Run, Supabase, dan konfigurasi Secret Manager.
- Pengujian backend/Android dan skenario demo tiga role.

## 5. Dokumen hasil tindak lanjut

- SRS v0.3: `../SRS Sistem Perizinan Perkuliahan v0.3.docx`
- Diagram terbaru: `../Diagram/`
- Kontrak API: `04-api-contract.md`
- Skema database: `03-database.md`
- Keputusan arsitektur: `05-decisions.md`
