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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.nsh07.pomodoro.di.AppInfo
import org.nsh07.pomodoro.settingsScreens
import org.nsh07.pomodoro.ui.Screen
import org.nsh07.pomodoro.ui.SettingsNavItem
import org.nsh07.pomodoro.ui.mergePaddingValues
import org.nsh07.pomodoro.ui.settingsScreen.ResetDataDialog
import org.nsh07.pomodoro.ui.settingsScreen.components.LocaleBottomSheet
import org.nsh07.pomodoro.ui.settingsScreen.components.PlusPromo
import org.nsh07.pomodoro.ui.settingsScreen.viewModel.SettingsAction
import org.nsh07.pomodoro.ui.settingsScreen.viewModel.SettingsState
import org.nsh07.pomodoro.ui.theme.CustomColors.listItemColors
import org.nsh07.pomodoro.ui.theme.CustomColors.topBarColors
import org.nsh07.pomodoro.ui.theme.LocalAppFonts
import org.nsh07.pomodoro.ui.theme.TomatoShapeDefaults.singleItemListItemShapes
import org.nsh07.pomodoro.utils.androidDeviceManufacturerIs
import org.nsh07.pomodoro.utils.androidSdkVersionAtLeast
import tomato.shared.generated.resources.Res
import tomato.shared.generated.resources.about
import tomato.shared.generated.resources.app_name
import tomato.shared.generated.resources.arrow_forward_big
import tomato.shared.generated.resources.backup
import tomato.shared.generated.resources.backup_and_restore
import tomato.shared.generated.resources.info
import tomato.shared.generated.resources.language
import tomato.shared.generated.resources.mobile_text
import tomato.shared.generated.resources.now_bar
import tomato.shared.generated.resources.open_in_browser
import tomato.shared.generated.resources.reset_data
import tomato.shared.generated.resources.restore
import tomato.shared.generated.resources.settings
import tomato.shared.generated.resources.system_default

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsMainScreen(
    settingsState: SettingsState,
    contentPadding: PaddingValues,
    currentScreen: Screen.Settings,
    isPlus: Boolean,
    onAction: (SettingsAction) -> Unit,
    onNavigate: (Screen.Settings) -> Unit,
    setShowPaywall: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    appInfo: AppInfo = koinInject()
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    val widthExpanded = currentWindowAdaptiveInfo()
        .windowSizeClass
        .isWidthAtLeastBreakpoint(WIDTH_DP_EXPANDED_LOWER_BOUND)

    val currentLocales =
        if (androidSdkVersionAtLeast(33)) {
            LocaleList.current
        } else null
    val currentLocalesSize = currentLocales?.size ?: 0

    var showLocaleSheet by remember { mutableStateOf(false) }

    if (showLocaleSheet)
        LocaleBottomSheet({ showLocaleSheet = it })

    if (settingsState.isShowingEraseDataDialog) {
        ResetDataDialog(
            resetData = { onAction(SettingsAction.EraseData) },
            onDismiss = { onAction(SettingsAction.CancelEraseData) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(Res.string.settings),
                        style = LocalTextStyle.current.copy(
                            fontFamily = LocalAppFonts.current.topBarTitle,
                            fontSize = 32.sp,
                            lineHeight = 32.sp
                        )
                    )
                },
                subtitle = {},
                colors = topBarColors,
                titleHorizontalAlignment = Alignment.CenterHorizontally,
                scrollBehavior = scrollBehavior
            )
        },
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        val insets = mergePaddingValues(innerPadding, contentPadding)
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            contentPadding = insets,
            modifier = Modifier
                .background(topBarColors.containerColor)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            item { Spacer(Modifier.height(14.dp)) }

            item {
                PlusPromo(isPlus, setShowPaywall)
            }

            item { Spacer(Modifier.height(12.dp)) }

            itemsIndexed(settingsScreens) { index, item ->
                SegmentedListItem(
                    leadingContent = {
                        Icon(painterResource(item.icon), null)
                    },
                    supportingContent = {
                        val innerStrings = item.innerSettings.map { stringResource(it) }
                        val joinedText = remember(innerStrings) { innerStrings.joinToString(", ") }
                        Text(
                            joinedText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    trailingContent = if (!widthExpanded) {
                        { Icon(painterResource(Res.drawable.arrow_forward_big), null) }
                    } else null,
                    shapes = ListItemDefaults.segmentedShapes(index, settingsScreens.size),
                    colors = listItemColors,
                    selected = widthExpanded && currentScreen == item.route,
                    onClick = { onNavigate(item.route) }
                ) { Text(stringResource(item.label)) }
            }

            item { Spacer(Modifier.height(12.dp)) }

            item {
                val item = remember {
                    SettingsNavItem(
                        Screen.Settings.Backup,
                        Res.drawable.backup,
                        Res.string.backup_and_restore,
                        listOf(Res.string.backup, Res.string.restore, Res.string.reset_data)
                    )
                }
                SegmentedListItem(
                    leadingContent = {
                        Icon(painterResource(item.icon), null)
                    },
                    supportingContent = {
                        val innerStrings = item.innerSettings.map { stringResource(it) }
                        val joinedText = remember(innerStrings) { innerStrings.joinToString(", ") }
                        Text(
                            joinedText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    trailingContent = if (!widthExpanded) {
                        { Icon(painterResource(Res.drawable.arrow_forward_big), null) }
                    } else null,
                    selected = currentScreen == Screen.Settings.Backup,
                    shapes = ListItemDefaults.segmentedShapes(0, 2),
                    colors = listItemColors,
                    onClick = { onNavigate(item.route) }
                ) { Text(stringResource(item.label)) }
            }
            item {
                SegmentedListItem(
                    leadingContent = {
                        Icon(painterResource(Res.drawable.info), null)
                    },
                    supportingContent = {
                        Text(stringResource(Res.string.app_name) + " ${appInfo.versionName}")
                    },
                    trailingContent = if (!widthExpanded) {
                        { Icon(painterResource(Res.drawable.arrow_forward_big), null) }
                    } else null,
                    selected = currentScreen == Screen.Settings.About,
                    shapes = ListItemDefaults.segmentedShapes(1, 2),
                    colors = listItemColors,
                    onClick = { onNavigate(Screen.Settings.About) }
                ) { Text(stringResource(Res.string.about)) }
            }

            item { Spacer(Modifier.height(12.dp)) }

            if (currentLocales != null)
                item {
                    SegmentedListItem(
                        leadingContent = {
                            Icon(painterResource(Res.drawable.language), contentDescription = null)
                        },
                        supportingContent = {
                            Text(
                                if (currentLocalesSize > 0) currentLocales[0].language
                                else stringResource(Res.string.system_default)
                            )
                        },
                        selected = showLocaleSheet,
                        shapes = ListItemDefaults.segmentedShapes(0, 1, singleItemListItemShapes),
                        colors = listItemColors,
                        onClick = { showLocaleSheet = true }
                    ) { Text(stringResource(Res.string.language)) }
                }

            if (androidSdkVersionAtLeast(36) && androidDeviceManufacturerIs("samsung")) {
                item {
                    val uriHandler = LocalUriHandler.current
                    Spacer(Modifier.height(14.dp))
                    SegmentedListItem(
                        leadingContent = {
                            Icon(painterResource(Res.drawable.mobile_text), null)
                        },
                        trailingContent = {
                            Icon(painterResource(Res.drawable.open_in_browser), null)
                        },
                        shapes = ListItemDefaults.segmentedShapes(0, 1, singleItemListItemShapes),
                        colors = listItemColors,
                        onClick = { uriHandler.openUri("https://gist.github.com/nsh07/3b42969aef017d98f72b097f1eca8911") }
                    ) { Text(stringResource(Res.string.now_bar)) }
                }
            }

            item { Spacer(Modifier.height(12.dp)) }

            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {

                    TextButton(
                        onClick = { onAction(SettingsAction.AskEraseData) },
                    ) {
                        Text(stringResource(Res.string.reset_data))
                    }
                }
            }
        }
    }
}