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

package org.nsh07.pomodoro.ui.settingsScreen

import android.annotation.SuppressLint
import android.app.LocaleManager
import android.os.Build
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.SliderState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import org.nsh07.pomodoro.R
import org.nsh07.pomodoro.ui.Screen
import org.nsh07.pomodoro.ui.settingsScreen.components.AboutCard
import org.nsh07.pomodoro.ui.settingsScreen.components.ClickableListItem
import org.nsh07.pomodoro.ui.settingsScreen.components.LocaleBottomSheet
import org.nsh07.pomodoro.ui.settingsScreen.components.PlusPromo
import org.nsh07.pomodoro.ui.settingsScreen.screens.AlarmSettings
import org.nsh07.pomodoro.ui.settingsScreen.screens.AppearanceSettings
import org.nsh07.pomodoro.ui.settingsScreen.screens.TimerSettings
import org.nsh07.pomodoro.ui.settingsScreen.viewModel.SettingsAction
import org.nsh07.pomodoro.ui.settingsScreen.viewModel.SettingsState
import org.nsh07.pomodoro.ui.settingsScreen.viewModel.SettingsViewModel
import org.nsh07.pomodoro.ui.settingsScreens
import org.nsh07.pomodoro.ui.theme.AppFonts.robotoFlexTopBar
import org.nsh07.pomodoro.ui.theme.CustomColors.listItemColors
import org.nsh07.pomodoro.ui.theme.CustomColors.topBarColors


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenRoot(
    setShowPaywall: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
) {
    val backStack = viewModel.backStack

    DisposableEffect(Unit) {
        viewModel.runTextFieldFlowCollection()
        onDispose { viewModel.cancelTextFieldFlowCollection() }
    }

    val focusTimeInputFieldState = viewModel.focusTimeTextFieldState
    val shortBreakTimeInputFieldState = viewModel.shortBreakTimeTextFieldState
    val longBreakTimeInputFieldState = viewModel.longBreakTimeTextFieldState

    val isPlus by viewModel.isPlus.collectAsStateWithLifecycle()
    val serviceRunning by viewModel.serviceRunning.collectAsStateWithLifecycle()

    val settingsState by viewModel.settingsState.collectAsStateWithLifecycle()

    val sessionsSliderState = rememberSaveable(
        saver = SliderState.Saver(
            viewModel.sessionsSliderState.onValueChangeFinished,
            viewModel.sessionsSliderState.valueRange
        )
    ) {
        viewModel.sessionsSliderState
    }

    SettingsScreen(
        isPlus = isPlus,
        serviceRunning = serviceRunning,
        settingsState = settingsState,
        backStack = backStack,
        focusTimeInputFieldState = focusTimeInputFieldState,
        shortBreakTimeInputFieldState = shortBreakTimeInputFieldState,
        longBreakTimeInputFieldState = longBreakTimeInputFieldState,
        sessionsSliderState = sessionsSliderState,
        onAction = viewModel::onAction,
        setShowPaywall = setShowPaywall,
        modifier = modifier
    )
}

@SuppressLint("LocalContextGetResourceValueCall")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SettingsScreen(
    isPlus: Boolean,
    serviceRunning: Boolean,
    settingsState: SettingsState,
    backStack: SnapshotStateList<Screen.Settings>,
    focusTimeInputFieldState: TextFieldState,
    shortBreakTimeInputFieldState: TextFieldState,
    longBreakTimeInputFieldState: TextFieldState,
    sessionsSliderState: SliderState,
    onAction: (SettingsAction) -> Unit,
    setShowPaywall: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    val currentLocales =
        if (Build.VERSION.SDK_INT >= 33) {
            context
                .getSystemService(LocaleManager::class.java)
                .applicationLocales
        } else null
    val currentLocalesSize = currentLocales?.size() ?: 0

    var showLocaleSheet by remember { mutableStateOf(false) }

    if (showLocaleSheet && currentLocales != null)
        LocaleBottomSheet(
            currentLocales = currentLocales,
            setShowSheet = { showLocaleSheet = it }
        )

    NavDisplay(
        backStack = backStack,
        onBack = backStack::removeLastOrNull,
        transitionSpec = {
            (slideInHorizontally(initialOffsetX = { it }))
                .togetherWith(slideOutHorizontally(targetOffsetX = { -it / 4 }) + fadeOut())
        },
        popTransitionSpec = {
            (slideInHorizontally(initialOffsetX = { -it / 4 }) + fadeIn())
                .togetherWith(slideOutHorizontally(targetOffsetX = { it }))
        },
        predictivePopTransitionSpec = {
            (slideInHorizontally(initialOffsetX = { -it / 4 }) + fadeIn())
                .togetherWith(slideOutHorizontally(targetOffsetX = { it }))
        },
        entryProvider = entryProvider {
            entry<Screen.Settings.Main> {
                Column(modifier.nestedScroll(scrollBehavior.nestedScrollConnection)) {
                    TopAppBar(
                        title = {
                            Text(
                                stringResource(R.string.settings),
                                style = LocalTextStyle.current.copy(
                                    fontFamily = robotoFlexTopBar,
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

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier
                            .background(topBarColors.containerColor)
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        item { Spacer(Modifier.height(12.dp)) }

                        if (!isPlus) item {
                            PlusPromo(isPlus, setShowPaywall)
                            Spacer(Modifier.height(14.dp))
                        }

                        item { AboutCard(isPlus) }

                        item { Spacer(Modifier.height(12.dp)) }

                        if (isPlus) item {
                            PlusPromo(isPlus, setShowPaywall)
                            Spacer(Modifier.height(14.dp))
                        }

                        itemsIndexed(settingsScreens) { index, item ->
                            ClickableListItem(
                                leadingContent = {
                                    Icon(painterResource(item.icon), null)
                                },
                                headlineContent = { Text(stringResource(item.label)) },
                                supportingContent = {
                                    Text(
                                        remember {
                                            item.innerSettings.joinToString(", ") {
                                                context.getString(it)
                                            }
                                        },
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                trailingContent = {
                                    Icon(painterResource(R.drawable.arrow_forward_big), null)
                                },
                                items = settingsScreens.size,
                                index = index
                            ) { backStack.add(item.route) }
                        }

                        item { Spacer(Modifier.height(12.dp)) }

                        if (currentLocales != null)
                            item {
                                ClickableListItem(
                                    leadingContent = {
                                        Icon(
                                            painterResource(R.drawable.language),
                                            contentDescription = null
                                        )
                                    },
                                    headlineContent = { Text(stringResource(R.string.language)) },
                                    supportingContent = {
                                        Text(
                                            if (currentLocalesSize > 0) currentLocales.get(0).displayName
                                            else stringResource(R.string.system_default)
                                        )
                                    },
                                    colors = listItemColors,
                                    items = 1,
                                    index = 0
                                ) { showLocaleSheet = true }
                            }

                        item { Spacer(Modifier.height(12.dp)) }
                    }
                }
            }

            entry<Screen.Settings.Alarm> {
                AlarmSettings(
                    settingsState = settingsState,
                    onAction = onAction,
                    onBack = backStack::removeLastOrNull
                )
            }
            entry<Screen.Settings.Appearance> {
                AppearanceSettings(
                    settingsState = settingsState,
                    isPlus = isPlus,
                    onAction = onAction,
                    setShowPaywall = setShowPaywall,
                    onBack = backStack::removeLastOrNull
                )
            }
            entry<Screen.Settings.Timer> {
                TimerSettings(
                    isPlus = isPlus,
                    serviceRunning = serviceRunning,
                    settingsState = settingsState,
                    focusTimeInputFieldState = focusTimeInputFieldState,
                    shortBreakTimeInputFieldState = shortBreakTimeInputFieldState,
                    longBreakTimeInputFieldState = longBreakTimeInputFieldState,
                    sessionsSliderState = sessionsSliderState,
                    onAction = onAction,
                    setShowPaywall = setShowPaywall,
                    onBack = backStack::removeLastOrNull,
                )
            }
        }
    )
}
