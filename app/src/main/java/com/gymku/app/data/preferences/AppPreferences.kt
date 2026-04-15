package com.gymku.app.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.gymku.app.data.model.Admin
import com.gymku.app.data.model.FirebaseConfig
import com.gymku.app.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AppPreferences(private val context: Context) {

    companion object {
        private val KEY_DB_URL    = stringPreferencesKey("firebase_db_url")
        private val KEY_PROJECT_ID = stringPreferencesKey("firebase_project_id")
        private val KEY_APP_ID    = stringPreferencesKey("firebase_app_id")
        private val KEY_API_KEY   = stringPreferencesKey("firebase_api_key")

        private val KEY_ADMIN_ID       = stringPreferencesKey("logged_admin_id")
        private val KEY_ADMIN_NAME     = stringPreferencesKey("logged_admin_name")
        private val KEY_ADMIN_USERNAME = stringPreferencesKey("logged_admin_username")
        private val KEY_ADMIN_ROLE     = stringPreferencesKey("logged_admin_role")
    }

    // ── Firebase Config ──────────────────────────────────────────────────────

    suspend fun saveFirebaseConfig(config: FirebaseConfig) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DB_URL]     = config.databaseUrl
            prefs[KEY_PROJECT_ID] = config.projectId
            prefs[KEY_APP_ID]     = config.appId
            prefs[KEY_API_KEY]    = config.apiKey
        }
    }

    fun getFirebaseConfig(): Flow<FirebaseConfig?> =
        context.dataStore.data.map { prefs ->
            val url = prefs[KEY_DB_URL] ?: return@map null
            FirebaseConfig(
                databaseUrl = url,
                projectId   = prefs[KEY_PROJECT_ID] ?: "",
                appId       = prefs[KEY_APP_ID]     ?: "",
                apiKey      = prefs[KEY_API_KEY]    ?: ""
            ).takeIf { it.isValid() }
        }

    // ── Logged Admin ─────────────────────────────────────────────────────────

    suspend fun saveLoggedAdmin(admin: Admin) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ADMIN_ID]       = admin.id
            prefs[KEY_ADMIN_NAME]     = admin.name
            prefs[KEY_ADMIN_USERNAME] = admin.username
            prefs[KEY_ADMIN_ROLE]     = admin.role
        }
    }

    fun getLoggedAdmin(): Flow<Admin?> =
        context.dataStore.data.map { prefs ->
            val id = prefs[KEY_ADMIN_ID] ?: return@map null
            Admin(
                id       = id,
                name     = prefs[KEY_ADMIN_NAME]     ?: "",
                username = prefs[KEY_ADMIN_USERNAME]  ?: "",
                role     = prefs[KEY_ADMIN_ROLE]      ?: "staff"
            )
        }

    suspend fun clearLoggedAdmin() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_ADMIN_ID)
            prefs.remove(KEY_ADMIN_NAME)
            prefs.remove(KEY_ADMIN_USERNAME)
            prefs.remove(KEY_ADMIN_ROLE)
        }
    }
}
