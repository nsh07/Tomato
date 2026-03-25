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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.Font
import tomato.shared.generated.resources.Res
import tomato.shared.generated.resources.google_sans_flex

val TYPOGRAPHY = Typography()

data class AppFonts(
    val topBarTitle: FontFamily,
    val annotatedString: FontFamily
)

@Composable
fun typography(): Typography {
    val googleFlex400 = FontFamily(
        Font(
            Res.font.google_sans_flex,
            FontWeight.Normal,
            variationSettings = FontVariation.Settings(FontVariation.weight(400))
        )
    )

    val googleFlex600 = FontFamily(
        Font(
            Res.font.google_sans_flex,
            FontWeight.Bold,
            variationSettings = FontVariation.Settings(
                FontVariation.weight(600),
                FontVariation.Setting("ROND", 100f)
            )
        )
    )

    return remember {
        Typography(
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
    }
}

@Composable
fun getAppFonts(): AppFonts {
    val robotoFlexTopBar = FontFamily(
        Font(
            Res.font.google_sans_flex,
            variationSettings = FontVariation.Settings(
                FontVariation.weight(900),
                FontVariation.width(112.5f),
                FontVariation.Setting("ROND", 35f)
            )
        )
    )

    val annotatedStringFontFamily = FontFamily(
        Font(
            Res.font.google_sans_flex,
            FontWeight.Normal,
            variationSettings = FontVariation.Settings(FontVariation.weight(400))
        ),
        Font(
            Res.font.google_sans_flex,
            FontWeight.Bold,
            variationSettings = FontVariation.Settings(
                FontVariation.weight(600),
                FontVariation.Setting("ROND", 100f)
            )
        ) // Used for <b> tags
    )

    return AppFonts(
        topBarTitle = robotoFlexTopBar,
        annotatedString = annotatedStringFontFamily
    )
}

val LocalAppFonts = staticCompositionLocalOf<AppFonts> { error("AppFonts not provided") }
