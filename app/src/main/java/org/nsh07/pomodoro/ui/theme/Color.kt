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

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val primaryLight = Color(0xFF4C662B)
val onPrimaryLight = Color(0xFFFFFFFF)
val primaryContainerLight = Color(0xFFCDEDA3)
val onPrimaryContainerLight = Color(0xFF354E16)
val secondaryLight = Color(0xFF586249)
val onSecondaryLight = Color(0xFFFFFFFF)
val secondaryContainerLight = Color(0xFFDCE7C8)
val onSecondaryContainerLight = Color(0xFF404A33)
val tertiaryLight = Color(0xFF386663)
val onTertiaryLight = Color(0xFFFFFFFF)
val tertiaryContainerLight = Color(0xFFBCECE7)
val onTertiaryContainerLight = Color(0xFF1F4E4B)
val errorLight = Color(0xFFBA1A1A)
val onErrorLight = Color(0xFFFFFFFF)
val errorContainerLight = Color(0xFFFFDAD6)
val onErrorContainerLight = Color(0xFF93000A)
val backgroundLight = Color(0xFFF9FAEF)
val onBackgroundLight = Color(0xFF1A1C16)
val surfaceLight = Color(0xFFF9FAEF)
val onSurfaceLight = Color(0xFF1A1C16)
val surfaceVariantLight = Color(0xFFE1E4D5)
val onSurfaceVariantLight = Color(0xFF44483D)
val outlineLight = Color(0xFF75796C)
val outlineVariantLight = Color(0xFFC5C8BA)
val scrimLight = Color(0xFF000000)
val inverseSurfaceLight = Color(0xFF2F312A)
val inverseOnSurfaceLight = Color(0xFFF1F2E6)
val inversePrimaryLight = Color(0xFFB1D18A)
val surfaceDimLight = Color(0xFFDADBD0)
val surfaceBrightLight = Color(0xFFF9FAEF)
val surfaceContainerLowestLight = Color(0xFFFFFFFF)
val surfaceContainerLowLight = Color(0xFFF3F4E9)
val surfaceContainerLight = Color(0xFFEEEFE3)
val surfaceContainerHighLight = Color(0xFFE8E9DE)
val surfaceContainerHighestLight = Color(0xFFE2E3D8)

val primaryDark = Color(0xFFB1D18A)
val onPrimaryDark = Color(0xFF1F3701)
val primaryContainerDark = Color(0xFF354E16)
val onPrimaryContainerDark = Color(0xFFCDEDA3)
val secondaryDark = Color(0xFFBFCBAD)
val onSecondaryDark = Color(0xFF2A331E)
val secondaryContainerDark = Color(0xFF404A33)
val onSecondaryContainerDark = Color(0xFFDCE7C8)
val tertiaryDark = Color(0xFFA0D0CB)
val onTertiaryDark = Color(0xFF003735)
val tertiaryContainerDark = Color(0xFF1F4E4B)
val onTertiaryContainerDark = Color(0xFFBCECE7)
val errorDark = Color(0xFFFFB4AB)
val onErrorDark = Color(0xFF690005)
val errorContainerDark = Color(0xFF93000A)
val onErrorContainerDark = Color(0xFFFFDAD6)
val backgroundDark = Color(0xFF12140E)
val onBackgroundDark = Color(0xFFE2E3D8)
val surfaceDark = Color(0xFF12140E)
val onSurfaceDark = Color(0xFFE2E3D8)
val surfaceVariantDark = Color(0xFF44483D)
val onSurfaceVariantDark = Color(0xFFC5C8BA)
val outlineDark = Color(0xFF8F9285)
val outlineVariantDark = Color(0xFF44483D)
val scrimDark = Color(0xFF000000)
val inverseSurfaceDark = Color(0xFFE2E3D8)
val inverseOnSurfaceDark = Color(0xFF2F312A)
val inversePrimaryDark = Color(0xFF4C662B)
val surfaceDimDark = Color(0xFF12140E)
val surfaceBrightDark = Color(0xFF383A32)
val surfaceContainerLowestDark = Color(0xFF0C0F09)
val surfaceContainerLowDark = Color(0xFF1A1C16)
val surfaceContainerDark = Color(0xFF1E201A)
val surfaceContainerHighDark = Color(0xFF282B24)
val surfaceContainerHighestDark = Color(0xFF33362E)

object CustomColors {
    var black = false

    @OptIn(ExperimentalMaterial3Api::class)
    val topBarColors: TopAppBarColors
        @Composable get() =
            TopAppBarDefaults.topAppBarColors(
                containerColor = if (!black) colorScheme.surfaceContainer else colorScheme.surface,
                scrolledContainerColor = if (!black) colorScheme.surfaceContainer else colorScheme.surface
            )

    val listItemColors: ListItemColors
        @Composable get() =
            ListItemDefaults.colors(containerColor = if (!black) colorScheme.surfaceBright else colorScheme.surfaceContainerHigh)

    val selectedListItemColors: ListItemColors
        @Composable get() =
            ListItemDefaults.colors(
                containerColor = colorScheme.secondaryContainer,
                headlineColor = colorScheme.secondary,
                leadingIconColor = colorScheme.onSecondaryContainer,
                supportingColor = colorScheme.onSecondaryFixedVariant,
                trailingIconColor = colorScheme.onSecondaryFixedVariant
            )

    val switchColors: SwitchColors
        @Composable get() = SwitchDefaults.colors(
            checkedIconColor = colorScheme.primary,
        )
}