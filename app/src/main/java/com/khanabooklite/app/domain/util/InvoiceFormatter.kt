package com.khanabooklite.app.domain.util

import com.khanabooklite.app.data.local.relation.BillWithItems
import com.khanabooklite.app.data.local.entity.RestaurantProfileEntity

object InvoiceFormatter {

    fun formatForWhatsApp(bill: BillWithItems, profile: RestaurantProfileEntity?): String {
        return formatForPrinter(bill, profile, 32) // Default to 32 chars for digital
    }

    fun formatForPrinter(bill: BillWithItems, profile: RestaurantProfileEntity?, charsPerLine: Int = 32): String {
        val sb = StringBuilder()
        val currency = if (profile?.currency == "INR" || profile?.currency == "Rupee") "₹" else profile?.currency ?: ""
        val isGst = profile?.gstEnabled == true
        
        val width = charsPerLine
        val line = "-".repeat(width)
        val doubleLine = "=".repeat(width)

        // Header
        sb.append("$doubleLine\n")
        sb.append(centerText(profile?.shopName?.uppercase() ?: "RESTAURANT", width) + "\n")
        profile?.shopAddress?.takeIf { it.isNotBlank() }?.let { address ->
            sb.append(centerText(address, width) + "\n")
        }
        if (!profile?.whatsappNumber.isNullOrBlank()) sb.append(centerText("Contact: ${profile?.whatsappNumber}", width) + "\n")
        
        if (isGst && !profile?.gstin.isNullOrBlank()) {
            sb.append(centerText("GSTIN: ${profile?.gstin}", width) + "\n")
        }
        
        sb.append("$doubleLine\n")
        sb.append(centerText(if (isGst) "TAX INVOICE" else "INVOICE", width) + "\n")
        sb.append("$line\n")
        
        sb.append("Daily Order : ${bill.bill.dailyOrderDisplay}\n")
        sb.append("Order #     : ${bill.bill.lifetimeOrderId.toString().padStart(5, '0')}\n")
        sb.append("Date        : ${bill.bill.createdAt}\n")
        bill.bill.customerName?.takeIf { it.isNotBlank() }?.let { sb.append("Customer    : $it\n") }
        
        sb.append("$line\n")
        
        // Dynamic Column Widths
        // ITEM(Width-17) QTY(3) RATE(7) AMT(7) -> 34 total? No, lets scale.
        val itemW = (width * 0.45).toInt()
        val qtyW = 4
        val rateW = (width * 0.2).toInt()
        val amtW = width - itemW - qtyW - rateW - 3
        
        val headerFormat = "%-${itemW}s %${qtyW}s %${rateW}s %${amtW}s\n"
        sb.append(String.format(headerFormat, "ITEM", "QTY", "RATE", "AMT"))
        sb.append("$line\n")
        
        for (item in bill.items) {
            val name = if (item.variantName != null) "${item.itemName} (${item.variantName})" else item.itemName
            sb.append(String.format(headerFormat, name.take(itemW), item.quantity, "%.0f".format(item.price), "%.0f".format(item.itemTotal)))
        }
        
        sb.append("$line\n")
        
        // Summary
        sb.append(formatRow("Subtotal:", "$currency ${"%.2f".format(bill.bill.subtotal)}", width))
        
        if (isGst && bill.bill.cgstAmount > 0) {
            val halfGst = bill.bill.gstPercentage / 2
            sb.append(formatRow("CGST (%.1f%%):".format(halfGst), "$currency ${"%.2f".format(bill.bill.cgstAmount)}", width))
            sb.append(formatRow("SGST (%.1f%%):".format(halfGst), "$currency ${"%.2f".format(bill.bill.sgstAmount)}", width))
        }
        
        if (!isGst && bill.bill.customTaxAmount > 0) {
            sb.append(formatRow("Tax:", "$currency ${"%.2f".format(bill.bill.customTaxAmount)}", width))
        }
        
        sb.append("$line\n")
        sb.append(formatRow("TOTAL AMOUNT:", "$currency ${"%.2f".format(bill.bill.totalAmount)}", width))
        sb.append("$line\n")
        
        sb.append("Payment Mode : ${bill.bill.paymentMode.uppercase()}\n")
        
        sb.append("$doubleLine\n")
        sb.append(centerText("Thank you for visiting us!", width) + "\n")
        sb.append(centerText("Have a great day!", width) + "\n")
        sb.append("$doubleLine\n")
        
        return sb.toString()
    }

    private fun centerText(text: String, width: Int): String {
        if (text.length >= width) return text.take(width)
        val padding = kotlin.math.max(0, (width - text.length) / 2)
        return " ".repeat(padding) + text
    }

    private fun formatRow(label: String, value: String, width: Int): String {
        val spaceCount = width - label.length - value.length
        return if (spaceCount > 0) label + " ".repeat(spaceCount) + value + "\n"
        else "$label\n${" ".repeat(width - value.length)}$value\n"
    }
}
