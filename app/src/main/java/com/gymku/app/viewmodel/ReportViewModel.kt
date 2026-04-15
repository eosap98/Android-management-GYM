package com.gymku.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymku.app.data.model.Transaction
import com.gymku.app.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ReportFilter(
    val type: FilterType = FilterType.TODAY,
    val startDate: String = "",
    val endDate: String = "",
    val shiftName: String = "Semua"
) {
    enum class FilterType { TODAY, THIS_WEEK, THIS_MONTH, CUSTOM }
}

data class ReportData(
    val transactions: List<Transaction> = emptyList(),
    val totalCash: Long = 0L,
    val totalQris: Long = 0L,
    val totalRevenue: Long = 0L,
    val newMemberCount: Int = 0,
    val renewCount: Int = 0,
    val visitorCount: Int = 0,
    val checkInCount: Int = 0
)

class ReportViewModel : ViewModel() {

    private val txRepo = com.gymku.app.data.repository.TransactionRepository()
    private val memberRepo = com.gymku.app.data.repository.MemberRepository()

    private val _filter = MutableStateFlow(ReportFilter())
    val filter: StateFlow<ReportFilter> = _filter.asStateFlow()

    private val _reportData = MutableStateFlow(ReportData())
    val reportData: StateFlow<ReportData> = _reportData.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _allTransactions = MutableStateFlow<List<Transaction>>(emptyList())
    private val _checkInCount = MutableStateFlow(0)

    init { 
        loadTransactions() 
        loadCheckIns()
    }

    private fun loadCheckIns() {
        viewModelScope.launch {
            memberRepo.getTodayCheckInCount().collect { count ->
                _checkInCount.value = count
                applyFilter()
            }
        }
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            txRepo.getAllTransactions().collect { list ->
                _allTransactions.value = list
                applyFilter()
            }
        }
    }

    fun setFilter(f: ReportFilter) {
        _filter.value = f
        applyFilter()
    }

    private fun applyFilter() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = sdf.format(Date())
        val cal = java.util.Calendar.getInstance()
        val currentFilter = _filter.value

        var filtered = when (currentFilter.type) {
            ReportFilter.FilterType.TODAY -> _allTransactions.value.filter { it.date == today }
            ReportFilter.FilterType.THIS_WEEK -> {
                cal.set(java.util.Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                val weekStart = sdf.format(cal.time)
                _allTransactions.value.filter { it.date >= weekStart }
            }
            ReportFilter.FilterType.THIS_MONTH -> {
                val monthPrefix = today.substring(0, 7) // "yyyy-MM"
                _allTransactions.value.filter { it.date.startsWith(monthPrefix) }
            }
            ReportFilter.FilterType.CUSTOM -> {
                _allTransactions.value.filter { tx ->
                    val afterStart = currentFilter.startDate.isEmpty() || tx.date >= currentFilter.startDate
                    val beforeEnd = currentFilter.endDate.isEmpty() || tx.date <= currentFilter.endDate
                    afterStart && beforeEnd
                }
            }
        }

        if (currentFilter.shiftName != "Semua" && currentFilter.shiftName.isNotEmpty()) {
            filtered = filtered.filter { it.shift == currentFilter.shiftName }
        }

        _reportData.value = ReportData(
            transactions      = filtered,
            totalCash         = filtered.filter { it.paymentMethod == "Cash" }.sumOf { it.amount },
            totalQris         = filtered.filter { it.paymentMethod == "QRIS" }.sumOf { it.amount },
            totalRevenue      = filtered.sumOf { it.amount },
            newMemberCount    = filtered.count { it.type == Transaction.TYPE_NEW_MEMBER },
            renewCount        = filtered.count { it.type == Transaction.TYPE_RENEW },
            visitorCount      = filtered.count { it.type == Transaction.TYPE_VISITOR },
            checkInCount      = if (currentFilter.type == ReportFilter.FilterType.TODAY) _checkInCount.value else 0
        )
    }

    fun deleteTransaction(txId: String) {
        viewModelScope.launch {
            try {
                txRepo.deleteTransaction(txId)
            } catch (e: Exception) {}
        }
    }
}
