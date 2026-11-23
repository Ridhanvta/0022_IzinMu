# 05 — Keputusan Arsitektur dan Produk

Status: keputusan Tahap 1–2, disetujui untuk dijadikan dasar implementasi.  
Tanggal: 23 November 2026.  
Rujukan: SRS v0.3 dan Pedoman Project Akhir PAM 2026.

## ADR-001 — Jendela pengajuan H-1 sampai H

**Status:** Accepted

**Konteks:** SRS v0.2 hanya mengizinkan pengajuan pada hari kuliah yang sama. Permintaan pengguna dan kebutuhan realistis mahasiswa adalah mengajukan sehari sebelumnya.

**Keputusan:** Pengajuan diterima bila `classDate` sama dengan tanggal server (H) atau satu hari setelah tanggal server (H-1), dan `classDate` jatuh pada `courses.class_day`. Tanggal server dihitung di Asia/Jakarta.

**Konsekuensi:**

- SRS dinaikkan ke v0.3; REQ-8, ruang lingkup, Activity Diagram, dan TBD diperbarui.
- Backend memvalidasi ulang; aplikasi hanya menampilkan tanggal yang valid.
- Pengajuan untuk tanggal yang sudah lewat ditolak.
- Perubahan tanggal saat edit harus lolos aturan yang sama.

## ADR-002 — Tabel `refresh_tokens` dan rotasi token

**Status:** Accepted

**Konteks:** SRS mewajibkan access token berumur pendek dan refresh token, tetapi tidak menyediakan tempat penyimpanan atau mekanisme pencabutan.

**Keputusan:** Tambahkan tabel `refresh_tokens` dengan hash SHA-256, masa berlaku, `revoked_at`, `replaced_by_token_id`, dan `user_agent`. Setiap refresh merotasi token; logout mencabut token.

**Konsekuensi:**

- Token mentah tidak pernah disimpan di database.
- Reuse token yang sudah dicabut dianggap indikasi pencurian; semua sesi pengguna dicabut.
- Dibutuhkan pembersihan token kedaluwarsa (TBD).
- ERD, kamus data, dan SRS diperbarui menjadi enam tabel.

## ADR-003 — Zona waktu operasional Asia/Jakarta

**Status:** Accepted

**Konteks:** TBD-3 belum menetapkan zona waktu; “hari yang sama” bergantung pada zona.

**Keputusan:** `APP_TIMEZONE=Asia/Jakarta` (WIB). Tanggal bisnis dihitung di zona ini; timestamp audit disimpan UTC.

**Konsekuensi:**

- Perangkat dengan zona lain tetap mengikuti aturan kampus.
- Backend memakai `serverDate`, bukan tanggal perangkat.
- Dokumentasi dan pengujian memakai tanggal Jakarta.

## ADR-004 — Bukti foto maksimum 5 MB, JPG/JPEG/PNG

**Status:** Accepted

**Konteks:** TBD-4 belum menetapkan batas ukuran/tipe; SRS mewajibkan foto.

**Keputusan:** Tepat satu berkas JPG/JPEG/PNG, maksimum 5 MB (5.242.880 byte), tanpa kompresi otomatis. Backend memeriksa MIME, ekstensi, dan magic byte.

**Konsekuensi:**

- Aplikasi memvalidasi awal untuk UX; backend tetap otoritatif.
- Bukti disimpan privat di R2.
- Retensi foto masih TBD; tidak ada penghapusan otomatis pada versi ini.

## ADR-005 — Satu pengajuan per mahasiswa, mata kuliah, dan tanggal

**Status:** Accepted

**Konteks:** SRS v0.2 belum menetapkan aturan duplikasi secara eksplisit; B.4 masih menyebutnya TBD.

**Keputusan:** UNIQUE `(student_user_id, course_id, class_date)`.

**Konsekuensi:**

- Pengajuan ganda untuk kelas yang sama ditolak `409 DUPLICATE_REQUEST`.
- Jika pengajuan dihapus saat masih `SUBMITTED`, mahasiswa dapat mengajukan ulang.
- Tidak ada penggabungan beberapa kategori dalam satu tanggal.

## ADR-006 — Audit Admin bersifat baca

**Status:** Accepted

**Konteks:** SRS memberi Admin akses ke seluruh pengajuan dan bukti, tetapi tidak memberi hak mengubah keputusan.

**Keputusan:** Admin dapat melihat semua pengajuan/detail/bukti dan memfilter; Admin tidak dapat mengubah, menghapus, atau memutuskan pengajuan.

**Konsekuensi:**

- Tidak ada endpoint admin untuk keputusan atau edit pengajuan.
- Admin tetap dapat menonaktifkan akun dan mengelola pemetaan.
- Jejak keputusan hanya berasal dari Instruktur.

## ADR-007 — Navigasi memakai `navigation-compose`

**Status:** Accepted

**Konteks:** Pedoman menyebut “Navigator Compose”; materi memakai `androidx.navigation:navigation-compose`.

**Keputusan:** Gunakan `androidx.navigation:navigation-compose` sebagai implementasi Navigator Compose, dengan route bertipe dan `NavHost`.

**Konsekuensi:**

- Satu dependensi navigasi resmi, sesuai contoh materi.
- Tidak memakai library navigasi alternatif.
- Jika dosen pembimbing mensyaratkan library lain, keputusan ini ditinjau ulang.

## ADR-008 — Backend JavaScript ESM di dalam repo

**Status:** Accepted

**Konteks:** Backend belum ada; perlu lokasi dan bahasa yang konsisten dengan tugas.

**Keputusan:** Backend Node.js 24 + Express 5, JavaScript ESM, berada di `TugasAkhir/backend`, tanpa TypeScript dan tanpa ORM.

**Konsekuensi:**

- SQL eksplisit dan parameterized.
- `package.json` memakai `"type": "module"`.
- Satu repo untuk Android, backend, migrasi, dan dokumentasi.
- Tidak ada build step backend selain container.

## ADR-009 — JWT access 15 menit, refresh 7 hari

**Status:** Accepted

**Konteks:** TBD-5 belum menetapkan masa berlaku.

**Keputusan:** Access token 15 menit; refresh token 7 hari; rotasi setiap refresh.

**Konsekuensi:**

- Klien harus menangani refresh otomatis.
- Refresh token dicabut saat logout.
- Tidak ada refresh token sliding lifetime; token baru selalu 7 hari.

## ADR-010 — `minSdk 24`

**Status:** Accepted

**Konteks:** TBD-5 menyebut minimum Android; perangkat target perlu ditetapkan.

**Keputusan:** `minSdk = 24` (Android 7.0), `compileSdk`/`targetSdk` mengikuti konfigurasi proyek.

**Konsekuensi:**

- API keamanan/penyimpanan yang lebih baru perlu fallback.
- Emulator dan perangkat uji harus Android 7.0+.

## ADR-011 — Admin pertama melalui CLI one-off

**Status:** Accepted

**Konteks:** Registrasi mandiri hanya untuk mahasiswa; tidak ada mekanisme membuat Admin pertama.

**Keputusan:** CLI `npm run create-admin` membaca environment, tidak memakai password default, dan dijalankan sekali saat bootstrap.

**Konsekuensi:**

- Tidak ada seed admin di migrasi.
- Kredensial tidak masuk Git.
- CLI menolak duplikasi dan dapat membatasi pembuatan Admin tambahan.

## ADR-012 — Nonaktifkan pengguna, batasi hapus mata kuliah

**Status:** Accepted

**Konteks:** TBD-7 belum menetapkan hard delete vs arsip; data historis harus aman.

**Keputusan:** `DELETE /admin/users/{id}` menonaktifkan (`is_active = FALSE`), bukan hard delete. Mata kuliah hanya boleh dihapus jika tidak direferensikan. Pemetaan boleh dihapus.

**Konsekuensi:**

- Riwayat pengajuan tetap utuh.
- Akun nonaktif tidak dapat login dan tokennya dicabut.
- Mata kuliah yang sudah dipakai akan menghasilkan `409 COURSE_IN_USE`.

## ADR-013 — Tanpa Room

**Status:** Accepted

**Konteks:** Pedoman melarang Room; data harus berasal dari API.

**Keputusan:** Tidak ada database lokal/Room. Hanya cache file sementara untuk foto dan penyimpanan token terenkripsi.

**Konsekuensi:**

- Aplikasi membutuhkan jaringan untuk data.
- Tidak ada sinkronisasi offline.
- State layar disimpan di ViewModel, bukan database.

## ADR-014 — Batas Supabase dan Cloudflare R2

**Status:** Accepted

**Konteks:** SRS menyebut PostgreSQL Supabase dan R2, tetapi implementasi bisa keliru menghubungkan Android langsung ke keduanya.

**Keputusan:** Android hanya memanggil backend. Backend memakai PostgreSQL Supabase melalui pooler dan R2 melalui S3-compatible API. Tidak ada Supabase Auth/Storage/Data API, tidak ada presigned URL.

**Konsekuensi:**

- Kredensial hanya di server.
- Bukti dibaca melalui endpoint streaming dengan header autentikasi.
- R2 bucket privat.

## ADR-015 — Koreksi dokumen dan diagram

**Status:** Accepted

**Konteks:** Ditemukan MySQL, Cloud Storage, FK yang salah, dan TBD yang sudah dapat diputuskan.

**Keputusan:** SRS menjadi v0.3; Activity dan ERD diperbarui; PNG diregenerasi; kamus data memakai PostgreSQL/TIMESTAMPTZ/R2 dan FK `users`.

**Konsekuensi:**

- Sumber dan gambar sinkron.
- SRS v0.2 tetap disimpan sebagai riwayat.
- Perubahan dokumen tidak boleh dilakukan tanpa persetujuan baru.

## ADR-016 — Kontrak API ber-envelope dan berversi

**Status:** Accepted

**Konteks:** Android dan backend perlu kesepakatan error/pagination yang stabil.

**Keputusan:** Base `/api/v1`; sukses `{data, meta?}`; error `{error:{code,message,details?}}`; enum huruf besar; tanggal `YYYY-MM-DD`; timestamp UTC.

**Konsekuensi:**

- Android memetakan `code` ke pesan.
- Perubahan merusak menaikkan versi.
- Tidak ada HTML error dari Express.

## ADR-017 — Bukti hanya melalui streaming backend

**Status:** Accepted

**Konteks:** Bucket privat dan risiko kebocoran jika URL dibagikan.

**Keputusan:** `GET /leave-requests/{id}/evidence` memeriksa role/ownership lalu mengalirkan objek R2. Tidak ada URL publik/presigned.

**Konsekuensi:**

- Semua permintaan bukti melewati backend.
- Cache privat, `no-store`.
- Beban bandwidth backend meningkat; dapat dioptimalkan nanti tanpa mengubah kontrak.

## ADR-018 — Cakupan versi ini

**Status:** Accepted

**Konteks:** Mencegah scope creep dan memastikan tugas selesai.

**Keputusan:** Tidak termasuk reset password, notifikasi, impor CSV, web admin, SSO, kompresi/retensi otomatis, dan multi-kampus.

**Konsekuensi:**

- Fitur tersebut tidak diklaim selesai.
- Jika diminta, masuk backlog terpisah.

## Keputusan yang masih terbuka

| Isu | Status | Dampak |
|---|---|---|
| Retensi foto setelah pengajuan final | TBD-4 | Penyimpanan dan privasi |
| Kebijakan retensi data | Tanpa nomor | Backup/penghapusan |
| Backup, pemulihan, region, biaya | TBD-6 | Recovery dan ketersediaan |
| Cakupan/rotasi token R2 | TBD-8 | Kredensial penyimpanan |
| Paket Supabase | TBD-9 | Ketersediaan saat penilaian |
| Pembersihan objek R2 yatim | TBD-10 | Operasional |
| Reset password | TBD-1; out of scope versi ini | UX |
| Impor CSV | TBD-2; out of scope versi ini | Operasional |
| Notifikasi | Out of scope | UX |

## Ringkasan pemetaan fitur CRUDS

| Fitur | Create | Read | Update | Delete/Search |
|---|---|---|---|---|
| Akun | Admin create | Admin list/detail | Admin profile | Nonaktifkan + search |
| Mata kuliah | Admin create | Semua role (scope) | Admin update | Admin delete terbatas + search |
| Pemetaan | Admin add | Admin list/search | — | Admin remove |
| Pengajuan | Mahasiswa create | Semua role (scope) | Mahasiswa edit | Mahasiswa delete + filter |
| Keputusan | Instruktur decide | Semua role (scope) | — | Filter status |

Dengan demikian lima fitur CRUDS terpenuhi tanpa menambahkan entitas di luar SRS.
