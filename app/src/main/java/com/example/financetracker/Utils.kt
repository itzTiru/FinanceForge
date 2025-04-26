package com.example.financetracker

import android.content.Context

object Utils {
    fun getCurrencySymbol(context: Context): String {
        val sharedPreferences = context.getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE)
        val currency = sharedPreferences.getString("currency", "USD ($)") ?: "USD ($)"
        return when (currency) {
            "USD ($)" -> "$"
            "EUR (€)" -> "€"
            "GBP (£)" -> "£"
            "JPY (¥)" -> "¥"
            "INR (₹)" -> "₹"
            else -> "$"
        }
    }
}