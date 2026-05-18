package com.spendsnap.app.data.local

import android.content.Context
import androidx.core.content.edit

object CurrencyManager {
    private const val PREFS_NAME = "currency_prefs"
    private const val KEY_CURRENCY = "selected_currency"
    private const val KEY_SYMBOL = "selected_symbol"
    private const val DEFAULT_CURRENCY = "VND"
    private const val DEFAULT_SYMBOL = "đ"

    // Map code → symbol. Khi BE thêm currency mới, update map này (hoặc lookup từ CurrencyRepository cache).
    private val KNOWN_SYMBOLS = mapOf(
        "VND" to "đ",
        "USD" to "$"
    )

    fun getSavedCurrency(context: Context): String {
        return context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_CURRENCY, null)
            ?: DEFAULT_CURRENCY
    }

    fun getSavedSymbol(context: Context): String {
        return context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_SYMBOL, null)
            ?: DEFAULT_SYMBOL
    }

    fun symbolFor(code: String): String = KNOWN_SYMBOLS[code] ?: DEFAULT_SYMBOL

    fun saveCurrency(context: Context, code: String, symbol: String) {
        context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit {
                putString(KEY_CURRENCY, code)
                    .putString(KEY_SYMBOL, symbol)
            }
    }

    /**
     * Sync `user.currency` từ BE → SharedPreferences. Lookup symbol qua [symbolFor].
     * No-op khi code null/blank.
     */
    fun syncFromUserCurrency(context: Context, code: String?) {
        if (code.isNullOrBlank()) return
        saveCurrency(context, code, symbolFor(code))
    }
}
