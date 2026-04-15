package com.gymku.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymku.app.data.model.Member
import com.gymku.app.data.model.Transaction
import com.gymku.app.data.repository.MemberRepository
import com.gymku.app.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ScannerState {
    object Scanning : ScannerState()
    object Loading : ScannerState()
    data class MemberFound(val member: Member) : ScannerState()
    data class NotFound(val qrCode: String) : ScannerState()
    object CheckInSuccess : ScannerState()
    data class Error(val message: String) : ScannerState()
}

class ScannerViewModel : ViewModel() {

    private val memberRepo = MemberRepository()
    private val txRepo = TransactionRepository()

    private val _state = MutableStateFlow<ScannerState>(ScannerState.Scanning)
    val state: StateFlow<ScannerState> = _state.asStateFlow()

    private var lastScannedCode: String = ""

    fun onQrCodeScanned(rawCode: String) {
        val code = rawCode.trim()
        if (code.isEmpty() || code == lastScannedCode) return   // debounce duplicate scan
        lastScannedCode = code
        viewModelScope.launch {
            _state.value = ScannerState.Loading
            // 1. Try direct lookup by ID (fastest, most reliable for new members)
            var member = memberRepo.getMemberById(code)
            
            // 2. Fallback to QR code search (for legacy members)
            if (member == null) {
                member = memberRepo.getMemberByQr(code)
            }
            
            _state.value = if (member != null) ScannerState.MemberFound(member)
                           else ScannerState.NotFound(code)
        }
    }

    fun confirmCheckIn(
        member: Member, adminId: String, adminName: String,
        shift: String, lockerNumber: String = ""
    ) {
        viewModelScope.launch {
            try {
                memberRepo.checkInMember(member, adminId, adminName, shift, lockerNumber)
                _state.value = ScannerState.CheckInSuccess
            } catch (e: Exception) {
                _state.value = ScannerState.Error(e.message ?: "Gagal check-in")
            }
        }
    }

    fun resetScanner() {
        lastScannedCode = ""
        _state.value = ScannerState.Scanning
    }
}
