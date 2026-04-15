package com.gymku.app

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.gymku.app.data.preferences.AppPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "gymku_prefs")

class GymApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        tryInitFirebase()
    }

    /**
     * Attempt to initialize Firebase from saved config in DataStore.
     * Called on every app start — safe to call multiple times (checks getApps).
     */
    fun tryInitFirebase() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefs = AppPreferences(applicationContext)
                val config = prefs.getFirebaseConfig().firstOrNull()
                if (config != null && config.isValid() && FirebaseApp.getApps(applicationContext).isEmpty()) {
                    val options = FirebaseOptions.Builder()
                        .setDatabaseUrl(config.databaseUrl.trim())
                        .setProjectId(config.projectId.trim())
                        .setApplicationId(config.appId.trim())
                        .setApiKey(config.apiKey.trim())
                        .build()
                    FirebaseApp.initializeApp(applicationContext, options)
                }
            } catch (e: Exception) {
                // Firebase not configured yet — user will be directed to setup screen
            }
        }
    }

    companion object {
        fun initFirebaseWith(context: Context, databaseUrl: String, projectId: String, appId: String, apiKey: String) {
            try {
                if (FirebaseApp.getApps(context).isNotEmpty()) {
                    FirebaseApp.getInstance().delete()
                }
                val options = FirebaseOptions.Builder()
                    .setDatabaseUrl(databaseUrl.trim())
                    .setProjectId(projectId.trim())
                    .setApplicationId(appId.trim())
                    .setApiKey(apiKey.trim())
                    .build()
                FirebaseApp.initializeApp(context.applicationContext, options)
            } catch (e: Exception) {
                throw Exception("Gagal inisialisasi Firebase: ${e.message}")
            }
        }
    }
}
