package com.gymku.app.data.model

data class CheckIn(
    val id: String = "",
    val memberId: String = "",
    val memberName: String = "",
    val adminId: String = "",
    val adminName: String = "",
    val shift: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val date: String = "", // "yyyy-MM-dd"
    val lockerNumber: String = ""
)
