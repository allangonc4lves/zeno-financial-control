package br.dev.allan.controlefinanceiro.utils

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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