package com.gymku.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymku.app.data.model.VisitorHistory
import com.gymku.app.data.repository.MemberRepository
import com.gymku.app.data.repository.TransactionRepository
import com.gymku.app.data.repository.VisitorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class DashboardData(
    val memberCheckIns: Int = 0,
    val visitorCount: Int = 0,
    val todayRevenue: Long = 0L,
    val visitorHistory: List<VisitorHistory> = emptyList()
)

class DashboardViewModel : ViewModel() {

    private val memberRepo = MemberRepository()
    private val visitorRepo = VisitorRepository()
    private val txRepo = TransactionRepository()

    private val _dashboardData = MutableStateFlow(DashboardData())
    val dashboardData: StateFlow<DashboardData> = _dashboardData.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadDashboard()
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            // Collect today's check-in records and transactions simultaneously
            combine(
                memberRepo.getTodayCheckInCount(),
                visitorRepo.getTodayVisitorCount(),
                txRepo.getTodayTransactions(),
                memberRepo.getTodayCheckIns(),
                visitorRepo.getTodayVisitors()
            ) { mCount, vCount, txList, mCheckIns, vList ->
                
                // Combine and sort total visitors (members + daily)
                val combinedHistory = (mCheckIns.map { 
                    VisitorHistory(it.memberName, it.timestamp, "Member") 
                } + vList.map { 
                    VisitorHistory(it.name, it.timestamp, "Harian") 
                }).sortedByDescending { it.timestamp }.take(5)

                DashboardData(
                    memberCheckIns = mCount,
                    visitorCount = vCount,
                    todayRevenue = txList.sumOf { it.amount },
                    visitorHistory = combinedHistory
                )
            }.collect { data ->
                _dashboardData.value = data
            }
        }
    }

    fun refresh() { loadDashboard() }
}
