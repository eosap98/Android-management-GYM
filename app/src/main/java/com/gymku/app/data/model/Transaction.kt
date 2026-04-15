package com.gymku.app.data.model

data class Transaction(
    val id: String = "",
    val type: String = "",         // "new_member" | "renew" | "visitor"
    val memberId: String = "",
    val memberName: String = "",
    val amount: Long = 0L,
    val paymentMethod: String = "Cash", // "Cash" | "QRIS"
    val adminId: String = "",
    val adminName: String = "",
    val shift: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val date: String = "",         // "yyyy-MM-dd"
    val months: Int = 0,           // for renew transactions
    val notes: String = ""
) {
    companion object {
        const val TYPE_NEW_MEMBER = "new_member"
        const val TYPE_RENEW = "renew"
        const val TYPE_VISITOR = "visitor"
    }
}
