/*
 * Copyright (c) 2026 Nishant Mishra
 *
 * This file is part of Tomato - a minimalist pomodoro timer for Android.
 *
 * Tomato is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Tomato is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Tomato.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package org.nsh07.pomodoro.widget.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.text.TextPaint
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.res.ResourcesCompat.getFont
import androidx.core.graphics.createBitmap
import androidx.core.util.TypedValueCompat.spToPx
import androidx.glance.unit.ColorProvider
import org.nsh07.pomodoro.R


fun createCustomFontBitmap(
    context: Context,
    text: String,
    fontSizeSp: Float,
    fontColor: ColorProvider
): Bitmap {
    val customTypeface = getFont(context, R.font.google_sans_flex)
    val displayMetrics = context.resources.displayMetrics

    val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = customTypeface
        textSize = spToPx(fontSizeSp, displayMetrics)
        color = fontColor.getColor(context).toArgb()
        fontFeatureSettings = "tnum"
        fontVariationSettings = "'ROND' 100"
        letterSpacing = -0.04f
    }

    val width = textPaint.measureText(text).toInt()
    val fontMetrics = textPaint.fontMetrics
    val height = (fontMetrics.descent - fontMetrics.ascent).toInt()

    val safeWidth = if (width > 0) width else 1
    val safeHeight = if (height > 0) height else 1
    val bitmap = createBitmap(safeWidth, safeHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    canvas.drawText(text, 0f, -fontMetrics.ascent, textPaint)

    return bitmap
}
