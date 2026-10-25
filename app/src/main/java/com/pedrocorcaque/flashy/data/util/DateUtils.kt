package com.pedrocorcaque.flashy.data.util

import java.util.Calendar
import java.util.TimeZone

private const val DAY_MS = 86_400_000L

/** Dia atual como "dias desde 1970", no fuso do aparelho. */
fun todayEpochDay(nowMs: Long = System.currentTimeMillis()): Long {
    val offset = TimeZone.getDefault().getOffset(nowMs)
    return Math.floorDiv(nowMs + offset, DAY_MS)
}

/** 20_000 -> "05/10". */
fun formatEpochDay(day: Long): String {
    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    cal.timeInMillis = day * DAY_MS
    val d = cal.get(Calendar.DAY_OF_MONTH)
    val m = cal.get(Calendar.MONTH) + 1
    return String.format("%02d/%02d", d, m)
}
