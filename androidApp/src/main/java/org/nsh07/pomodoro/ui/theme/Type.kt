/*
 * Copyright (c) 2025-2026 Nishant Mishra
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
import androidx.compose.ui.text.font.FontWeight
import org.nsh07.pomodoro.R
import org.nsh07.pomodoro.ui.theme.AppFonts.googleFlex400
import org.nsh07.pomodoro.ui.theme.AppFonts.googleFlex600

val TYPOGRAPHY = Typography()

val Typography = Typography(
    displayLarge = TYPOGRAPHY.displayLarge.copy(
        fontFamily = googleFlex600,
        fontFeatureSettings = "ss02, dlig"
    ),
    displayMedium = TYPOGRAPHY.displayMedium.copy(
        fontFamily = googleFlex600,
        fontFeatureSettings = "ss02, dlig"
    ),
    displaySmall = TYPOGRAPHY.displaySmall.copy(
        fontFamily = googleFlex600,
        fontFeatureSettings = "ss02, dlig"
    ),
    headlineLarge = TYPOGRAPHY.headlineLarge.copy(
        fontFamily = googleFlex600,
        fontFeatureSettings = "ss02, dlig"
    ),
    headlineMedium = TYPOGRAPHY.headlineMedium.copy(
        fontFamily = googleFlex600,
        fontFeatureSettings = "ss02, dlig"
    ),
    headlineSmall = TYPOGRAPHY.headlineSmall.copy(
        fontFamily = googleFlex600,
        fontFeatureSettings = "ss02, dlig"
    ),
    titleLarge = TYPOGRAPHY.titleLarge.copy(
        fontFamily = googleFlex400,
        fontFeatureSettings = "ss02, dlig"
    ),
    titleMedium = TYPOGRAPHY.titleMedium.copy(
        fontFamily = googleFlex600,
        fontFeatureSettings = "ss02, dlig"
    ),
    titleSmall = TYPOGRAPHY.titleSmall.copy(
        fontFamily = googleFlex600,
        fontFeatureSettings = "ss02, dlig"
    ),
    bodyLarge = TYPOGRAPHY.bodyLarge.copy(
        fontFamily = googleFlex600,
        fontFeatureSettings = "ss02, dlig"
    ),
    bodyMedium = TYPOGRAPHY.bodyMedium.copy(
        fontFamily = googleFlex400,
        fontFeatureSettings = "ss02, dlig"
    ),
    bodySmall = TYPOGRAPHY.bodySmall.copy(
        fontFamily = googleFlex400,
        fontFeatureSettings = "ss02, dlig"
    ),
    labelLarge = TYPOGRAPHY.labelLarge.copy(
        fontFamily = googleFlex600,
        fontFeatureSettings = "ss02, dlig"
    ),
    labelMedium = TYPOGRAPHY.labelMedium.copy(
        fontFamily = googleFlex600,
        fontFeatureSettings = "ss02, dlig"
    ),
    labelSmall = TYPOGRAPHY.labelSmall.copy(
        fontFamily = googleFlex600,
        fontFeatureSettings = "ss02, dlig"
    )
)

@OptIn(ExperimentalTextApi::class)
object AppFonts {
    val googleFlex400 = FontFamily(Font(R.font.google_sans_flex_400))

    val googleFlex600 = FontFamily(Font(R.font.google_sans_flex_600))

    val robotoFlexTopBar = FontFamily(Font(R.font.roboto_flex_logo))

    val annotatedStringFontFamily = FontFamily(
        Font(resId = R.font.google_sans_flex_400, weight = FontWeight.Normal),
        Font(resId = R.font.google_sans_flex_600, weight = FontWeight.Bold) // Used for <b> tags
    )
}