package com.gymku.app.data.model

data class VisitorHistory(
    val name: String,
    val timestamp: Long,
    val type: String // "Member" or "Harian"
)
