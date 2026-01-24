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
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.nsh07.pomodoro.R
import org.nsh07.pomodoro.ui.theme.CustomColors.listItemColors

@Composable
fun PlusPromo(
    isPlus: Boolean,
    setShowPaywall: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    ClickableListItem(
        leadingContent = {
            Icon(
                painterResource(R.drawable.tomato_logo_notification),
                null,
                modifier = Modifier.size(24.dp)
            )
        },
        headlineContent = {
            Text(
                if (!isPlus) stringResource(R.string.get_plus)
                else stringResource(R.string.app_name_plus)
            )
        },
        trailingContent = {
            Icon(
                painterResource(R.drawable.arrow_forward_big),
                null
            )
        },
        colors = if (isPlus) listItemColors else ListItemDefaults.colors(
            containerColor = colorScheme.primary,
            leadingIconColor = colorScheme.onPrimary,
            trailingIconColor = colorScheme.onPrimary,
            supportingColor = colorScheme.onPrimary,
            headlineColor = colorScheme.onPrimary
        ),
        items = 1,
        index = 0,
        modifier = modifier
    ) { setShowPaywall(true) }
}