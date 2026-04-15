package com.gymku.app.data.model

data class StaffSchedule(
    val id: String = "",
    val date: String = "",     // Format: yyyy-MM-dd
    val adminId: String = "",
    val shiftName: String = "" // "Pagi", "Siang", "Malam", "Luar Jam", or "" (Bebas)
)
