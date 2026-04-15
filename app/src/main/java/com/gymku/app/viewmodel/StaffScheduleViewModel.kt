package com.gymku.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymku.app.data.model.Admin
import com.gymku.app.data.model.ShiftInfo
import com.gymku.app.data.model.StaffSchedule
import com.gymku.app.data.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class CalendarDay(
    val dateString: String,      // "yyyy-MM-dd"
    val displayDay: String,      // e.g. "1", "15"
    val isCurrentMonth: Boolean,
    val isSunday: Boolean,
    val holidayName: String?
)

class StaffScheduleViewModel : ViewModel() {
    private val repo = AdminRepository()

    private val _staffList = MutableStateFlow<List<Admin>>(emptyList())
    val staffList: StateFlow<List<Admin>> = _staffList.asStateFlow()

    private val _shiftList = MutableStateFlow<List<ShiftInfo>>(emptyList())
    val shiftList: StateFlow<List<ShiftInfo>> = _shiftList.asStateFlow()

    private val _schedules = MutableStateFlow<List<StaffSchedule>>(emptyList())
    val schedules: StateFlow<List<StaffSchedule>> = _schedules.asStateFlow()

    private val _monthYearTitle = MutableStateFlow("")
    val monthYearTitle: StateFlow<String> = _monthYearTitle.asStateFlow()

    private val _calendarGrid = MutableStateFlow<List<CalendarDay>>(emptyList())
    val calendarGrid: StateFlow<List<CalendarDay>> = _calendarGrid.asStateFlow()

    private var currentMonthCal = Calendar.getInstance(Locale("id", "ID"))

    private val fixedHolidays = mapOf(
        "01-01" to "Tahun Baru Masehi",
        "05-01" to "Hari Buruh",
        "06-01" to "Hari Lahir Pancasila",
        "08-17" to "Kemerdekaan RI",
        "12-25" to "Hari Raya Natal"
    )

    init {
        // Reset to first day of month
        currentMonthCal.set(Calendar.DAY_OF_MONTH, 1)
        updateCalendar()
        
        viewModelScope.launch {
            launch {
                repo.getAllAdmins().collect { list ->
                    _staffList.value = list.filter { it.role == "staff" }
                }
            }
            launch {
                repo.getAllShifts().collect { list ->
                    _shiftList.value = list
                }
            }
            launch {
                repo.getAllStaffSchedules().collect { list ->
                    _schedules.value = list
                }
            }
        }
    }

    fun prevMonth() {
        currentMonthCal.add(Calendar.MONTH, -1)
        updateCalendar()
    }

    fun nextMonth() {
        currentMonthCal.add(Calendar.MONTH, 1)
        updateCalendar()
    }

    private fun updateCalendar() {
        val localeId = Locale("id", "ID")
        val titleFormat = SimpleDateFormat("MMMM yyyy", localeId)
        _monthYearTitle.value = titleFormat.format(currentMonthCal.time)

        val cal = currentMonthCal.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)
        
        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        
        // Sunday = 1, Monday = 2 ... Saturday = 7
        // We want Monday to be the first column (index 0) or Sunday (index 0)?
        // Indonesian standard usually starts with Monday or Sunday. Let's start with Monday for standard,
        // Wait, standard Calendar usually starts with Sunday. Let's make columns: M, S, S, R, K, J, S
        // If Sunday is first: SUNDAY(1) -> index 0. MONDAY(2) -> index 1.
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1 to 7
        val emptyPrefixDays = firstDayOfWeek - 1 // If Sunday(1), 0 empty days. If Monday(2), 1 empty day.

        val grid = mutableListOf<CalendarDay>()

        // Add prefix blank days (from previous month)
        val prevMonthCal = cal.clone() as Calendar
        prevMonthCal.add(Calendar.MONTH, -1)
        val maxPrevDays = prevMonthCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (i in 0 until emptyPrefixDays) {
            val day = maxPrevDays - emptyPrefixDays + i + 1
            grid.add(CalendarDay(
                dateString = "", // Not selectable
                displayDay = day.toString(),
                isCurrentMonth = false,
                isSunday = false,
                holidayName = null
            ))
        }

        // Add current month days
        val sdfDate = SimpleDateFormat("yyyy-MM-dd", localeId)
        val sdfMonthDay = SimpleDateFormat("MM-dd", localeId)
        for (i in 1..maxDays) {
            cal.set(Calendar.DAY_OF_MONTH, i)
            val dateStr = sdfDate.format(cal.time)
            val mdStr = sdfMonthDay.format(cal.time)
            val isSunday = cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
            val holiday = fixedHolidays[mdStr]

            grid.add(CalendarDay(
                dateString = dateStr,
                displayDay = i.toString(),
                isCurrentMonth = true,
                isSunday = isSunday,
                holidayName = holiday
            ))
        }

        // Add suffix blank days (next month) to complete the grid of 7 columns
        val trailingEmptyDays = (7 - grid.size % 7) % 7
        for (i in 1..trailingEmptyDays) {
            grid.add(CalendarDay(
                dateString = "",
                displayDay = i.toString(),
                isCurrentMonth = false,
                isSunday = false,
                holidayName = null
            ))
        }

        _calendarGrid.value = grid
    }

    fun saveSchedule(date: String, adminId: String, shiftName: String) {
        viewModelScope.launch {
            val existing = _schedules.value.find { it.date == date && it.adminId == adminId }
            if (shiftName.isEmpty() || shiftName == "Bebas") {
                if (existing != null) repo.deleteStaffSchedule(existing.id)
            } else {
                val newSchedule = StaffSchedule(
                    id = existing?.id ?: "",
                    date = date,
                    adminId = adminId,
                    shiftName = shiftName
                )
                repo.saveStaffSchedule(newSchedule)
            }
        }
    }
}
