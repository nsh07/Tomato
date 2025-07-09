package org.nsh07.pomodoro.utils

import java.util.Locale
import kotlin.math.ceil

fun millisecondsToStr(t: Int): String {
    val min = (ceil(t / 1000.0).toInt() / 60)
    val sec = (ceil(t / 1000.0).toInt() % 60)
    return String.format(locale = Locale.getDefault(), "%02d:%02d", min, sec)
}