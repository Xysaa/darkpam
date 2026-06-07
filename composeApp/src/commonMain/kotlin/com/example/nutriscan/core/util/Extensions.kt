package com.example.nutriscan.core.util

import kotlinx.coroutines.delay
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

// ==================== DATE/TIME EXTENSIONS ====================

fun Instant.formatToDisplay(): String {
    val dateTime = this.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${dateTime.dayOfMonth}/${dateTime.monthNumber}/${dateTime.year} " +
            "${dateTime.hour.toString().padStart(2, '0')}:${dateTime.minute.toString().padStart(2, '0')}"
}

fun Instant.formatDateOnly(): String {
    val dateTime = this.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${dateTime.dayOfMonth}/${dateTime.monthNumber}/${dateTime.year}"
}

fun Instant.formatTimeOnly(): String {
    val dateTime = this.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${dateTime.hour.toString().padStart(2, '0')}:${dateTime.minute.toString().padStart(2, '0')}"
}

// ==================== NUMBER FORMATTING ====================

/**
 * Format a number with at most one decimal place, dropping a trailing ".0".
 * e.g. 12.0 -> "12", 12.34 -> "12.3", 0.0 -> "0".
 */
fun Float.format1(): String {
    val rounded = kotlin.math.round(this * 10f) / 10f
    if (rounded % 1f == 0f) return rounded.toInt().toString()
    val tenths = kotlin.math.round(kotlin.math.abs(rounded) * 10f).toInt()
    val whole = tenths / 10
    val frac = tenths % 10
    val sign = if (rounded < 0f) "-" else ""
    return "$sign$whole.$frac"
}

fun Double.format1(): String = this.toFloat().format1()

// ==================== STRING EXTENSIONS ====================

fun String.truncate(maxLength: Int): String {
    return if (this.length > maxLength) {
        this.take(maxLength - 3) + "..."
    } else {
        this
    }
}

fun String.capitalizeFirst(): String {
    return this.replaceFirstChar { it.uppercase() }
}

// ==================== RETRY HELPER ====================

suspend fun <T> retryWithBackoff(
    times: Int = 3,
    initialDelay: Long = 100,
    maxDelay: Long = 1000,
    factor: Double = 2.0,
    block: suspend () -> T
): T {
    var currentDelay = initialDelay
    repeat(times - 1) {
        try {
            return block()
        } catch (e: Exception) {
            println("Retry attempt failed: ${e.message}")
        }
        delay(currentDelay)
        currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
    }
    return block()
}

// ==================== RESULT EXTENSIONS ====================

inline fun <T, R> Result<T>.mapSuccess(transform: (T) -> R): Result<R> {
    return this.map(transform)
}

inline fun <T> Result<T>.handle(
    onSuccess: (T) -> Unit,
    onFailure: (Throwable) -> Unit
) {
    this.onSuccess(onSuccess)
    this.onFailure(onFailure)
}
