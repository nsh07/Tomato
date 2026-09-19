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

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.nsh07.pomodoro.ui.theme.CustomColors.listItemColors
import org.nsh07.pomodoro.ui.theme.CustomColors.switchColors
import org.nsh07.pomodoro.ui.theme.TomatoShapeDefaults.segmentedListItemShapes
import org.nsh07.pomodoro.utils.OS
import org.nsh07.pomodoro.utils.currentOS
import tomato.shared.generated.resources.Res
import tomato.shared.generated.resources.advanced
import tomato.shared.generated.resources.arrow_down
import tomato.shared.generated.resources.check

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
actual fun PlatformSettings() {
    val viewModel: PlatformSettingsViewModel = koinViewModel()
    val settingsState by viewModel.settingsState.collectAsState()

    var expanded by remember { mutableStateOf(false) }

    AnimatedContent(expanded) { isExpanded ->
        if (!isExpanded)
            SegmentedListItem(
                onClick = { expanded = true },
                shapes = segmentedListItemShapes(0, 1),
                colors = listItemColors,
                leadingContent = { Icon(painterResource(Res.drawable.arrow_down), null) }
            ) {
                Text(stringResource(Res.string.advanced))
            }
        else SegmentedListItem(
            onClick = { viewModel.saveCustomWindowDecor(!settingsState.customWindowDecor) },
            shapes = segmentedListItemShapes(0, 1),
            colors = listItemColors,
            supportingContent = {
                Text(
                    "Disable if you encounter window-related bugs." + " " +
                            if (currentOS == OS.WINDOWS) "This is enabled automatically if Always On Display is enabled."
                            else ""
                )
            },
            trailingContent = {
                Switch(
                    checked = settingsState.customWindowDecor,
                    onCheckedChange = { viewModel.saveCustomWindowDecor(it) },
                    thumbContent = {
                        if (settingsState.customWindowDecor)
                            Icon(
                                painterResource(Res.drawable.check),
                                null,
                                modifier = Modifier.size(SwitchDefaults.IconSize)
                            )
                    },
                    colors = switchColors
                )
            }
        ) {
            Text("Custom window decorations")
        }
    }
}