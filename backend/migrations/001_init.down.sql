-- IzinMu — rollback skema awal PostgreSQL
-- Migrasi: 001_init (down)
-- Ekstensi pg_trgm tidak dihapus karena dapat dipakai objek lain di database.

BEGIN;

DROP TABLE IF EXISTS refresh_tokens;
DROP TABLE IF EXISTS leave_requests;
DROP TABLE IF EXISTS course_enrollments;
DROP TABLE IF EXISTS course_instructors;
DROP TABLE IF EXISTS courses;
DROP TABLE IF EXISTS users;

COMMIT;
