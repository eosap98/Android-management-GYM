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

## Teknologi

- **Bahasa Pemrograman**: Kotlin
- **UI Framework**: Jetpack Compose (Material 3)
- **Database**: Firebase Realtime Database
- **Arsitektur**: MVVM (Model-View-ViewModel)
- **Library Lainnya**:
  - CameraX & ML Kit (untuk QR Scanner)
  - DataStore (untuk penyimpanan konfigurasi lokal)
  - MPAndroidChart (untuk visualisasi data)
  - iText7 (untuk ekspor PDF)(*BETA*)


Download Aplikasi [Klik disini](https://github.com/eosap98/Android-management-GYM-/releases/download/V1.0/gymkuApp.apk)


## Struktur Project

- `app/src/main/java/com/gymku/app/data`: Model data dan repositori.
- `app/src/main/java/com/gymku/app/ui`: Layer UI (Screens, Theme, Components).
- `app/src/main/java/com/gymku/app/viewmodel`: Logika bisnis dan state management.
- `app/src/main/java/com/gymku/app/util`: Utility classes (Exporter, Formatter).

---
*Dibuat oleh [Eos Ageng](https://www.instagram.com/eosnada1702/)*
