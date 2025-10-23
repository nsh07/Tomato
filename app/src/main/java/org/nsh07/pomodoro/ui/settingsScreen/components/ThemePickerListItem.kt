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

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import org.nsh07.pomodoro.R

@Composable
fun ThemePickerListItem(
    theme: String,
    themeMap: Map<String, Pair<Int, Int>>,
    reverseThemeMap: Map<String, String>,
    items: Int,
    index: Int,
    onThemeChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by rememberSaveable { mutableStateOf(false) }

    if (showDialog) {
        ThemeDialog(
            themeMap = themeMap,
            reverseThemeMap = reverseThemeMap,
            theme = theme,
            setShowThemeDialog = { showDialog = it },
            onThemeChange = onThemeChange
        )
    }

    ClickableListItem(
        leadingContent = {
            Icon(
                painter = painterResource(themeMap[theme]!!.first),
                contentDescription = null
            )
        },
        headlineContent = { Text(stringResource(R.string.theme)) },
        supportingContent = {
            Text(stringResource(themeMap[theme]!!.second))
        },
        items = items,
        index = index,
        modifier = modifier.fillMaxWidth()
    ) { showDialog = true }
}