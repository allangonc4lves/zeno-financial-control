package br.dev.allan.controlefinanceiro.utils

import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrencyManager @Inject constructor() {

    fun getFormatter(locale: Locale): NumberFormat {
        return NumberFormat.getCurrencyInstance(locale)
    }

    fun formatByCurrencyCode(amount: Double, code: String): String {
        val locale = when (code) {
            "USD" -> Locale.US
            "EUR" -> Locale.FRANCE
            "ARS" -> Locale("es", "AR")
            else -> Locale("pt", "BR")
        }
        return NumberFormat.getCurrencyInstance(locale).format(amount)
    }
}