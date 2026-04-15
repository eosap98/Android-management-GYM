package com.gymku.app.data.model

data class Visitor(
    val id: String = "",
    val name: String = "Tamu",
    val amount: Long = 10_000L,
    val paymentMethod: String = "Cash", // "Cash" or "QRIS"
    val adminId: String = "",
    val adminName: String = "",
    val shift: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val date: String = "" // "yyyy-MM-dd"
)
