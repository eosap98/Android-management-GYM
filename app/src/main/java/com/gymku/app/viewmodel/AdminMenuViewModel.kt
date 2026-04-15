package com.gymku.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymku.app.data.model.Admin
import com.gymku.app.data.model.ShiftInfo
import com.gymku.app.data.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminMenuViewModel : ViewModel() {
    private val repo = AdminRepository()

    private val _staffList = MutableStateFlow<List<Admin>>(emptyList())
    val staffList: StateFlow<List<Admin>> = _staffList.asStateFlow()

    private val _shiftList = MutableStateFlow<List<ShiftInfo>>(emptyList())
    val shiftList: StateFlow<List<ShiftInfo>> = _shiftList.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            repo.getAllAdmins().collect { admins ->
                _staffList.value = admins.filter { it.role == "staff" }
            }
        }
        viewModelScope.launch {
            repo.getAllShifts().collect { shifts ->
                _shiftList.value = shifts
            }
        }
    }

    fun addStaff(admin: Admin) {
        viewModelScope.launch { repo.addAdmin(admin.copy(role = "staff")) }
    }

    fun deleteStaff(adminId: String) {
        viewModelScope.launch { repo.deleteAdmin(adminId) }
    }

    fun saveShift(shift: ShiftInfo) {
        viewModelScope.launch { repo.saveShift(shift) }
    }

    fun deleteShift(shiftId: String) {
        viewModelScope.launch { repo.deleteShift(shiftId) }
    }

    fun updateAdminPassword(admin: Admin, newPassword: String) {
        viewModelScope.launch { repo.addAdmin(admin.copy(password = newPassword)) }
    }
}
