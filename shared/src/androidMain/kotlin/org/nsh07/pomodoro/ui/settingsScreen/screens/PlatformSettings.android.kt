/*
 * Copyright (c) 2026 Nishant Mishra
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

package org.nsh07.pomodoro.ui.settingsScreen.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.nsh07.pomodoro.ui.settingsScreen.components.LocaleBottomSheet
import org.nsh07.pomodoro.ui.theme.CustomColors.listItemColors
import org.nsh07.pomodoro.ui.theme.TomatoShapeDefaults.segmentedListItemShapes
import org.nsh07.pomodoro.utils.androidDeviceManufacturerIs
import org.nsh07.pomodoro.utils.androidSdkVersionAtLeast
import tomato.shared.generated.resources.Res
import tomato.shared.generated.resources.language
import tomato.shared.generated.resources.mobile_text
import tomato.shared.generated.resources.now_bar
import tomato.shared.generated.resources.open_in_browser

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
actual fun PlatformSettings() {
    val currentLocale =
        if (androidSdkVersionAtLeast(33)) {
            LocalLocale.current
        } else null

    var showLocaleSheet by remember { mutableStateOf(false) }

    if (showLocaleSheet)
        LocaleBottomSheet({ showLocaleSheet = it })

    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        if (currentLocale != null)
            SegmentedListItem(
                leadingContent = {
                    Icon(painterResource(Res.drawable.language), contentDescription = null)
                },
                supportingContent = {
                    Text(currentLocale.platformLocale.displayLanguage)
                },
                selected = showLocaleSheet,
                shapes = segmentedListItemShapes(0, 1),
                colors = listItemColors,
                onClick = { showLocaleSheet = true }
            ) { Text(stringResource(Res.string.language)) }

        if (androidSdkVersionAtLeast(36) && androidDeviceManufacturerIs("samsung")) {
            val uriHandler = LocalUriHandler.current
            Spacer(Modifier.height(14.dp))
            SegmentedListItem(
                leadingContent = {
                    Icon(painterResource(Res.drawable.mobile_text), null)
                },
                trailingContent = {
                    Icon(painterResource(Res.drawable.open_in_browser), null)
                },
                shapes = segmentedListItemShapes(0, 1),
                colors = listItemColors,
                onClick = { uriHandler.openUri("https://gist.github.com/nsh07/3b42969aef017d98f72b097f1eca8911") }
            ) { Text(stringResource(Res.string.now_bar)) }
        }
    }
}