package com.gymku.app.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.gymku.app.data.model.Transaction
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransactionRepository {

    private val db get() = FirebaseDatabase.getInstance().reference

    suspend fun addTransaction(tx: Transaction): String {
        val key = db.child("transactions").push().key ?: throw Exception("Failed to generate key")
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = sdf.format(Date())
        db.child("transactions").child(key)
            .setValue(tx.copy(id = key, date = today, timestamp = System.currentTimeMillis()))
            .await()
        return key
    }

    fun getTodayTransactions(): Flow<List<Transaction>> = callbackFlow {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = sdf.format(Date())
        val ref = db.child("transactions").orderByChild("date").equalTo(today)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { child ->
                    child.getValue(Transaction::class.java)?.copy(id = child.key ?: "")
                }.sortedByDescending { it.timestamp }
                trySend(list)
            }
            override fun onCancelled(error: DatabaseError) { close(error.toException()) }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun getAllTransactions(): Flow<List<Transaction>> = callbackFlow {
        val ref = db.child("transactions").orderByChild("timestamp")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { child ->
                    child.getValue(Transaction::class.java)?.copy(id = child.key ?: "")
                }.sortedByDescending { it.timestamp }
                trySend(list)
            }
            override fun onCancelled(error: DatabaseError) { close(error.toException()) }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    /** Total revenue for today by payment method */
    suspend fun getTodayRevenueByCash(): Long {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = sdf.format(Date())
        return try {
            val snap = db.child("transactions").orderByChild("date").equalTo(today).get().await()
            snap.children
                .mapNotNull { it.getValue(Transaction::class.java) }
                .filter { it.paymentMethod == "Cash" }
                .sumOf { it.amount }
        } catch (e: Exception) { 0L }
    }

    suspend fun getTodayRevenueByQris(): Long {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = sdf.format(Date())
        return try {
            val snap = db.child("transactions").orderByChild("date").equalTo(today).get().await()
            snap.children
                .mapNotNull { it.getValue(Transaction::class.java) }
                .filter { it.paymentMethod == "QRIS" }
                .sumOf { it.amount }
        } catch (e: Exception) { 0L }
    }

    suspend fun deleteTransaction(txId: String) {
        db.child("transactions").child(txId).removeValue().await()
    }
}
