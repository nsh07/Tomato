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
import org.nsh07.pomodoro.ui.theme.AppFonts.interBody
import org.nsh07.pomodoro.ui.theme.AppFonts.interLabel
import org.nsh07.pomodoro.ui.theme.AppFonts.robotoFlexHeadline
import org.nsh07.pomodoro.ui.theme.AppFonts.robotoFlexTitle

val TYPOGRAPHY = Typography()

// Set of Material typography styles to start with
val Typography = Typography(
    displayLarge = TYPOGRAPHY.displayLarge.copy(fontFamily = robotoFlexHeadline),
    displayMedium = TYPOGRAPHY.displayMedium.copy(fontFamily = robotoFlexHeadline),
    displaySmall = TYPOGRAPHY.displaySmall.copy(fontFamily = robotoFlexHeadline),
    headlineLarge = TYPOGRAPHY.headlineLarge.copy(fontFamily = robotoFlexHeadline),
    headlineMedium = TYPOGRAPHY.headlineMedium.copy(fontFamily = robotoFlexHeadline),
    headlineSmall = TYPOGRAPHY.headlineSmall.copy(fontFamily = robotoFlexHeadline),
    titleLarge = TYPOGRAPHY.titleLarge.copy(fontFamily = robotoFlexTitle),
    titleMedium = TYPOGRAPHY.titleMedium.copy(fontFamily = robotoFlexTitle),
    titleSmall = TYPOGRAPHY.titleSmall.copy(fontFamily = robotoFlexTitle),
    bodyLarge = TYPOGRAPHY.bodyLarge.copy(fontFamily = interBody),
    bodyMedium = TYPOGRAPHY.bodyMedium.copy(fontFamily = interBody),
    bodySmall = TYPOGRAPHY.bodySmall.copy(fontFamily = interBody),
    labelLarge = TYPOGRAPHY.labelLarge.copy(fontFamily = interLabel),
    labelMedium = TYPOGRAPHY.labelMedium.copy(fontFamily = interLabel),
    labelSmall = TYPOGRAPHY.labelSmall.copy(fontFamily = interLabel)
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

    val interBody = FontFamily(
        Font(
            R.font.inter_variable, variationSettings = FontVariation.Settings(
                FontWeight.Normal,
                FontStyle.Normal
            )
        )
    )

    val interLabel = FontFamily(
        Font(
            R.font.inter_variable, variationSettings = FontVariation.Settings(
                FontWeight.Medium,
                FontStyle.Normal
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

    val robotoFlexHeadline = FontFamily(
        Font(
            R.font.roboto_flex_variable,
            variationSettings = FontVariation.Settings(
                FontVariation.width(130f),
                FontVariation.weight(600),
                FontVariation.grade(0)
            )
        )
    )

    val robotoFlexTitle = FontFamily(
        Font(
            R.font.roboto_flex_variable,
            variationSettings = FontVariation.Settings(
                FontVariation.width(130f),
                FontVariation.weight(700),
                FontVariation.grade(0)
            )
        )
    )
}