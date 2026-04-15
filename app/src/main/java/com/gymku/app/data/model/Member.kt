package com.gymku.app.data.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class Member(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val gender: String = "L",   // "L" or "P"
    val joinDate: String = "",   // "yyyy-MM-dd"
    val expireDate: String = "", // "yyyy-MM-dd"
    val isActive: Boolean = true,
    val qrCode: String = "",     // same as id: "GYM-XXXX"
    val photoUrl: String = "",
    val lastCheckIn: String = "",
    val lockerNumber: String = ""
) {
    /** Dynamically compute days remaining from today */
    fun daysRemaining(): Int {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val expire = sdf.parse(expireDate) ?: return 0
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }.time
            val diff = expire.time - today.time
            val days = (diff / (1000 * 60 * 60 * 24)).toInt()
            if (days < 0) 0 else days
        } catch (e: Exception) { 0 }
    }

    fun isCurrentlyActive(): Boolean = daysRemaining() > 0

    fun getInitials(): String {
        val parts = name.trim().split(" ")
        return when {
            parts.size >= 2 -> "${parts[0].firstOrNull() ?: ""}${parts[1].firstOrNull() ?: ""}".uppercase()
            parts.size == 1 -> name.take(2).uppercase()
            else -> "?"
        }
    }

    companion object {
        const val PRICE_NEW_MEMBER = 150_000L
        const val PRICE_RENEW_PER_MONTH = 100_000L

        fun generateId(existingCount: Int): String {
            return "GYM-${String.format("%04d", existingCount + 1)}"
        }

        fun calculateExpireDate(fromDate: Date = Date(), months: Int): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val cal = Calendar.getInstance().apply {
                time = fromDate
                add(Calendar.MONTH, months)
                add(Calendar.DAY_OF_YEAR, -1) // reduce 1 day
            }
            return sdf.format(cal.time)
        }
    }
}
