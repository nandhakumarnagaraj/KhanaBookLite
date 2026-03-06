package com.khanabook.lite.pos.domain.util

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.compose.material.icons.Icons
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


