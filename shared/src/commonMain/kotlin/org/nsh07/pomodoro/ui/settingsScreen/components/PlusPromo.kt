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

package org.nsh07.pomodoro.ui.settingsScreen.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.nsh07.pomodoro.ui.theme.CustomColors.listItemColors
import org.nsh07.pomodoro.ui.theme.TomatoShapeDefaults.segmentedListItemShapes
import tomato.shared.generated.resources.Res
import tomato.shared.generated.resources.app_name_plus
import tomato.shared.generated.resources.arrow_forward_big
import tomato.shared.generated.resources.get_plus
import tomato.shared.generated.resources.tomato_logo_notification

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PlusPromo(
    isPlus: Boolean,
    setShowPaywall: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    SegmentedListItem(
        onClick = { setShowPaywall(true) },
        leadingContent = {
            Icon(
                painterResource(Res.drawable.tomato_logo_notification),
                null,
                modifier = Modifier.size(24.dp)
            )
        },
        content = {
            Text(
                if (!isPlus) stringResource(Res.string.get_plus)
                else stringResource(Res.string.app_name_plus)
            )
        },
        trailingContent = {
            Icon(
                painterResource(Res.drawable.arrow_forward_big),
                null
            )
        },
        selected = !isPlus,
        shapes = segmentedListItemShapes(0, 1),
        colors = listItemColors,
        modifier = modifier
    )
}