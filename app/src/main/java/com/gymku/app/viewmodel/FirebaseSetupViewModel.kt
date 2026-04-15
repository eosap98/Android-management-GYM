package com.gymku.app.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymku.app.GymApplication
import com.gymku.app.data.model.Admin
import com.gymku.app.data.model.FirebaseConfig
import com.gymku.app.data.preferences.AppPreferences
import com.gymku.app.data.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class FirebaseSetupState {
    object Idle : FirebaseSetupState()
    object Loading : FirebaseSetupState()
    object Success : FirebaseSetupState()
    data class Error(val message: String) : FirebaseSetupState()
}

class FirebaseSetupViewModel : ViewModel() {

    private val _state = MutableStateFlow<FirebaseSetupState>(FirebaseSetupState.Idle)
    val state: StateFlow<FirebaseSetupState> = _state.asStateFlow()

    fun saveAndInit(context: Context, config: FirebaseConfig) {
        viewModelScope.launch {
            _state.value = FirebaseSetupState.Loading
            try {
                // Initialize Firebase programmatically
                GymApplication.initFirebaseWith(
                    context = context,
                    databaseUrl = config.databaseUrl,
                    projectId   = config.projectId,
                    appId       = config.appId,
                    apiKey      = config.apiKey
                )
                // Save to DataStore
                val prefs = AppPreferences(context)
                prefs.saveFirebaseConfig(config)

                // Seed default admin
                AdminRepository().seedDefaultAdminIfNeeded()

                _state.value = FirebaseSetupState.Success
            } catch (e: Exception) {
                _state.value = FirebaseSetupState.Error(e.message ?: "Terjadi kesalahan")
            }
        }
    }

    fun resetState() { _state.value = FirebaseSetupState.Idle }
}
