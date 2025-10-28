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

package org.nsh07.pomodoro.ui.settingsScreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.nsh07.pomodoro.R
import org.nsh07.pomodoro.ui.theme.AppFonts.robotoFlexTopBar

@Composable
fun PlusPromo(
    isPlus: Boolean,
    setShowPaywall: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val container = if (isPlus) colorScheme.surfaceBright else colorScheme.primary
    val onContainer = if (isPlus) colorScheme.onSurface else colorScheme.onPrimary
    val onContainerVariant = if (isPlus) colorScheme.onSurfaceVariant else colorScheme.onPrimary

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(CircleShape)
            .background(container)
            .padding(16.dp)
            .clickable { setShowPaywall(true) }
    ) {
        Icon(
            painterResource(R.drawable.tomato_logo_notification),
            null,
            tint = onContainerVariant,
            modifier = Modifier
                .size(24.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            if (!isPlus) stringResource(R.string.get_plus)
            else stringResource(R.string.app_name_plus),
            style = typography.titleLarge,
            fontFamily = robotoFlexTopBar,
            color = onContainer
        )
        Spacer(Modifier.weight(1f))
        Icon(
            painterResource(R.drawable.arrow_forward_big),
            null,
            tint = onContainerVariant
        )
    }
}