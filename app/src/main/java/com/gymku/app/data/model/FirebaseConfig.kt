package com.gymku.app.data.model

data class FirebaseConfig(
    val databaseUrl: String = "",
    val projectId: String = "",
    val appId: String = "",
    val apiKey: String = ""
) {
    fun isValid(): Boolean =
        databaseUrl.isNotBlank() &&
        projectId.isNotBlank() &&
        appId.isNotBlank() &&
        apiKey.isNotBlank()
}
