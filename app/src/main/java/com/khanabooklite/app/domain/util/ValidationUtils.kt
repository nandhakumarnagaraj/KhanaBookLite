package com.khanabooklite.app.domain.util

fun isValidPhone(phone: String): Boolean {
    return phone.length == 10 && phone.all { it.isDigit() }
}

fun isValidEmail(email: String): Boolean {
    if (email.isBlank()) return true // Email is often optional
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[a-z]{2,}$".toRegex()
    return email.matches(emailRegex)
}

fun isValidGst(gst: String): Boolean {
    if (gst.isBlank()) return true // Optional
    val gstRegex = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$".toRegex()
    return gst.matches(gstRegex)
}

fun isValidName(name: String): Boolean {
    return name.isNotBlank() && name.length >= 2
}

fun isValidPassword(password: String): Boolean {
    return password.length >= 6
}

fun isValidOtp(otp: String): Boolean {
    return otp.length == 6 && otp.all { it.isDigit() }
}

fun isValidTaxPercentage(percentage: String): Boolean {
    val value = percentage.toDoubleOrNull() ?: return false
    return value in 0.0..100.0
}
