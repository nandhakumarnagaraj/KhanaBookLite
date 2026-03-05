package com.khanabook.lite.pos.domain.manager

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import com.khanabook.lite.pos.data.local.entity.RestaurantProfileEntity
import com.khanabook.lite.pos.data.local.relation.BillWithItems
import java.io.File
import java.io.FileOutputStream

class InvoicePDFGenerator(private val context: Context) {

    fun generatePDF(
            bill: BillWithItems,
            profile: RestaurantProfileEntity?,
            isDigital: Boolean = true
    ): File {
        val pdfDocument = PdfDocument()

        // 58mm = ~164 pts, 80mm = ~226 pts
        val is80mm = profile?.paperSize == "80mm"
        val pageWidth = if (is80mm) 226 else 164

        // Load Logo if exists
        val logoBitmap =
                profile?.logoPath?.let { path ->
                    try {
                        BitmapFactory.decodeFile(path)
                    } catch (e: Exception) {
                        null
                    }
                }

        // 0. Build visual flags from profile
        val includeLogo = profile?.includeLogoInPrint == true
        val includeCustomerWhatsapp = profile?.printCustomerWhatsapp == true

        // Calculate height dynamically
        val logoHeight = if (logoBitmap != null && includeLogo) 40 else 0
        val whatsappHeight =
                if (includeCustomerWhatsapp && !bill.bill.customerWhatsapp.isNullOrBlank()) 10
                else 0
        val itemHeight = bill.items.size * 12 // Reduced from 15
        val headerHeight = 160 + logoHeight + whatsappHeight
        val summaryHeight = 120 // Reduced from 150
        val taxHeight = if (profile?.gstEnabled == true) 25 else 0
        val footerHeight = 80 // Reduced from 100
        val pageHeight = headerHeight + itemHeight + summaryHeight + taxHeight + footerHeight

        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint()
        val normalTypeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val boldTypeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

        var y = 15f

        // 0. Draw Logo (if enabled in settings)
        if (logoBitmap != null && includeLogo) {
            val scaledWidth = 35f
            val scaledHeight =
                    (logoBitmap.height.toFloat() / logoBitmap.width.toFloat()) * scaledWidth
            val left = (pageWidth - scaledWidth) / 2
            val rect = RectF(left, y, left + scaledWidth, y + scaledHeight)
            canvas.drawBitmap(logoBitmap, null, rect, paint)
            y += scaledHeight + 12f
        }

        // Colors
        val colorPrimary = if (isDigital) Color.parseColor("#2E150B") else Color.BLACK
        val colorVeg = if (isDigital) Color.parseColor("#2E7D32") else Color.BLACK
        val colorNonVeg = if (isDigital) Color.parseColor("#C62828") else Color.BLACK
        val colorText = Color.BLACK

        // Font sizes (Polished & Shrunk)
        val mainTitleSize = if (is80mm) 12f else 10f
        val subTitleSize = if (is80mm) 7f else 6f
        val bodySize = if (is80mm) 8f else 6.5f
        val headerLabelSize = if (is80mm) 7f else 6f

        // 1. Header (Centered)
        paint.color = colorPrimary
        paint.typeface = boldTypeface
        paint.textSize = mainTitleSize
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(
                profile?.shopName?.uppercase() ?: "RESTAURANT",
                (pageWidth / 2).toFloat(),
                y,
                paint
        )

        paint.color = colorText
        paint.typeface = normalTypeface
        paint.textSize = subTitleSize
        y += 10f
        profile?.shopAddress?.split(",")?.forEach { line ->
            if (line.isNotBlank()) {
                canvas.drawText(line.trim(), (pageWidth / 2).toFloat(), y, paint)
                y += 8f
            }
        }
        canvas.drawText(
                "Mob: ${profile?.whatsappNumber ?: "N/A"}",
                (pageWidth / 2).toFloat(),
                y,
                paint
        )
        y += 8f
        if (profile?.gstEnabled == true && !profile.gstin.isNullOrBlank()) {
            canvas.drawText("GSTIN: ${profile.gstin}", (pageWidth / 2).toFloat(), y, paint)
            y += 8f
        }

        // 2. Divider & Title
        y += 5f
        paint.strokeWidth = 1f
        canvas.drawLine(5f, y, (pageWidth - 5).toFloat(), y, paint)
        y += 12f
        paint.typeface = boldTypeface
        paint.textSize = 9f
        canvas.drawText(
                if (profile?.gstEnabled == true) "TAX INVOICE" else "INVOICE",
                (pageWidth / 2).toFloat(),
                y,
                paint
        )
        y += 5f
        canvas.drawLine(5f, y, (pageWidth - 5).toFloat(), y, paint)

        // 3. Bill Info
        y += 10f
        paint.typeface = normalTypeface
        paint.textSize = headerLabelSize
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("BILL: #${bill.bill.lifetimeOrderId}", 5f, y, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("CNT: ${bill.bill.dailyOrderDisplay}", (pageWidth - 5).toFloat(), y, paint)

        y += 8f
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("DATE: ${bill.bill.createdAt.take(10)}", 5f, y, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(
                "TIME: ${bill.bill.createdAt.drop(11).take(5)}",
                (pageWidth - 5).toFloat(),
                y,
                paint
        )

        // Customer Info (If enabled in settings)
        if (includeCustomerWhatsapp && !bill.bill.customerWhatsapp.isNullOrBlank()) {
            y += 8f
            paint.textAlign = Paint.Align.LEFT
            val custName = bill.bill.customerName ?: "GUEST"
            canvas.drawText("CUST: $custName", 5f, y, paint)
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(
                    "WA: ${bill.bill.customerWhatsapp}",
                    (pageWidth - 5).toFloat(),
                    y,
                    paint
            )
        }

        // 4. Table Header
        y += 10f
        canvas.drawLine(5f, y, (pageWidth - 5).toFloat(), y, paint)
        y += 8f
        paint.typeface = boldTypeface
        paint.textSize = headerLabelSize
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("ITEM DESCRIPTION", 5f, y, paint)

        val qtyX = if (is80mm) 120f else 85f
        val priceX = if (is80mm) 160f else 115f

        canvas.drawText("QTY", qtyX, y, paint)
        // Adjust Rate X to be slightly left of Amt
        canvas.drawText("RATE", priceX, y, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("AMT", (pageWidth - 5).toFloat(), y, paint)
        y += 4f
        canvas.drawLine(5f, y, (pageWidth - 5).toFloat(), y, paint)

        // 5. Items
        paint.typeface = normalTypeface
        paint.textSize = bodySize
        y += 10f
        bill.items.forEachIndexed { _, item ->
            paint.textAlign = Paint.Align.LEFT
            val isVeg =
                    !(item.itemName.contains("Chicken", true) ||
                            item.itemName.contains("Mutton", true) ||
                            item.itemName.contains("Egg", true) ||
                            item.itemName.contains("Fish", true))
            paint.color = if (isVeg) colorVeg else colorNonVeg
            canvas.drawCircle(8f, y - 2.2f, 1.8f, paint)

            paint.color = colorText
            val displayName = item.itemName.uppercase()
            canvas.drawText(
                    if (displayName.length > 17) displayName.take(15) + ".." else displayName,
                    15f,
                    y,
                    paint
            )

            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("${item.quantity}", qtyX + 8f, y, paint)

            paint.textAlign = Paint.Align.LEFT
            canvas.drawText(String.format("%.0f", item.price), priceX, y, paint)

            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(
                    String.format("%.2f", item.itemTotal),
                    (pageWidth - 5).toFloat(),
                    y,
                    paint
            )
            y += 10f
        }

        // 6. Summary
        y += 4f
        paint.color = colorText
        canvas.drawLine(5f, y, (pageWidth - 5).toFloat(), y, paint)
        y += 10f
        paint.typeface = normalTypeface
        paint.textSize = bodySize
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("Sub-Total", 15f, y, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(
                String.format("%.2f", bill.bill.subtotal),
                (pageWidth - 15).toFloat(),
                y,
                paint
        )

        // GST Row - ONLY if enabled
        if (profile?.gstEnabled == true) {
            y += 9f
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText("GST (${bill.bill.gstPercentage}%)", 15f, y, paint)
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(
                    String.format("%.2f", bill.bill.cgstAmount + bill.bill.sgstAmount),
                    (pageWidth - 15).toFloat(),
                    y,
                    paint
            )
        }

        y += 15f
        paint.typeface = boldTypeface
        paint.textSize = bodySize + 1.5f
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("NET AMOUNT", 15f, y, paint)
        paint.textAlign = Paint.Align.RIGHT
        val currency =
                if (profile?.currency == "INR" || profile?.currency == "Rupee") "₹"
                else profile?.currency ?: ""
        canvas.drawText(
                "$currency ${String.format("%.2f", bill.bill.totalAmount)}",
                (pageWidth - 15).toFloat(),
                y,
                paint
        )

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


