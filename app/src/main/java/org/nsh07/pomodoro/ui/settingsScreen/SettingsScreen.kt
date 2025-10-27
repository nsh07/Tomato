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
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.SliderState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import org.nsh07.pomodoro.service.TimerService
import org.nsh07.pomodoro.ui.Screen
import org.nsh07.pomodoro.ui.settingsScreen.components.AboutCard
import org.nsh07.pomodoro.ui.settingsScreen.components.ClickableListItem
import org.nsh07.pomodoro.ui.settingsScreen.screens.AlarmSettings
import org.nsh07.pomodoro.ui.settingsScreen.screens.AppearanceSettings
import org.nsh07.pomodoro.ui.settingsScreen.screens.TimerSettings
import org.nsh07.pomodoro.ui.settingsScreen.viewModel.PreferencesState
import org.nsh07.pomodoro.ui.settingsScreen.viewModel.SettingsViewModel
import org.nsh07.pomodoro.ui.settingsScreens
import org.nsh07.pomodoro.ui.theme.AppFonts.robotoFlexTopBar
import org.nsh07.pomodoro.ui.theme.CustomColors.topBarColors


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenRoot(
    setShowPaywall: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
) {
    val context = LocalContext.current

    val backStack = viewModel.backStack

    DisposableEffect(Unit) {
        viewModel.runTextFieldFlowCollection()
        onDispose { viewModel.cancelTextFieldFlowCollection() }
    }

    val focusTimeInputFieldState = rememberSaveable(saver = TextFieldState.Saver) {
        viewModel.focusTimeTextFieldState
    }
    val shortBreakTimeInputFieldState = rememberSaveable(saver = TextFieldState.Saver) {
        viewModel.shortBreakTimeTextFieldState
    }
    val longBreakTimeInputFieldState = rememberSaveable(saver = TextFieldState.Saver) {
        viewModel.longBreakTimeTextFieldState
    }

    val isPlus by viewModel.isPlus.collectAsStateWithLifecycle()
    val alarmEnabled by viewModel.alarmEnabled.collectAsStateWithLifecycle(true)
    val vibrateEnabled by viewModel.vibrateEnabled.collectAsStateWithLifecycle(true)
    val dndEnabled by viewModel.dndEnabled.collectAsStateWithLifecycle(false)
    val alarmSound by viewModel.alarmSound.collectAsStateWithLifecycle(viewModel.currentAlarmSound)

    val preferencesState by viewModel.preferencesState.collectAsStateWithLifecycle()

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
        preferencesState = preferencesState,
        backStack = backStack,
        focusTimeInputFieldState = focusTimeInputFieldState,
        shortBreakTimeInputFieldState = shortBreakTimeInputFieldState,
        longBreakTimeInputFieldState = longBreakTimeInputFieldState,
        sessionsSliderState = sessionsSliderState,
        alarmEnabled = alarmEnabled,
        vibrateEnabled = vibrateEnabled,
        dndEnabled = dndEnabled,
        alarmSound = alarmSound,
        onAlarmEnabledChange = viewModel::saveAlarmEnabled,
        onVibrateEnabledChange = viewModel::saveVibrateEnabled,
        onBlackThemeChange = viewModel::saveBlackTheme,
        onAodEnabledChange = viewModel::saveAodEnabled,
        onDndEnabledChange = viewModel::saveDndEnabled,
        onAlarmSoundChanged = {
            viewModel.saveAlarmSound(it)
            Intent(context, TimerService::class.java).apply {
                action = TimerService.Actions.RESET.toString()
                context.startService(this)
            }
        },
        onThemeChange = viewModel::saveTheme,
        onColorSchemeChange = viewModel::saveColorScheme,
        setShowPaywall = setShowPaywall,
        modifier = modifier
    )
}

@SuppressLint("LocalContextGetResourceValueCall")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SettingsScreen(
    isPlus: Boolean,
    preferencesState: PreferencesState,
    backStack: SnapshotStateList<Screen.Settings>,
    focusTimeInputFieldState: TextFieldState,
    shortBreakTimeInputFieldState: TextFieldState,
    longBreakTimeInputFieldState: TextFieldState,
    sessionsSliderState: SliderState,
    alarmEnabled: Boolean,
    vibrateEnabled: Boolean,
    dndEnabled: Boolean,
    alarmSound: String,
    onAlarmEnabledChange: (Boolean) -> Unit,
    onVibrateEnabledChange: (Boolean) -> Unit,
    onBlackThemeChange: (Boolean) -> Unit,
    onAodEnabledChange: (Boolean) -> Unit,
    onDndEnabledChange: (Boolean) -> Unit,
    onAlarmSoundChanged: (Uri?) -> Unit,
    onThemeChange: (String) -> Unit,
    onColorSchemeChange: (Color) -> Unit,
    setShowPaywall: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(colorScheme.primary)
                                    .padding(16.dp)
                                    .clickable { setShowPaywall(true) }
                            ) {
                                Icon(
                                    painterResource(R.drawable.tomato_logo_notification),
                                    null,
                                    tint = colorScheme.onPrimary,
                                    modifier = Modifier
                                        .size(24.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Get Tomato+",
                                    style = typography.titleLarge,
                                    fontFamily = robotoFlexTopBar,
                                    color = colorScheme.onPrimary
                                )
                                Spacer(Modifier.weight(1f))
                                Icon(
                                    painterResource(R.drawable.arrow_forward_big),
                                    null,
                                    tint = colorScheme.onPrimary
                                )
                            }
                            Spacer(Modifier.height(14.dp))
                        }

                        item { AboutCard() }

                        item { Spacer(Modifier.height(12.dp)) }

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
                    }
                }
            }

            entry<Screen.Settings.Alarm> {
                AlarmSettings(
                    preferencesState = preferencesState,
                    alarmEnabled = alarmEnabled,
                    vibrateEnabled = vibrateEnabled,
                    alarmSound = alarmSound,
                    onAlarmEnabledChange = onAlarmEnabledChange,
                    onVibrateEnabledChange = onVibrateEnabledChange,
                    onAlarmSoundChanged = onAlarmSoundChanged,
                    onBack = backStack::removeLastOrNull
                )
            }
            entry<Screen.Settings.Appearance> {
                AppearanceSettings(
                    preferencesState = preferencesState,
                    onBlackThemeChange = onBlackThemeChange,
                    onThemeChange = onThemeChange,
                    onColorSchemeChange = onColorSchemeChange,
                    onBack = backStack::removeLastOrNull
                )
            }
            entry<Screen.Settings.Timer> {
                TimerSettings(
                    aodEnabled = preferencesState.aodEnabled,
                    dndEnabled = dndEnabled,
                    focusTimeInputFieldState = focusTimeInputFieldState,
                    shortBreakTimeInputFieldState = shortBreakTimeInputFieldState,
                    longBreakTimeInputFieldState = longBreakTimeInputFieldState,
                    sessionsSliderState = sessionsSliderState,
                    onAodEnabledChange = onAodEnabledChange,
                    onDndEnabledChange = onDndEnabledChange,
                    onBack = backStack::removeLastOrNull
                )
            }
        }
    )
}
