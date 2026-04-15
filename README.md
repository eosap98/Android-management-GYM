# GymKu - Aplikasi Manajemen GYM

Aplikasi manajemen GYM (Native Android) yang dibangun menggunakan Kotlin dan Jetpack Compose. Aplikasi ini dirancang untuk memudahkan pengelolaan anggota, pengunjung, jadwal staf, dan pelaporan keuangan.

## Fitur Utama

- **Dashboard Real-time**: Ringkasan jumlah anggota aktif, pengunjung harian, dan total pemasukan.
- **Manajemen Anggota**: Pendaftaran anggota baru, pencarian anggota, dan ekspor kartu anggota.
- **Check-in QR Code**: Scanner QR untuk kehadiran anggota secara cepat dan efisien.
- **Manajemen Pengunjung**: Pencatatan pengunjung harian (transaksi instan).
- **Jadwal Staf**: Kalender shift staf terintegrasi dengan validasi login berdasarkan waktu shift.
- **Laporan Keuangan**: Rekapitulasi transaksi bulanan yang dapat diekspor ke format PDF.
- **Konfigurasi Firebase**: Sinkronisasi data real-time dengan Firebase Realtime Database.

## Screenshot
<img src="https://github.com/user-attachments/assets/e45d2be7-1692-4b93-85da-7b6962d94d75" width="300" height="auto" alt="Deskripsi Gambar">
<img src="https://github.com/user-attachments/assets/981376df-4027-448c-a673-f5d1c5987c49" width="300" height="auto" alt="Deskripsi Gambar">
<img src="https://github.com/user-attachments/assets/b0c35d9e-a84e-42b3-8079-63cbb4ecb608" width="300" height="auto" alt="Deskripsi Gambar">
<img src="https://github.com/user-attachments/assets/b377dde4-5e21-4c16-8c3e-24cdcb8da220" width="300" height="auto" alt="Deskripsi Gambar">
<img src="https://github.com/user-attachments/assets/bdb7b402-ef08-4fe2-a5e3-6f71fd351c40" width="300" height="auto" alt="Deskripsi Gambar">
<img src="https://github.com/user-attachments/assets/d569a4f9-f9d1-4b18-ab65-1d9a6ba711d9" width="300" height="auto" alt="Deskripsi Gambar">
<img src="https://github.com/user-attachments/assets/4ab2d36f-1b06-4cf7-a67a-7dcb96abcc7e" width="300" height="auto" alt="Deskripsi Gambar">



## Teknologi

- **Bahasa Pemrograman**: Kotlin
- **UI Framework**: Jetpack Compose (Material 3)
- **Database**: Firebase Realtime Database
- **Arsitektur**: MVVM (Model-View-ViewModel)
- **Library Lainnya**:
  - CameraX & ML Kit (untuk QR Scanner)
  - DataStore (untuk penyimpanan konfigurasi lokal)
  - MPAndroidChart (untuk visualisasi data)
  - iText7 (untuk ekspor PDF) **BETA**


Download Aplikasi [Klik disini](https://github.com/eosap98/Android-management-GYM-/releases/download/V1.0/gymkuApp.apk)


## Struktur Project

- `app/src/main/java/com/gymku/app/data`: Model data dan repositori.
- `app/src/main/java/com/gymku/app/ui`: Layer UI (Screens, Theme, Components).
- `app/src/main/java/com/gymku/app/viewmodel`: Logika bisnis dan state management.
- `app/src/main/java/com/gymku/app/util`: Utility classes (Exporter, Formatter).

---
*Dibuat oleh [Eos Ageng](https://www.instagram.com/eosnada1702/)*
