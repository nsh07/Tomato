/*
 * Copyright (c) 2025 Nishant Mishra
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

package org.nsh07.pomodoro.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import org.nsh07.pomodoro.R
import org.nsh07.pomodoro.ui.theme.AppFonts.googleFlex400
import org.nsh07.pomodoro.ui.theme.AppFonts.googleFlex600

val TYPOGRAPHY = Typography()

val Typography = Typography(
    displayLarge = TYPOGRAPHY.displayLarge.copy(fontFamily = googleFlex600),
    displayMedium = TYPOGRAPHY.displayMedium.copy(fontFamily = googleFlex600),
    displaySmall = TYPOGRAPHY.displaySmall.copy(fontFamily = googleFlex600),
    headlineLarge = TYPOGRAPHY.headlineLarge.copy(fontFamily = googleFlex600),
    headlineMedium = TYPOGRAPHY.headlineMedium.copy(fontFamily = googleFlex600),
    headlineSmall = TYPOGRAPHY.headlineSmall.copy(fontFamily = googleFlex600),
    titleLarge = TYPOGRAPHY.titleLarge.copy(fontFamily = googleFlex400),
    titleMedium = TYPOGRAPHY.titleMedium.copy(fontFamily = googleFlex600),
    titleSmall = TYPOGRAPHY.titleSmall.copy(fontFamily = googleFlex600),
    bodyLarge = TYPOGRAPHY.bodyLarge.copy(fontFamily = googleFlex600),
    bodyMedium = TYPOGRAPHY.bodyMedium.copy(fontFamily = googleFlex400),
    bodySmall = TYPOGRAPHY.bodySmall.copy(fontFamily = googleFlex400),
    labelLarge = TYPOGRAPHY.labelLarge.copy(fontFamily = googleFlex600),
    labelMedium = TYPOGRAPHY.labelMedium.copy(fontFamily = googleFlex600),
    labelSmall = TYPOGRAPHY.labelSmall.copy(fontFamily = googleFlex600)
)

@OptIn(ExperimentalTextApi::class)
object AppFonts {
    val interClock = FontFamily(
        Font(
            R.font.inter_variable, variationSettings = FontVariation.Settings(
                FontWeight.Bold,
                FontStyle.Normal
            )
        )
    )

    val googleFlex400 = FontFamily(
        Font(
            R.font.google_sans_flex_variable, variationSettings = FontVariation.Settings(
                FontVariation.weight(400)
            )
        )
    )

    val googleFlex600 = FontFamily(
        Font(
            R.font.google_sans_flex_variable, variationSettings = FontVariation.Settings(
                FontVariation.weight(600),
                FontVariation.Setting("ROND", 100f)
            )
        )
    )

    val robotoFlexTopBar = FontFamily(
        Font(
            R.font.roboto_flex_variable,
            variationSettings = FontVariation.Settings(
                FontVariation.width(125f),
                FontVariation.weight(1000),
                FontVariation.grade(0),
                FontVariation.Setting("XOPQ", 96F),
                FontVariation.Setting("XTRA", 500F),
                FontVariation.Setting("YOPQ", 79F),
                FontVariation.Setting("YTAS", 750F),
                FontVariation.Setting("YTDE", -203F),
                FontVariation.Setting("YTFI", 738F),
                FontVariation.Setting("YTLC", 514F),
                FontVariation.Setting("YTUC", 712F)
            )
        )
    )
}