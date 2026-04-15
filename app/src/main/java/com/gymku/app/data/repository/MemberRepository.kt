package com.gymku.app.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.gymku.app.data.model.CheckIn
import com.gymku.app.data.model.Member
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MemberRepository {

    private val db get() = FirebaseDatabase.getInstance().reference

    fun getAllMembers(): Flow<List<Member>> = callbackFlow {
        val ref = db.child("members")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { child ->
                    child.getValue(Member::class.java)?.copy(id = child.key ?: "")
                }.sortedBy { it.name }
                trySend(list)
            }
            override fun onCancelled(error: DatabaseError) { close(error.toException()) }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun getMemberById(memberId: String): Member? {
        return try {
            val snapshot = db.child("members").child(memberId).get().await()
            snapshot.getValue(Member::class.java)?.copy(id = snapshot.key ?: "")
        } catch (e: Exception) { null }
    }

    suspend fun getMemberByQr(qrCode: String): Member? {
        return try {
            // Full fallback scan: fetching all members and filtering locally.
            // Bypasses missing Firebase ".indexOn" rules which silently fail.
            val snapshot = db.child("members").get().await()
            val list = snapshot.children.mapNotNull { it.getValue(Member::class.java)?.copy(id = it.key ?: "") }
            list.firstOrNull { it.qrCode == qrCode }
        } catch (e: Exception) { null }
    }

    suspend fun addMember(member: Member): String {
        val key = db.child("members").push().key ?: throw Exception("Failed to generate key")
        // Use the Firebase push key for both ID and QR Code to ensure uniqueness and fast lookup
        val newMember = member.copy(id = key, qrCode = key)
        db.child("members").child(key).setValue(newMember).await()
        return key
    }

    suspend fun updateMember(member: Member) {
        db.child("members").child(member.id).setValue(member).await()
    }

    suspend fun renewMember(memberId: String, months: Int): Member {
        val member = getMemberById(memberId) ?: throw Exception("Member tidak ditemukan")
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentExpire = try { sdf.parse(member.expireDate) } catch (e: Exception) { null }
        val baseDate: Date = if (currentExpire != null && currentExpire.after(Date())) currentExpire else Date()
        val newExpireDate = Member.calculateExpireDate(baseDate, months)
        val updated = member.copy(expireDate = newExpireDate, isActive = true)
        updateMember(updated)
        return updated
    }

    suspend fun checkInMember(
        member: Member,
        adminId: String,
        adminName: String,
        shift: String,
        lockerNumber: String = ""
    ) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = sdf.format(Date())
        val checkIn = CheckIn(
            memberId = member.id,
            memberName = member.name,
            adminId = adminId,
            adminName = adminName,
            shift = shift,
            timestamp = System.currentTimeMillis(),
            date = today,
            lockerNumber = lockerNumber
        )
        val key = db.child("checkins").child(today).push().key ?: return
        db.child("checkins").child(today).child(key).setValue(checkIn.copy(id = key)).await()
        // Update lastCheckIn on member
        db.child("members").child(member.id).child("lastCheckIn").setValue(today).await()
    }

    fun getTodayCheckInCount(): Flow<Int> = callbackFlow {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = sdf.format(Date())
        val ref = db.child("checkins").child(today)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) { trySend(snapshot.childrenCount.toInt()) }
            override fun onCancelled(error: DatabaseError) { close(error.toException()) }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun getTodayCheckIns(): Flow<List<CheckIn>> = callbackFlow {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = sdf.format(Date())
        val ref = db.child("checkins").child(today)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { child ->
                    child.getValue(CheckIn::class.java)?.copy(id = child.key ?: "")
                }.sortedByDescending { it.timestamp }
                trySend(list)
            }
            override fun onCancelled(error: DatabaseError) { close(error.toException()) }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun getMemberCount(): Int {
        return try {
            db.child("members").get().await().childrenCount.toInt()
        } catch (e: Exception) { 0 }
    }

    suspend fun deleteMember(memberId: String) {
        db.child("members").child(memberId).removeValue().await()
    }
}
