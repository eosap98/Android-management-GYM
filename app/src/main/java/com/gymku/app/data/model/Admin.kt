package com.gymku.app.data.model

data class Admin(
    val id: String = "",
    val name: String = "",
    val username: String = "",
    val password: String = "", // stored as plain text (or hashed)
    val role: String = "staff", // "admin" | "staff"
    val assignedShift: String = "" // e.g. "Pagi" or id of shift
)

data class ShiftInfo(
    val id: String = "",
    val name: String = "",      // "Pagi" | "Siang" | "Malam"
    val startHour: Int = 0,
    val endHour: Int = 0
) {
    fun isCurrentTimeInShift(): Boolean {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return if (startHour <= endHour) {
            hour in startHour until endHour
        } else {
            hour >= startHour || hour < endHour
        }
    }

    companion object {
        fun detectCurrentShift(activeShifts: List<ShiftInfo>): ShiftInfo {
            val matched = activeShifts.firstOrNull { it.isCurrentTimeInShift() }
            if (matched != null) return matched

            // Fallback to Luar Jam if none matched
            return ShiftInfo("Luar Jam", "Luar Jam", 0, 0)
        }
    }
}
