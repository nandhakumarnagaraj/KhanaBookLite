package com.khanabook.lite.pos.domain.util

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.material.icons.Icons
import androidx.core.content.FileProvider
import com.khanabook.lite.pos.data.local.entity.RestaurantProfileEntity
import com.khanabook.lite.pos.data.local.relation.BillWithItems
import com.khanabook.lite.pos.domain.manager.InvoicePDFGenerator
import androidx.compose.material.icons.automirrored.filled.ArrowBack

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    private const val DISPLAY_FORMAT = "dd MMM yyyy, hh:mm a"
    private const val DB_FORMAT = "yyyy-MM-dd HH:mm:ss"

    fun formatDisplay(date: Date): String = SimpleDateFormat(DISPLAY_FORMAT, Locale.getDefault()).format(date)
    fun formatDb(date: Date): String = SimpleDateFormat(DB_FORMAT, Locale.getDefault()).format(date)
    
    fun parseDb(dateStr: String): Date? = try {
        SimpleDateFormat(DB_FORMAT, Locale.getDefault()).parse(dateStr)
    } catch (e: Exception) {
        null
    }
}

object CurrencyUtils {
    fun formatPrice(amount: Double, currency: String = "\u20b9"): String {
        return "$currency ${String.format("%.2f", amount)}"
    }
}

object ValidationUtils {
    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidPhone(phone: String): Boolean {
        return phone.length >= 10
    }
}

fun Context.findActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

fun shareBillAsPdf(context: Context, billWithItems: BillWithItems, profile: RestaurantProfileEntity?) {
    try {
        val pdfGenerator = InvoicePDFGenerator(context)
        val pdfFile = pdfGenerator.generatePDF(billWithItems, profile)
        val pdfUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            pdfFile
        )

        val phone = billWithItems.bill.customerWhatsapp
        val formattedPhone = if (!phone.isNullOrBlank()) {
            if (phone.length == 10) "91$phone" else phone
        } else null

        if (formattedPhone != null) {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, pdfUri)
                putExtra("jid", "$formattedPhone@s.whatsapp.net")
                `package` = "com.whatsapp"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, pdfUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(fallbackIntent, "Share Invoice PDF"))
            }
        } else {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, pdfUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share Invoice PDF"))
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Error sharing PDF: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

fun openBillToPrint(context: Context, billWithItems: BillWithItems, profile: RestaurantProfileEntity?) {
    try {
        val pdfGenerator = InvoicePDFGenerator(context)
        val pdfFile = pdfGenerator.generatePDF(billWithItems, profile)
        val pdfUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            pdfFile
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(pdfUri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Open PDF to Print"))
    } catch (e: Exception) {
        Toast.makeText(context, "Error opening printer: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}


