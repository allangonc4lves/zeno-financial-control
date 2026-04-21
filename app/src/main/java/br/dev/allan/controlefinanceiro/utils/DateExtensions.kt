package br.dev.allan.controlefinanceiro.utils

import java.text.DateFormat
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateHelper {
    private val dbFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val uiFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    fun fromMillisToDb(millis: Long): String {
        return dbFormat.format(Date(millis))
    }

    fun fromUiToDb(inputDate: String): String {
        return try {
            val date = uiFormat.parse(inputDate)
            dbFormat.format(date ?: Date())
        } catch (e: Exception) {
            dbFormat.format(Date())
        }
    }

    fun fromDbToUi(dbDate: String): String {
        return try {
            val date = dbFormat.parse(dbDate)
            uiFormat.format(date ?: Date())
        } catch (e: Exception) {
            uiFormat.format(Date())
        }
    }
}

fun String.toSystemFormatDate(): String {
    return try {
        val date = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")).parse(this)
        DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault()).format(date ?: Date())
    } catch (e: Exception) {
        this
    }
}

fun String.toSystemDayMonth(): String {
    return try {
        val inputFormatter = SimpleDateFormat("dd/MM", Locale("pt", "BR"))
        val date = inputFormatter.parse(this) ?: Date()

        val pattern = android.text.format.DateFormat.getBestDateTimePattern(Locale.getDefault(), "MMMd")

        val outputFormatter = SimpleDateFormat(pattern, Locale.getDefault())
        outputFormatter.format(date)
    } catch (e: Exception) {
        this
    }
}

fun formatMillisToMonthYear(millis: Long): String {
    val date = java.util.Date(millis)
    val formatter = java.text.SimpleDateFormat("MM/yyyy", java.util.Locale.getDefault())
    return formatter.format(date)
}


fun String.formatAsCurrency(): String {
    val digits = this.filter { it.isDigit() }.take(9)
    val doubleValue = digits.toDoubleOrNull()?.div(100) ?: 0.0
    val symbols = DecimalFormatSymbols(Locale("pt", "BR")).apply {
        currencySymbol = ""
        decimalSeparator = ','
        groupingSeparator = '.'
    }
    return DecimalFormat("#,##0.00", symbols).format(doubleValue)
}

fun String.parseToDouble(): Double {
    return this.replace(Regex("[^0-9,]"), "")
        .replace(",", ".")
        .toDoubleOrNull() ?: 0.0
}

fun formatAmountForUi(amount: Double): String {
    val formatter = DecimalFormat("#,##0.00", DecimalFormatSymbols(Locale("pt", "BR")))
    return formatter.format(amount)
}