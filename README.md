# Web Service Sistem Presensi Apel Mahasiswa (WS-Apel)

Backend REST API untuk presensi apel dan perizinan mahasiswa. Proyek ini telah disederhanakan (tanpa frontend) dan hanya berisi layanan backend dengan keamanan JWT, dokumentasi Swagger, dan struktur kode terorganisir.

## Teknologi
- Spring Boot 3 (Web, Security, JPA)
- PostgreSQL
- JWT (jjwt)
- Swagger/OpenAPI (springdoc-openapi-starter-webmvc-ui)
- Lombok

## Menjalankan Proyek
1. Konfigurasi database di `src/main/resources/application.properties`.
2. Jalankan aplikasi:
   ```bash
   ./mvnw spring-boot:run
   ```
3. Akses aplikasi di `http://localhost:8080`.
4. Swagger UI: `http://localhost:8080/swagger-ui.html`

## Keamanan & Otorisasi
- Header untuk endpoint dilindungi:
  ```
  Authorization: Bearer <JWT>
  ```
- Authority (tanpa prefix `ROLE_`): `ADMIN`, `SPD`, `MAHASISWA`.
- Guard endpoint konsisten:
  - Admin: `@PreAuthorize("hasAuthority('ADMIN')")`
  - SPD: `@PreAuthorize("hasAuthority('SPD')")`
  - Umum/Mahasiswa: `@PreAuthorize("isAuthenticated()")` atau guard spesifik

## Endpoint Utama

### 1) Auth (publik)
- Register Mahasiswa — `POST /api/auth/register`
  - Body:
    ```json
    {
      "username": "222312949",  
      "password": "secret",
      "nama": "Nama Lengkap",
      "kelas": "A",
      "tingkat": "2"
    }
    ```
  - Hasil: membuat User(MAHASISWA) dan Mahasiswa (nim=username) lalu mengembalikan `{ token, role }`.

- Register Admin Pertama — `POST /api/auth/register-admin`
  - Membuat akun ADMIN pertama (sekali saja). Tidak perlu kelas/tingkat.
  - Balikkan `{ token, role }`.

- Login — `POST /api/auth/login`
  - Body:
    ```json
    { "username": "222312949", "password": "secret" }
    ```
  - Hasil: `{ token, role }`.

- Validate Token — `POST /api/auth/validate`
- Refresh Token — `POST /api/auth/refresh`
- Logout — `POST /api/auth/logout` (stateless; klien hapus token)

### 2) User (login)
- Profil saya — `GET /api/user/me` → `{ username, role, name }`
- Update profil — `PUT /api/user/me` → body `{ "username": "...", "name": "..." }`
- Ganti password — `PUT /api/user/password` → body `{ "oldPassword": "...", "newPassword": "..." }`
- Hapus akun — `DELETE /api/user/me`

- Admin membuat akun ADMIN/SPD — `POST /api/user/admin/register`
  - Body:
    ```json
    { "username": "spd1", "password": "secret", "role": "SPD", "name": "Nama SPD" }
    ```

- Admin user mgmt — `GET /api/user`, `GET /api/user/{id}`, `DELETE /api/user/{id}`

### 3) Mahasiswa
- Profil mahasiswa saya — `GET /api/mahasiswa/me`
- Ajukan izin (multipart) — `POST /api/mahasiswa/izin`
  - form-data fields: `tanggal`, `tingkat` (opsional), `alasan`, `bukti` (file opsional)
  - Prasyarat: harus ada jadwal untuk kombinasi `tanggal+tingkat`.
- List izin saya — `GET /api/mahasiswa/izin`
- Detail izin — `GET /api/mahasiswa/izin/{id}` → menyertakan `buktiBase64` jika ada
- Batalkan izin — `DELETE /api/mahasiswa/izin/{id}` (status MENUNGGU)

### 4) SPD
- Upload presensi batch — `POST /api/spd/presensi` (menandai HADIR)
  - Body:
    ```json
    { "tanggal": "2025-10-24", "tingkat": "2", "mahasiswa": ["2221001", "2221002"] }
    ```
  - Presensi disimpan bersama `createdBySpd` (username SPD) untuk audit.
- Rekap presensi — `GET /api/spd/presensi` (filter opsional `tanggal`, `tingkat`), atau `GET /api/spd/presensi/{tanggal}?tingkat=...`
- Hapus entri presensi — `DELETE /api/spd/presensi/{id}`
- Tandai terlambat — `POST /api/spd/terlambat` (buat/ubah entri menjadi TERLAMBAT, dengan `createdBySpd`)
- Rekap terlambat — `GET /api/spd/terlambat` (filter opsional)

### 5) Admin
- Jadwal — `POST /api/admin/jadwal`, `GET /api/admin/jadwal`, `PUT/DELETE /api/admin/jadwal/{id}`
- List izin — `GET /api/admin/izin`
  - Mengembalikan ringkasan: `{ id, tanggal, tingkat, mahasiswaNim, mahasiswaNama, jenis, statusBukti, alasan, catatanAdmin, hasBukti, buktiBase64 }`
- Ubah status izin — `PUT /api/admin/izin/{id}?status=DITERIMA|DITOLAK|MENUNGGU`
- Redaksi bukti (hapus isi) — `DELETE /api/admin/bukti`
- Rekap presensi global — `GET /api/admin/presensi`

## Prasyarat Data
- Jadwal apel untuk kombinasi `tanggal+tingkat` harus ada agar presensi dan izin dapat diproses.

## Contoh Postman/cURL
- Ajukan izin (multipart):
  ```bash
  curl -X POST http://localhost:8080/api/mahasiswa/izin \
    -H "Authorization: Bearer <JWT_MHS>" \
    -F tanggal=2025-10-26 \
    -F tingkat=2 \
    -F alasan="Sakit" \
    -F bukti=@/path/ke/file.jpg
  ```
- List izin (ADMIN):
  ```bash
  curl -H "Authorization: Bearer <JWT_ADMIN>" http://localhost:8080/api/admin/izin
  ```
- Upload presensi (SPD):
  ```bash
  curl -X POST http://localhost:8080/api/spd/presensi \
    -H "Authorization: Bearer <JWT_SPD>" \
    -H "Content-Type: application/json" \
    -d '{"tanggal":"2025-10-24","tingkat":"2","mahasiswa":["2221001","2221002"]}'
  ```
- Set status izin (ADMIN):
  ```bash
  curl -X PUT "http://localhost:8080/api/admin/izin/1?status=DITERIMA" \
    -H "Authorization: Bearer <JWT_ADMIN>"
  ```

## Swagger / OpenAPI
- UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Security schema: bearerAuth (JWT). Klik "Authorize" dan masukkan `Bearer <JWT>`.

## Troubleshooting
- 403 Forbidden:
  - Token tidak cocok dengan server aktif, role tidak sesuai guard, atau token kedaluwarsa. Login ulang dan pastikan role benar.
- 500 ByteBuddy/serialization saat list entity:
  - Telah diperbaiki dengan DTO. Gunakan endpoint list izin admin yang mengembalikan ringkasan (bukan entity secara langsung).
- Port 8080 in use: ubah `server.port` atau hentikan proses yang memakai port tersebut.

