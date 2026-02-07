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

import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.nsh07.pomodoro.R

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TopButton(modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    ClickableListItem(
        leadingContent = {
            Icon(
                painterResource(R.drawable.bmc),
                tint = colorScheme.primary,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        },
        headlineContent = { Text(stringResource(R.string.bmc)) },
        supportingContent = { Text(stringResource(R.string.bmc_desc)) },
        trailingContent = { Icon(painterResource(R.drawable.open_in_browser), null) },
        items = 2,
        index = 0,
        modifier = modifier
    ) { uriHandler.openUri("https://coff.ee/nsh07") }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BottomButton(modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    ClickableListItem(
        leadingContent = {
            Icon(
                painterResource(R.drawable.weblate),
                tint = colorScheme.secondary,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        },
        headlineContent = { Text(stringResource(R.string.help_with_translation)) },
        supportingContent = { Text(stringResource(R.string.help_with_translation_desc)) },
        trailingContent = { Icon(painterResource(R.drawable.open_in_browser), null) },
        items = 2,
        index = 1,
        modifier = modifier
    ) { uriHandler.openUri("https://hosted.weblate.org/engage/tomato/") }
}