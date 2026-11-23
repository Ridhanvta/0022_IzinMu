package com.example.izinmu.util

import androidx.annotation.StringRes
import com.example.izinmu.R

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val jakarta: TimeZone = TimeZone.getTimeZone("Asia/Jakarta")

fun formatDateKey(key: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("id-ID"))
        formatter.format(parser.parse(key) ?: Date())
    } catch (_: Exception) {
        key
    }
}

fun formatTimestamp(iso: String?): String {
    if (iso.isNullOrBlank()) return "-"
    val pola = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        "yyyy-MM-dd'T'HH:mm:ssXXX",
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd HH:mm:ss",
    )
    for (format in pola) {
        val hasil = runCatching {
            val parser = SimpleDateFormat(format, Locale.US)
            if (format.endsWith("'Z'")) parser.timeZone = TimeZone.getTimeZone("UTC")
            val tanggal = parser.parse(iso) ?: return@runCatching null
            val formatter = SimpleDateFormat("dd MMM yyyy, HH.mm", Locale.forLanguageTag("id-ID"))
            formatter.timeZone = jakarta
            formatter.format(tanggal)
        }.getOrNull()
        if (hasil != null) return hasil
    }
    return iso
}

fun todayDayName(): String {
    val formatter = SimpleDateFormat("EEEE", Locale.forLanguageTag("id-ID"))
    formatter.timeZone = jakarta
    return formatter.format(Date())
}

fun todayDateKey(): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    formatter.timeZone = jakarta
    return formatter.format(Date())
}

fun tomorrowDateKey(): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    formatter.timeZone = jakarta
    return formatter.format(Date(System.currentTimeMillis() + 86_400_000L))
}

fun besokDari(tanggal: String): String {
    if (tanggal.isBlank()) return tomorrowDateKey()
    return runCatching {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        formatter.timeZone = jakarta
        val date = formatter.parse(tanggal) ?: return@runCatching tomorrowDateKey()
        formatter.format(Date(date.time + 86_400_000L))
    }.getOrDefault(tomorrowDateKey())
}

@StringRes
fun sapaanRes(): Int {
    val hour = Calendar.getInstance(jakarta).get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 4..10 -> R.string.home_greeting_morning
        in 11..14 -> R.string.home_greeting_noon
        in 15..17 -> R.string.home_greeting_evening
        else -> R.string.home_greeting_night
    }
}
