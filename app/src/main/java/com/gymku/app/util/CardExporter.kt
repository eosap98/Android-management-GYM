package com.gymku.app.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.core.content.FileProvider
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.gymku.app.data.model.Member
import java.io.File
import java.io.FileOutputStream

object CardExporter {

    fun exportCardAndShare(context: Context, member: Member) {
        Toast.makeText(context, "Sedang menyiapkan Kartu Member...", Toast.LENGTH_SHORT).show()

        val document = PdfDocument()
        // ID Card standard size (CR80) is ~54mm x 86mm. Let's use points: 153 x 243 for portrait.
        // We'll scale it up for better resolution: 306 x 486
        val pageInfo = PdfDocument.PageInfo.Builder(306, 486, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        // Background
        val paint = Paint()
        paint.color = android.graphics.Color.parseColor("#0F172A") // Slate900
        canvas.drawRect(0f, 0f, 306f, 486f, paint)

        // Accent header
        paint.color = android.graphics.Color.parseColor("#4F46E5") // IndigoMain
        canvas.drawRect(0f, 0f, 306f, 100f, paint)

        // Title
        paint.color = android.graphics.Color.WHITE
        paint.textSize = 24f
        paint.isFakeBoldText = true
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("GYMKU", 153f, 45f, paint)

        paint.textSize = 14f
        paint.isFakeBoldText = false
        canvas.drawText("MEMBER CARD", 153f, 70f, paint)

        // QR Code Box
        val qrBitmap = generateQrBitmap(member.qrCode)
        if (qrBitmap != null) {
            val qrRect = RectF(73f, 130f, 233f, 290f)
            paint.color = android.graphics.Color.WHITE
            canvas.drawRoundRect(qrRect, 10f, 10f, paint) // border
            canvas.drawBitmap(qrBitmap, null, RectF(83f, 140f, 223f, 280f), null)
        }

        // Member Info
        paint.color = android.graphics.Color.WHITE
        paint.textAlign = Paint.Align.CENTER
        
        paint.textSize = 22f
        paint.isFakeBoldText = true
        canvas.drawText(member.name.take(20), 153f, 340f, paint)

        paint.textSize = 12f
        paint.isFakeBoldText = false
        paint.color = android.graphics.Color.parseColor("#94A3B8") // Slate400
        canvas.drawText("ID: ${member.id}", 153f, 365f, paint)

        paint.color = android.graphics.Color.WHITE
        canvas.drawText("Bergabung: ${member.joinDate}", 153f, 410f, paint)
        
        paint.color = android.graphics.Color.parseColor("#10B981") // EmeraldGreen
        paint.isFakeBoldText = true
        canvas.drawText("Valid s/d: ${member.expireDate}", 153f, 435f, paint)

        document.finishPage(page)

        val fileName = "Kartu_Member_${member.name.replace(" ", "_")}.pdf"
        val file = File(context.filesDir, fileName)

        try {
            FileOutputStream(file).use { out ->
                document.writeTo(out)
            }
            shareFile(context, file)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error: $e", Toast.LENGTH_LONG).show()
        } finally {
            document.close()
        }
    }

    private fun generateQrBitmap(text: String): Bitmap? {
        return try {
            val hints = mapOf(EncodeHintType.MARGIN to 1)
            val bits = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, 200, 200, hints)
            val bmp = Bitmap.createBitmap(200, 200, Bitmap.Config.RGB_565)
            for (x in 0 until 200) for (y in 0 until 200)
                bmp.setPixel(x, y, if (bits[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            bmp
        } catch (e: Exception) { null }
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
            context.startActivity(Intent.createChooser(intent, "Bagikan Kartu Member"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Gagal membagikan kartu", Toast.LENGTH_SHORT).show()
        }
    }
}
