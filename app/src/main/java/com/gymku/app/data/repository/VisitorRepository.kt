package com.gymku.app.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.gymku.app.data.model.Visitor
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class VisitorRepository {

    private val db get() = FirebaseDatabase.getInstance().reference

    suspend fun addVisitor(visitor: Visitor): String {
        val key = db.child("visitors").push().key ?: throw Exception("Failed to generate key")
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = sdf.format(Date())
        db.child("visitors").child(key)
            .setValue(visitor.copy(id = key, date = today, timestamp = System.currentTimeMillis()))
            .await()
        return key
    }

    fun getTodayVisitors(): Flow<List<Visitor>> = callbackFlow {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = sdf.format(Date())
        val ref = db.child("visitors").orderByChild("date").equalTo(today)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { child ->
                    child.getValue(Visitor::class.java)?.copy(id = child.key ?: "")
                }.sortedByDescending { it.timestamp }
                trySend(list)
            }
            override fun onCancelled(error: DatabaseError) { close(error.toException()) }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun getTodayVisitorCount(): Flow<Int> = callbackFlow {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = sdf.format(Date())
        val ref = db.child("visitors").orderByChild("date").equalTo(today)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) { trySend(snapshot.childrenCount.toInt()) }
            override fun onCancelled(error: DatabaseError) { close(error.toException()) }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }
}
