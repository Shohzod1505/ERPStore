package ru.itis.kpfu.ermstore.ui.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val ruLocale = Locale.forLanguageTag("ru-RU")

fun formatMoney(kopecks: Long): String {
	val sign = if (kopecks < 0) "-" else ""
	val absolute = kotlin.math.abs(kopecks)
	val rubles = absolute / 100
	val cents = absolute % 100
	val formattedRubles = NumberFormat.getIntegerInstance(ruLocale).format(rubles)
	return "$sign$formattedRubles,${cents.toString().padStart(2, '0')} ₽"
}

fun formatDate(millis: Long): String =
	SimpleDateFormat("dd.MM.yyyy HH:mm", ruLocale).format(Date(millis))

fun formatQuantity(units: Long, unitsPerBlock: Int): String {
	val blocks = units / unitsPerBlock
	val remainder = units % unitsPerBlock
	return "$units шт. · $blocks бл. + $remainder шт."
}

fun formatSignedUnits(units: Long): String = when {
	units > 0 -> "+$units шт."
	else -> "$units шт."
}

fun parseMoneyToKopecks(input: String): Long? {
	val normalized = input.trim().replace(',', '.')
	if (normalized.isEmpty()) return null
	return runCatching {
		BigDecimal(normalized)
			.movePointRight(2)
			.setScale(0, RoundingMode.HALF_UP)
			.toLong()
	}.getOrNull()
}

fun parseUnits(input: String): Long? {
	val value = input.trim().toLongOrNull() ?: return null
	return value.takeIf { it >= 0 }
}
