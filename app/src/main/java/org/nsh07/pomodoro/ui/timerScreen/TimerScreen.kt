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

package org.nsh07.pomodoro.ui.timerScreen

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import org.nsh07.pomodoro.R
import org.nsh07.pomodoro.ui.theme.AppFonts.interClock
import org.nsh07.pomodoro.ui.theme.AppFonts.robotoFlexTopBar
import org.nsh07.pomodoro.ui.theme.TomatoTheme
import org.nsh07.pomodoro.ui.timerScreen.viewModel.TimerAction
import org.nsh07.pomodoro.ui.timerScreen.viewModel.TimerMode
import org.nsh07.pomodoro.ui.timerScreen.viewModel.TimerState


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SharedTransitionScope.TimerScreen(
    timerState: TimerState,
    isPlus: Boolean,
    progress: () -> Float,
    onAction: (TimerAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val motionScheme = motionScheme
    val haptic = LocalHapticFeedback.current

    val color by animateColorAsState(
        if (timerState.timerMode == TimerMode.FOCUS) colorScheme.primary
        else colorScheme.tertiary,
        animationSpec = motionScheme.slowEffectsSpec()
    )
    val onColor by animateColorAsState(
        if (timerState.timerMode == TimerMode.FOCUS) colorScheme.onPrimary
        else colorScheme.onTertiary,
        animationSpec = motionScheme.slowEffectsSpec()
    )
    val colorContainer by animateColorAsState(
        if (timerState.timerMode == TimerMode.FOCUS) colorScheme.secondaryContainer
        else colorScheme.tertiaryContainer,
        animationSpec = motionScheme.slowEffectsSpec()
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {}
    )

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Column(modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)) {
        TopAppBar(
            title = {
                AnimatedContent(
                    if (!timerState.showBrandTitle) timerState.timerMode else TimerMode.BRAND,
                    transitionSpec = {
                        slideInVertically(
                            animationSpec = motionScheme.defaultSpatialSpec(),
                            initialOffsetY = { (-it * 1.25).toInt() }
                        ).togetherWith(
                            slideOutVertically(
                                animationSpec = motionScheme.defaultSpatialSpec(),
                                targetOffsetY = { (it * 1.25).toInt() }
                            )
                        )
                    },
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    when (it) {
                        TimerMode.BRAND ->
                            Text(
                                if (!isPlus) stringResource(R.string.app_name)
                                else stringResource(R.string.app_name_plus),
                                style = TextStyle(
                                    fontFamily = robotoFlexTopBar,
                                    fontSize = 32.sp,
                                    lineHeight = 32.sp,
                                    color = colorScheme.error
                                ),
                                textAlign = TextAlign.Center
                            )

                        TimerMode.FOCUS ->
                            Text(
                                stringResource(R.string.focus),
                                style = TextStyle(
                                    fontFamily = robotoFlexTopBar,
                                    fontSize = 32.sp,
                                    lineHeight = 32.sp,
                                    color = colorScheme.primary
                                ),
                                textAlign = TextAlign.Center
                            )

                        TimerMode.SHORT_BREAK -> Text(
                            stringResource(R.string.short_break),
                            style = TextStyle(
                                fontFamily = robotoFlexTopBar,
                                fontSize = 32.sp,
                                lineHeight = 32.sp,
                                color = colorScheme.tertiary
                            ),
                            textAlign = TextAlign.Center
                        )

                        TimerMode.LONG_BREAK -> Text(
                            stringResource(R.string.long_break),
                            style = TextStyle(
                                fontFamily = robotoFlexTopBar,
                                fontSize = 32.sp,
                                lineHeight = 32.sp,
                                color = colorScheme.tertiary
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            },
            subtitle = {},
            titleHorizontalAlignment = CenterHorizontally,
            scrollBehavior = scrollBehavior
        )

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Column(horizontalAlignment = CenterHorizontally) {
                Box(contentAlignment = Alignment.Center) {
                    if (timerState.timerMode == TimerMode.FOCUS) {
                        CircularProgressIndicator(
                            progress = progress,
                            modifier = Modifier
                                .sharedBounds(
                                    sharedContentState = this@TimerScreen.rememberSharedContentState(
                                        "focus progress"
                                    ),
                                    animatedVisibilityScope = LocalNavAnimatedContentScope.current
                                )
                                .widthIn(max = 350.dp)
                                .fillMaxWidth(0.9f)
                                .aspectRatio(1f),
                            color = color,
                            trackColor = colorContainer,
                            strokeWidth = 16.dp,
                            gapSize = 8.dp
                        )
                    } else {
                        CircularWavyProgressIndicator(
                            progress = progress,
                            modifier = Modifier
                                .sharedBounds(
                                    sharedContentState = this@TimerScreen.rememberSharedContentState(
                                        "break progress"
                                    ),
                                    animatedVisibilityScope = LocalNavAnimatedContentScope.current
                                )
                                .widthIn(max = 350.dp)
                                .fillMaxWidth(0.9f)
                                .aspectRatio(1f),
                            color = color,
                            trackColor = colorContainer,
                            stroke = Stroke(
                                width = with(LocalDensity.current) {
                                    16.dp.toPx()
                                },
                                cap = StrokeCap.Round,
                            ),
                            trackStroke = Stroke(
                                width = with(LocalDensity.current) {
                                    16.dp.toPx()
                                },
                                cap = StrokeCap.Round,
                            ),
                            wavelength = 60.dp,
                            gapSize = 8.dp
                        )
                    }
                    var expanded by remember { mutableStateOf(timerState.showBrandTitle) }
                    Column(
                        horizontalAlignment = CenterHorizontally,
                        modifier = Modifier
                            .clip(shapes.largeIncreased)
                            .clickable(onClick = { expanded = !expanded })
                    ) {
                        LaunchedEffect(timerState.showBrandTitle) {
                            expanded = timerState.showBrandTitle
                        }
                        Text(
                            text = timerState.timeStr,
                            style = TextStyle(
                                fontFamily = interClock,
                                fontSize = 72.sp,
                                letterSpacing = (-2).sp,
                                fontFeatureSettings = "tnum"
                            ),
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            modifier = Modifier.sharedBounds(
                                sharedContentState = this@TimerScreen.rememberSharedContentState("clock"),
                                animatedVisibilityScope = LocalNavAnimatedContentScope.current
                            )
                        )
                        AnimatedVisibility(
                            expanded,
                            enter = fadeIn(motionScheme.defaultEffectsSpec()) +
                                    expandVertically(motionScheme.defaultSpatialSpec()),
                            exit = fadeOut(motionScheme.defaultEffectsSpec()) +
                                    shrinkVertically(motionScheme.defaultSpatialSpec())
                        ) {
                            Text(
                                stringResource(
                                    R.string.timer_session_count,
                                    timerState.currentFocusCount,
                                    timerState.totalFocusCount
                                ),
                                fontFamily = interClock,
                                style = typography.titleLarge,
                                color = colorScheme.outline
                            )
                        }
                    }
                }
                val interactionSources = remember { List(3) { MutableInteractionSource() } }
                ButtonGroup(
                    overflowIndicator = { state ->
                        ButtonGroupDefaults.OverflowIndicator(
                            state,
                            colors = IconButtonDefaults.filledTonalIconButtonColors(),
                            modifier = Modifier.size(64.dp, 96.dp)
                        )
                    },
                    modifier = Modifier.padding(16.dp)
                ) {
                    customItem(
                        {
                            FilledIconToggleButton(
                                onCheckedChange = { checked ->
                                    onAction(TimerAction.ToggleTimer)

                                    if (checked) haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                                    else haptic.performHapticFeedback(HapticFeedbackType.ToggleOff)

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && checked) {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                },
                                checked = timerState.timerRunning,
                                colors = IconButtonDefaults.filledIconToggleButtonColors(
                                    checkedContainerColor = color,
                                    checkedContentColor = onColor
                                ),
                                shapes = IconButtonDefaults.toggleableShapes(),
                                interactionSource = interactionSources[0],
                                modifier = Modifier
                                    .size(width = 128.dp, height = 96.dp)
                                    .animateWidth(interactionSources[0])
                            ) {
                                if (timerState.timerRunning) {
                                    Icon(
                                        painterResource(R.drawable.pause_large),
                                        contentDescription = stringResource(R.string.pause),
                                        modifier = Modifier.size(32.dp)
                                    )
                                } else {
                                    Icon(
                                        painterResource(R.drawable.play_large),
                                        contentDescription = stringResource(R.string.play),
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        },
                        { state ->
                            DropdownMenuItem(
                                leadingIcon = {
                                    if (timerState.timerRunning) {
                                        Icon(
                                            painterResource(R.drawable.pause),
                                            contentDescription = stringResource(R.string.pause)
                                        )
                                    } else {
                                        Icon(
                                            painterResource(R.drawable.play),
                                            contentDescription = stringResource(R.string.play)
                                        )
                                    }
                                },
                                text = {
                                    Text(
                                        if (timerState.timerRunning) stringResource(R.string.pause) else stringResource(
                                            R.string.play
                                        )
                                    )
                                },
                                onClick = {
                                    onAction(TimerAction.ToggleTimer)
                                    state.dismiss()
                                }
                            )
                        }
                    )

                    customItem(
                        {
                            FilledTonalIconButton(
                                onClick = {
                                    onAction(TimerAction.ResetTimer)
                                    haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                                },
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = colorContainer
                                ),
                                shapes = IconButtonDefaults.shapes(),
                                interactionSource = interactionSources[1],
                                modifier = Modifier
                                    .size(96.dp)
                                    .animateWidth(interactionSources[1])
                            ) {
                                Icon(
                                    painterResource(R.drawable.restart_large),
                                    contentDescription = stringResource(R.string.restart),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        },
                        { state ->
                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(
                                        painterResource(R.drawable.restart),
                                        stringResource(R.string.restart)
                                    )
                                },
                                text = { Text(stringResource(R.string.restart)) },
                                onClick = {
                                    onAction(TimerAction.ResetTimer)
                                    state.dismiss()
                                }
                            )
                        }
                    )

                    customItem(
                        {
                            FilledTonalIconButton(
                                onClick = {
                                    onAction(TimerAction.SkipTimer(fromButton = true))
                                    haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                                },
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = colorContainer
                                ),
                                shapes = IconButtonDefaults.shapes(),
                                interactionSource = interactionSources[2],
                                modifier = Modifier
                                    .size(64.dp, 96.dp)
                                    .animateWidth(interactionSources[2])
                            ) {
                                Icon(
                                    painterResource(R.drawable.skip_next_large),
                                    contentDescription = stringResource(R.string.skip_to_next),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        },
                        { state ->
                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(
                                        painterResource(R.drawable.skip_next),
                                        stringResource(R.string.skip_to_next)
                                    )
                                },
                                text = { Text(stringResource(R.string.skip_to_next)) },
                                onClick = {
                                    onAction(TimerAction.SkipTimer(fromButton = true))
                                    state.dismiss()
                                }
                            )
                        }
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            Column(horizontalAlignment = CenterHorizontally) {
                Text(stringResource(R.string.up_next), style = typography.titleSmall)
                AnimatedContent(
                    timerState.nextTimeStr,
                    transitionSpec = {
                        slideInVertically(
                            animationSpec = motionScheme.defaultSpatialSpec(),
                            initialOffsetY = { (-it * 1.25).toInt() }
                        ).togetherWith(
                            slideOutVertically(
                                animationSpec = motionScheme.defaultSpatialSpec(),
                                targetOffsetY = { (it * 1.25).toInt() }
                            )
                        )
                    }
                ) {
                    Text(
                        it,
                        style = TextStyle(
                            fontFamily = interClock,
                            fontSize = 22.sp,
                            lineHeight = 28.sp,
                            color = if (timerState.nextTimerMode == TimerMode.FOCUS) colorScheme.primary else colorScheme.tertiary,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.width(200.dp)
                    )
                }
                AnimatedContent(
                    timerState.nextTimerMode,
                    transitionSpec = {
                        slideInVertically(
                            animationSpec = motionScheme.defaultSpatialSpec(),
                            initialOffsetY = { (-it * 1.25).toInt() }
                        ).togetherWith(
                            slideOutVertically(
                                animationSpec = motionScheme.defaultSpatialSpec(),
                                targetOffsetY = { (it * 1.25).toInt() }
                            )
                        )
                    }
                ) {
                    Text(
                        when (it) {
                            TimerMode.FOCUS -> stringResource(R.string.focus)
                            TimerMode.SHORT_BREAK -> stringResource(R.string.short_break)
                            else -> stringResource(R.string.long_break)
                        },
                        style = typography.titleMediumEmphasized,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(200.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Preview(
    showSystemUi = true,
    device = Devices.PIXEL_9_PRO
)
@Composable
fun TimerScreenPreview() {
    val timerState = TimerState(
        timeStr = "03:34", nextTimeStr = "5:00", timerMode = TimerMode.FOCUS, timerRunning = true
    )
    TomatoTheme {
        Surface {
            SharedTransitionLayout {
                TimerScreen(
                    timerState,
                    isPlus = true,
                    { 0.3f },
                    {}
                )
            }
        }
    }
}
