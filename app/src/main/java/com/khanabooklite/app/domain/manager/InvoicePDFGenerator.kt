package com.khanabooklite.app.domain.manager

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import com.khanabooklite.app.data.local.entity.RestaurantProfileEntity
import com.khanabooklite.app.data.local.relation.BillWithItems
import java.io.File
import java.io.FileOutputStream

class InvoicePDFGenerator(private val context: Context) {

    fun generatePDF(bill: BillWithItems, profile: RestaurantProfileEntity?, isDigital: Boolean = true): File {
        val pdfDocument = PdfDocument()
        
        // 58mm = ~164 pts, 80mm = ~226 pts
        val is80mm = profile?.paperSize == "80mm"
        val pageWidth = if (is80mm) 226 else 164
        
        // Load Logo if exists
        val logoBitmap = profile?.logoPath?.let { path ->
            try { BitmapFactory.decodeFile(path) } catch (e: Exception) { null }
        }
        
        // Calculate height dynamically
        val logoHeight = if (logoBitmap != null) 40 else 0
        val itemHeight = bill.items.size * 15
        val headerHeight = 180 + logoHeight
        val summaryHeight = 150
        val taxHeight = if (profile?.gstEnabled == true) 20 else 0
        val footerHeight = 100
        val pageHeight = headerHeight + itemHeight + summaryHeight + taxHeight + footerHeight
        
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        
        val paint = Paint()
        val normalTypeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val boldTypeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        
        var y = 20f
        
        // 0. Draw Logo
        if (logoBitmap != null) {
            val scaledWidth = 40f
            val scaledHeight = (logoBitmap.height.toFloat() / logoBitmap.width.toFloat()) * scaledWidth
            val left = (pageWidth - scaledWidth) / 2
            val rect = RectF(left, y, left + scaledWidth, y + scaledHeight)
            canvas.drawBitmap(logoBitmap, null, rect, paint)
            y += scaledHeight + 10f
        }

        // Colors
        val colorPrimary = if (isDigital) Color.parseColor("#2E150B") else Color.BLACK 
        val colorVeg = if (isDigital) Color.parseColor("#2E7D32") else Color.BLACK
        val colorNonVeg = if (isDigital) Color.parseColor("#C62828") else Color.BLACK
        val colorText = Color.BLACK
        
        // Header scaling
        val mainTitleSize = if (is80mm) 14f else 11f
        val subTitleSize = if (is80mm) 8f else 7f
        
        // 1. Header (Centered)
        paint.color = colorPrimary
        paint.typeface = boldTypeface
        paint.textSize = mainTitleSize
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(profile?.shopName?.uppercase() ?: "RESTAURANT", (pageWidth / 2).toFloat(), y, paint)
        
        paint.color = colorText
        paint.typeface = normalTypeface
        paint.textSize = subTitleSize
        y += 12f
        profile?.shopAddress?.split(",")?.forEach { line ->
            if (line.isNotBlank()) {
                canvas.drawText(line.trim(), (pageWidth / 2).toFloat(), y, paint)
                y += 10f
            }
        }
        canvas.drawText("Mob: ${profile?.whatsappNumber ?: "N/A"}", (pageWidth / 2).toFloat(), y, paint)
        y += 10f
        if (profile?.gstEnabled == true && !profile.gstin.isNullOrBlank()) {
            canvas.drawText("GSTIN: ${profile.gstin}", (pageWidth / 2).toFloat(), y, paint)
            y += 10f
        }
        
        // 2. Divider & Title
        y += 5f
        paint.strokeWidth = 1f
        canvas.drawLine(5f, y, (pageWidth - 5).toFloat(), y, paint)
        y += 12f
        paint.typeface = boldTypeface
        paint.textSize = 9f
        canvas.drawText(if (profile?.gstEnabled == true) "TAX INVOICE" else "INVOICE", (pageWidth / 2).toFloat(), y, paint)
        y += 5f
        canvas.drawLine(5f, y, (pageWidth - 5).toFloat(), y, paint)
        
        // 3. Bill Info
        y += 12f
        paint.typeface = normalTypeface
        paint.textSize = 7f
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("Bill: #${bill.bill.lifetimeOrderId}", 5f, y, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Counter: ${bill.bill.dailyOrderDisplay}", (pageWidth - 5).toFloat(), y, paint)
        
        y += 10f
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("Date: ${bill.bill.createdAt.take(10)}", 5f, y, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Time: ${bill.bill.createdAt.drop(11).take(8)}", (pageWidth - 5).toFloat(), y, paint)
        
        // 4. Table Header
        y += 12f
        canvas.drawLine(5f, y, (pageWidth - 5).toFloat(), y, paint)
        y += 10f
        paint.typeface = boldTypeface
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("DESCRIPTION", 5f, y, paint)
        
        val qtyX = if (is80mm) 130f else 90f
        val priceX = if (is80mm) 165f else 120f
        
        canvas.drawText("QTY", qtyX, y, paint)
        canvas.drawText("RATE", priceX, y, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("AMT", (pageWidth - 5).toFloat(), y, paint)
        y += 5f
        canvas.drawLine(5f, y, (pageWidth - 5).toFloat(), y, paint)
        
        // 5. Items
        paint.typeface = normalTypeface
        y += 12f
        bill.items.forEachIndexed { _, item ->
            paint.textAlign = Paint.Align.LEFT
            val isVeg = !(item.itemName.contains("Chicken", true) || item.itemName.contains("Mutton", true) || item.itemName.contains("Egg", true) || item.itemName.contains("Fish", true))
            paint.color = if (isVeg) colorVeg else colorNonVeg
            canvas.drawCircle(8f, y - 2.5f, 2f, paint)
            
            paint.color = colorText
            val displayName = item.itemName.uppercase()
            canvas.drawText(if (displayName.length > 15) displayName.take(13) + ".." else displayName, 15f, y, paint)
            
            canvas.drawText("${item.quantity}", qtyX + 5f, y, paint)
            canvas.drawText(String.format("%.0f", item.price), priceX, y, paint)
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(String.format("%.2f", item.itemTotal), (pageWidth - 5).toFloat(), y, paint)
            y += 12f
        }
        
        // 6. Summary
        y += 5f
        paint.color = colorText
        canvas.drawLine(5f, y, (pageWidth - 5).toFloat(), y, paint)
        y += 12f
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("Sub-Total", 5f, y, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(String.format("%.2f", bill.bill.subtotal), (pageWidth - 5).toFloat(), y, paint)
        
        // GST Row - ONLY if enabled
        if (profile?.gstEnabled == true) {
            y += 10f
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText("GST (${bill.bill.gstPercentage}%)", 5f, y, paint)
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(String.format("%.2f", bill.bill.cgstAmount + bill.bill.sgstAmount), (pageWidth - 5).toFloat(), y, paint)
        }
        
        y += 18f
        paint.typeface = boldTypeface
        paint.textSize = 11f
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("NET AMOUNT", 5f, y, paint)
        paint.textAlign = Paint.Align.RIGHT
        val currency = if (profile?.currency == "INR" || profile?.currency == "Rupee") "₹" else profile?.currency ?: ""
        canvas.drawText("$currency ${String.format("%.2f", bill.bill.totalAmount)}", (pageWidth - 5).toFloat(), y, paint)
        
        // 8. Footer
        paint.typeface = boldTypeface
        paint.textSize = 7f
        paint.textAlign = Paint.Align.CENTER
        y += 20f
        
        if (isDigital) {
            paint.color = colorPrimary
            canvas.drawRect(5f, y - 8f, (pageWidth - 5).toFloat(), y + 4f, paint)
            paint.color = Color.WHITE
        }
        
        canvas.drawText("THANK YOU! VISIT AGAIN", (pageWidth / 2).toFloat(), y, paint)
        
        paint.color = colorText
        paint.typeface = normalTypeface
        y += 12f
        canvas.drawText("Software by KhanaBook", (pageWidth / 2).toFloat(), y, paint)
        
        pdfDocument.finishPage(page)
        
        val file = File(context.cacheDir, "invoice_${bill.bill.lifetimeOrderId}.pdf")
        pdfDocument.writeTo(FileOutputStream(file))
        pdfDocument.close()
        
        return file
    }
}
