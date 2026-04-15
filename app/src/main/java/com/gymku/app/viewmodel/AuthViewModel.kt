package com.gymku.app.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymku.app.data.model.Admin
import com.gymku.app.data.model.ShiftInfo
import com.gymku.app.data.preferences.AppPreferences
import com.gymku.app.data.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class LoggedIn(val admin: Admin, val shift: ShiftInfo) : AuthState()
    object AlreadyLoggedIn : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {

    private val repo = AdminRepository()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentAdmin = MutableStateFlow<Admin?>(null)
    val currentAdmin: StateFlow<Admin?> = _currentAdmin.asStateFlow()

    private val _currentShift = MutableStateFlow(ShiftInfo("...", "Luar Jam", 0, 0))
    val currentShift: StateFlow<ShiftInfo> = _currentShift.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                repo.getAllShifts().collect { shifts ->
                    _currentShift.value = ShiftInfo.detectCurrentShift(shifts)
                }
            } catch (e: Exception) {}
        }
    }

    fun checkAlreadyLoggedIn(context: Context) {
        viewModelScope.launch {
            val prefs = AppPreferences(context)
            prefs.getLoggedAdmin().collect { admin ->
                if (admin != null) {
                    _currentAdmin.value = admin
                    try {
                        val shifts = repo.getAllShifts().first()
                        _currentShift.value = ShiftInfo.detectCurrentShift(shifts)
                    } catch(e: Exception) {}
                    _authState.value = AuthState.AlreadyLoggedIn
                }
            }
        }
    }

    fun login(context: Context, username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Username dan password tidak boleh kosong")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                // Seed default admin first (throws if Firebase not ready)
                repo.seedDefaultAdminIfNeeded()
                try { repo.seedDefaultShiftsIfNeeded() } catch(e: Exception) {}
                val admin = repo.login(username, password)
                if (admin != null) {
                    val shifts = repo.getAllShifts().first()
                    val shift = ShiftInfo.detectCurrentShift(shifts)
                    
                    // Validation for staff
                    if (admin.role != "admin") {
                        val allStaffSchedules = repo.getAllStaffSchedules().first()
                        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale("id", "ID")).format(java.util.Date())
                        val todaySchedule = allStaffSchedules.find { it.date == today && it.adminId == admin.id }?.shiftName
                        
                        if (todaySchedule.isNullOrEmpty()) {
                            _authState.value = AuthState.Error("Gagal: Anda tidak memiliki jadwal shift hari ini. Silakan hubungi Admin.")
                            return@launch
                        }

                        if (shift.name != todaySchedule) {
                            val errorMsg = if (shift.name == "Luar Jam") {
                                "Gagal: Saat ini di luar jam operasional shift."
                            } else {
                                "Gagal: Jadwal Anda hari ini adalah shift $todaySchedule. Saat ini adalah shift ${shift.name}."
                            }
                            _authState.value = AuthState.Error(errorMsg)
                            return@launch
                        }
                    }

                    _currentAdmin.value = admin
                    _currentShift.value = shift
                    AppPreferences(context).saveLoggedAdmin(admin)
                    _authState.value = AuthState.LoggedIn(admin, shift)
                } else {
                    _authState.value = AuthState.Error("Username atau password salah")
                }
            } catch (e: Exception) {
                val msg = when {
                    e.message?.contains("Firebase", ignoreCase = true) == true ||
                    e.message?.contains("No Firebase", ignoreCase = true) == true ->
                        "Firebase belum dikonfigurasi. Masuk ke Pengaturan untuk setup."
                    e.message?.contains("network", ignoreCase = true) == true ||
                    e.message?.contains("Unable to resolve", ignoreCase = true) == true ->
                        "Tidak ada koneksi internet. Periksa jaringan Anda."
                    e.message?.contains("permission", ignoreCase = true) == true ->
                        "Akses ditolak. Periksa Firebase Database Rules."
                    else ->
                        "Gagal login: ${e.message ?: "Error tidak diketahui"}"
                }
                _authState.value = AuthState.Error(msg)
            }
        }
    }

    fun logout(context: Context, onDone: () -> Unit) {
        viewModelScope.launch {
            AppPreferences(context).clearLoggedAdmin()
            _currentAdmin.value = null
            _authState.value = AuthState.Idle
            onDone()
        }
    }

    fun resetError() { _authState.value = AuthState.Idle }
}
