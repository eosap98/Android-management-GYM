package com.gymku.app.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.core.content.FileProvider
import com.gymku.app.viewmodel.ReportData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

object ReportExporter {

    fun exportToPdfAndShare(context: Context, data: ReportData, filterLabel: String) {
        if (data.transactions.isEmpty()) {
            Toast.makeText(context, "Tidak ada data transaksi untuk diekspor", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(context, "Sedang menyiapkan PDF...", Toast.LENGTH_SHORT).show()

        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()
        val nf = NumberFormat.getInstance(Locale("id", "ID"))
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        var y = 40f

        try {
            // Title
            paint.textSize = 20f
            paint.isFakeBoldText = true
            canvas.drawText("Laporan Keuangan GymKu", 40f, y, paint)
            y += 30f

            paint.textSize = 12f
            paint.isFakeBoldText = false
            canvas.drawText("Periode: $filterLabel", 40f, y, paint)
            y += 20f
            canvas.drawText("Dicetak pada: ${sdf.format(Date())}", 40f, y, paint)
            y += 40f

            // Summary
            paint.isFakeBoldText = true
            canvas.drawText("Ringkasan Pendapatan", 40f, y, paint)
            y += 20f
            paint.isFakeBoldText = false
            canvas.drawText("Total Pendapatan: Rp ${nf.format(data.totalRevenue)}", 60f, y, paint)
            y += 20f
            canvas.drawText("Cash: Rp ${nf.format(data.totalCash)}", 60f, y, paint)
            y += 20f
            canvas.drawText("QRIS: Rp ${nf.format(data.totalQris)}", 60f, y, paint)
            y += 40f

            // Breakdown
            paint.isFakeBoldText = true
            canvas.drawText("Statistik Aktivitas", 40f, y, paint)
            y += 20f
            paint.isFakeBoldText = false
            canvas.drawText("Member Baru: ${data.newMemberCount}", 60f, y, paint)
            y += 20f
            canvas.drawText("Perpanjangan: ${data.renewCount}", 60f, y, paint)
            y += 20f
            canvas.drawText("Tamu Harian: ${data.visitorCount}", 60f, y, paint)
            y += 40f

            // Transaction List Header
            paint.isFakeBoldText = true
            canvas.drawText("Daftar Transaksi", 40f, y, paint)
            y += 25f
            
            paint.textSize = 10f
            canvas.drawText("Waktu", 40f, y, paint)
            canvas.drawText("Nama", 100f, y, paint)
            canvas.drawText("Tipe", 250f, y, paint)
            canvas.drawText("Metode", 350f, y, paint)
            canvas.drawText("Jumlah", 450f, y, paint)
            y += 10f
            canvas.drawLine(40f, y, 550f, y, paint)
            y += 20f

            paint.isFakeBoldText = false
            val timeSdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            
            data.transactions.take(25).forEach { tx -> // Limit for simple layout
                if (y > 800) return@forEach
                val time = timeSdf.format(Date(tx.timestamp))
                canvas.drawText(time, 40f, y, paint)
                canvas.drawText(tx.memberName.take(20), 100f, y, paint)
                val type = when(tx.type) {
                    "TYPE_NEW_MEMBER" -> "Baru"
                    "TYPE_RENEW" -> "Renew"
                    "TYPE_VISITOR" -> "Tamu"
                    else -> tx.type
                }
                canvas.drawText(type, 250f, y, paint)
                canvas.drawText(tx.paymentMethod, 350f, y, paint)
                canvas.drawText("Rp ${nf.format(tx.amount)}", 450f, y, paint)
                y += 20f
            }

            document.finishPage(page)

            val fileName = "Laporan_Gym_${System.currentTimeMillis()}.pdf"
            val file = File(context.filesDir, fileName)
            
            FileOutputStream(file).use { out ->
                document.writeTo(out)
            }
            shareFile(context, file)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error PDF: $e", Toast.LENGTH_LONG).show()
        } finally {
            document.close()
        }
    }

    private fun shareFile(context: Context, file: File) {
        try {
            val uri = FileProvider.getUriForFile(context, "com.gymku.app.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Bagikan Laporan Melalui"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Gagal membagikan file", Toast.LENGTH_SHORT).show()
        }
    }
}
