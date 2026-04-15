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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class MemberActionState {
    object Idle : MemberActionState()
    object Loading : MemberActionState()
    data class Success(val message: String) : MemberActionState()
    data class Error(val message: String) : MemberActionState()
}

class MemberViewModel : ViewModel() {
    private val memberRepo = MemberRepository()
    private val txRepo = TransactionRepository()

    private val _members = MutableStateFlow<List<Member>>(emptyList())
    val members: StateFlow<List<Member>> = _members.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filteredMembers = MutableStateFlow<List<Member>>(emptyList())
    val filteredMembers: StateFlow<List<Member>> = _filteredMembers.asStateFlow()

    private val _selectedMember = MutableStateFlow<Member?>(null)
    val selectedMember: StateFlow<Member?> = _selectedMember.asStateFlow()

    private val _actionState = MutableStateFlow<MemberActionState>(MemberActionState.Idle)
    val actionState: StateFlow<MemberActionState> = _actionState.asStateFlow()

    init { loadMembers() }

    private fun loadMembers() {
        viewModelScope.launch {
            memberRepo.getAllMembers().collect { list ->
                _members.value = list
                applyFilter()
            }
        }
    }

    fun setSearch(query: String) {
        _searchQuery.value = query
        applyFilter()
    }

    private fun applyFilter() {
        val q = _searchQuery.value.trim().lowercase()
        _filteredMembers.value = if (q.isEmpty()) _members.value
        else _members.value.filter {
            it.name.lowercase().contains(q) || it.id.lowercase().contains(q) || it.phone.contains(q)
        }
    }

    fun selectMember(member: Member) { _selectedMember.value = member }
    fun clearSelectedMember() { _selectedMember.value = null }

    fun addMember(
        name: String, phone: String, gender: String, months: Int,
        paymentMethod: String, adminId: String, adminName: String, shift: String
    ) {
        viewModelScope.launch {
            _actionState.value = MemberActionState.Loading
            try {
                val count = memberRepo.getMemberCount()
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val today = sdf.format(Date())
                val memberId = Member.generateId(count)
                val expireDate = Member.calculateExpireDate(months = months)
                val member = Member(
                    id = memberId, name = name, phone = phone, gender = gender,
                    joinDate = today, expireDate = expireDate,
                    isActive = true, qrCode = memberId
                )
                val key = memberRepo.addMember(member)
                val extraMonths = if (months > 1) months - 1 else 0
                val price = Member.PRICE_NEW_MEMBER + (extraMonths * Member.PRICE_RENEW_PER_MONTH)
                txRepo.addTransaction(Transaction(
                    type = Transaction.TYPE_NEW_MEMBER, memberId = key, memberName = name,
                    amount = price, paymentMethod = paymentMethod,
                    adminId = adminId, adminName = adminName, shift = shift, months = months
                ))
                _actionState.value = MemberActionState.Success("Member $name berhasil didaftarkan!")
            } catch (e: Exception) {
                _actionState.value = MemberActionState.Error(e.message ?: "Gagal mendaftarkan member")
            }
        }
    }

    fun renewMember(
        memberId: String, months: Int, paymentMethod: String,
        adminId: String, adminName: String, shift: String
    ) {
        viewModelScope.launch {
            _actionState.value = MemberActionState.Loading
            try {
                val updated = memberRepo.renewMember(memberId, months)
                val price = months * Member.PRICE_RENEW_PER_MONTH
                txRepo.addTransaction(Transaction(
                    type = Transaction.TYPE_RENEW, memberId = memberId, memberName = updated.name,
                    amount = price, paymentMethod = paymentMethod,
                    adminId = adminId, adminName = adminName, shift = shift, months = months
                ))
                _actionState.value = MemberActionState.Success("Perpanjangan ${months} bulan berhasil!")
            } catch (e: Exception) {
                _actionState.value = MemberActionState.Error(e.message ?: "Gagal memperpanjang")
            }
        }
    }

    fun resetActionState() { _actionState.value = MemberActionState.Idle }

    fun deleteMember(member: Member) {
        viewModelScope.launch {
            _actionState.value = MemberActionState.Loading
            try {
                memberRepo.deleteMember(member.id)
                _actionState.value = MemberActionState.Success("Member ${member.name} berhasil dihapus.")
            } catch (e: Exception) {
                _actionState.value = MemberActionState.Error(e.message ?: "Gagal menghapus member")
            }
        }
    }

    fun updateMember(member: Member) {
        viewModelScope.launch {
            _actionState.value = MemberActionState.Loading
            try {
                memberRepo.updateMember(member)
                _actionState.value = MemberActionState.Success("Data ${member.name} berhasil diperbarui.")
            } catch (e: Exception) {
                _actionState.value = MemberActionState.Error(e.message ?: "Gagal memperbarui member")
            }
        }
    }

    fun manualCheckIn(
        member: Member, adminId: String, adminName: String,
        shift: String, lockerNumber: String = ""
    ) {
        viewModelScope.launch {
            _actionState.value = MemberActionState.Loading
            try {
                memberRepo.checkInMember(member, adminId, adminName, shift, lockerNumber)
                _actionState.value = MemberActionState.Success(
                    "Check-in berhasil!\nWaktu: ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())}\nSisa Aktif: ${member.daysRemaining()} hari"
                )
            } catch (e: Exception) {
                _actionState.value = MemberActionState.Error(e.message ?: "Gagal check-in manual")
            }
        }
    }
}
