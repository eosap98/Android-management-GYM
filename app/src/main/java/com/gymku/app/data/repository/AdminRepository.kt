package com.gymku.app.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.gymku.app.data.model.Admin
import com.gymku.app.data.model.ShiftInfo
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AdminRepository {

    private val db get() = FirebaseDatabase.getInstance().reference

    /** Attempt login — returns Admin if credentials match, null otherwise.
     *  Throws exception if Firebase is not reachable / not initialized. */
    suspend fun login(username: String, password: String): Admin? {
        // Primary: indexed query (fast) — may throw if .indexOn rule is missing
        try {
            val snapshot = db.child("admins")
                .orderByChild("username")
                .equalTo(username)
                .get()
                .await()
            val adminFromIndex = snapshot.children.firstOrNull()?.let { child ->
                val admin = child.getValue(Admin::class.java) ?: return@let null
                if (admin.password == password) admin.copy(id = child.key ?: "") else null
            }
            if (adminFromIndex != null) return adminFromIndex
        } catch (_: Exception) {
            // Index not defined in Firebase rules — fall through to linear scan below
        }

        // Fallback: linear scan — always works, no index rule required
        val allSnapshot = db.child("admins").get().await()
        return allSnapshot.children.firstOrNull { child ->
            child.getValue(Admin::class.java)?.username == username
        }?.let { child ->
            val admin = child.getValue(Admin::class.java) ?: return null
            if (admin.password == password) admin.copy(id = child.key ?: "") else null
        }
    }

    /** Ensure default admin exists at key "admin_default".
     *  Checks the specific key, so it still seeds even if other admins exist. */
    suspend fun seedDefaultAdminIfNeeded() {
        // If Firebase not ready, this will throw and be caught by caller
        val snapshot = db.child("admins").child("admin_default").get().await()
        if (!snapshot.exists()) {
            val defaultAdmin = Admin(
                id = "admin_default",
                name = "Admin",
                username = "admin",
                password = "admin123",
                role = "admin"
            )
            db.child("admins").child("admin_default").setValue(defaultAdmin).await()
        }
    }

    fun getAllAdmins(): Flow<List<Admin>> = callbackFlow {
        val ref = db.child("admins")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { child ->
                    child.getValue(Admin::class.java)?.copy(id = child.key ?: "")
                }
                trySend(list)
            }
            override fun onCancelled(error: DatabaseError) { close(error.toException()) }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun addAdmin(admin: Admin) {
        val key = if (admin.id.isNotEmpty()) admin.id else db.child("admins").push().key ?: return
        db.child("admins").child(key).setValue(admin.copy(id = key)).await()
    }

    suspend fun deleteAdmin(adminId: String) {
        db.child("admins").child(adminId).removeValue().await()
    }

    // --- Shift Management ---

    fun getAllShifts(): Flow<List<ShiftInfo>> = callbackFlow {
        val ref = db.child("shifts")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { child ->
                    child.getValue(ShiftInfo::class.java)?.copy(id = child.key ?: "")
                }
                trySend(list)
            }
            override fun onCancelled(error: DatabaseError) { close(error.toException()) }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun saveShift(shift: ShiftInfo) {
        val key = if (shift.id.isNotEmpty()) shift.id else db.child("shifts").push().key ?: return
        db.child("shifts").child(key).setValue(shift.copy(id = key)).await()
    }

    suspend fun deleteShift(shiftId: String) {
        db.child("shifts").child(shiftId).removeValue().await()
    }

    suspend fun seedDefaultShiftsIfNeeded() {
        val snapshot = db.child("shifts").get().await()
        if (!snapshot.exists() || snapshot.childrenCount == 0L) {
            saveShift(ShiftInfo("shift_pagi", "Pagi", 6, 14))
            saveShift(ShiftInfo("shift_siang", "Siang", 14, 22))
            saveShift(ShiftInfo("shift_malam", "Malam", 22, 6))
        }
    }

    // --- Staff Schedule Management ---

    fun getAllStaffSchedules(): Flow<List<com.gymku.app.data.model.StaffSchedule>> = callbackFlow {
        val ref = db.child("staff_schedules")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { child ->
                    child.getValue(com.gymku.app.data.model.StaffSchedule::class.java)?.copy(id = child.key ?: "")
                }
                trySend(list)
            }
            override fun onCancelled(error: DatabaseError) { close(error.toException()) }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun saveStaffSchedule(schedule: com.gymku.app.data.model.StaffSchedule) {
        val key = if (schedule.id.isNotEmpty()) schedule.id else db.child("staff_schedules").push().key ?: return
        db.child("staff_schedules").child(key).setValue(schedule.copy(id = key)).await()
    }

    suspend fun deleteStaffSchedule(scheduleId: String) {
        db.child("staff_schedules").child(scheduleId).removeValue().await()
    }
}
