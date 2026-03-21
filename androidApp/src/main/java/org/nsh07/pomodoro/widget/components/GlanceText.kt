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
import android.graphics.Typeface
import android.text.TextPaint
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.res.ResourcesCompat.getFont
import androidx.core.graphics.createBitmap
import androidx.core.util.TypedValueCompat.spToPx
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.text.FontWeight
import androidx.glance.unit.ColorProvider
import org.nsh07.pomodoro.R


fun createCustomFontBitmap(
    context: Context,
    text: String,
    fontSizeSp: Float,
    fontColor: ColorProvider,
    fontWeight: FontWeight = FontWeight.Normal,
    isClock: Boolean = true
): Bitmap {
    val customTypeface = Typeface.create(
        getFont(context, R.font.google_sans_flex),
        when (fontWeight) {
            FontWeight.Bold -> Typeface.BOLD
            FontWeight.Medium -> Typeface.ITALIC
            else -> Typeface.NORMAL
        }
    )
    val displayMetrics = context.resources.displayMetrics

    val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = customTypeface
        textSize = spToPx(fontSizeSp, displayMetrics)
        color = fontColor.getColor(context).toArgb()
        if (isClock) {
            fontVariationSettings = "'ROND' 100"
            if (fontWeight == FontWeight.Normal) {
                fontFeatureSettings = "tnum"
                letterSpacing = -0.04f
            }
        }
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

@Composable
fun GlanceText(
    context: Context,
    text: String,
    fontSizeSp: Float,
    fontColor: ColorProvider,
    modifier: GlanceModifier = GlanceModifier,
    fontWeight: FontWeight = FontWeight.Normal,
    isClock: Boolean = true
) {
    Image(
        provider = ImageProvider(
            createCustomFontBitmap(
                context,
                text,
                fontSizeSp,
                fontColor,
                fontWeight,
                isClock
            )
        ),
        contentDescription = text,
        modifier = modifier
    )
}
