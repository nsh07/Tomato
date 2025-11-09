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

package org.nsh07.pomodoro.ui

import android.app.Activity
import android.view.WindowManager
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import kotlinx.coroutines.delay
import org.nsh07.pomodoro.ui.theme.AppFonts.interClock
import org.nsh07.pomodoro.ui.theme.TomatoTheme
import org.nsh07.pomodoro.ui.timerScreen.TimerScreen
import org.nsh07.pomodoro.ui.timerScreen.viewModel.TimerMode
import org.nsh07.pomodoro.ui.timerScreen.viewModel.TimerState
import kotlin.random.Random

/**
 * Always On Display composable. Must be called within a [SharedTransitionScope] which allows
 * animating the clock and progress indicator
 *
 * @param timerState [TimerState] instance. This must be the same instance as the one used on the
 * root [TimerScreen] composable
 * @param progress lambda that returns the current progress of the clock
 * randomized offset for the clock to allow smooth motion with sharedBounds
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SharedTransitionScope.AlwaysOnDisplay(
    timerState: TimerState,
    progress: () -> Float,
    setTimerFrequency: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var sharedElementTransitionComplete by remember { mutableStateOf(false) }

    val activity = LocalActivity.current
    val density = LocalDensity.current
    val windowInfo = LocalWindowInfo.current
    val view = LocalView.current

    val window = remember { (view.context as Activity).window }
    val insetsController = remember { WindowCompat.getInsetsController(window, view) }

    DisposableEffect(Unit) {
        setTimerFrequency(1f)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        )
        activity?.setShowWhenLocked(true)
        insetsController.apply {
            hide(WindowInsetsCompat.Type.statusBars())
            hide(WindowInsetsCompat.Type.navigationBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        onDispose {
            setTimerFrequency(60f)
            window.clearFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
            )
            activity?.setShowWhenLocked(false)
            insetsController.apply {
                show(WindowInsetsCompat.Type.statusBars())
                show(WindowInsetsCompat.Type.navigationBars())
                systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
            }
        }
    }

    LaunchedEffect(Unit) {
        delay(300)
        sharedElementTransitionComplete = true
    }

    val primary by animateColorAsState(
        if (sharedElementTransitionComplete) Color(0xFFA2A2A2)
        else {
            if (timerState.timerMode == TimerMode.FOCUS) colorScheme.primary
            else colorScheme.tertiary
        },
        animationSpec = motionScheme.slowEffectsSpec()
    )
    val secondaryContainer by animateColorAsState(
        if (sharedElementTransitionComplete) Color(0xFF1D1D1D)
        else {
            if (timerState.timerMode == TimerMode.FOCUS) colorScheme.secondaryContainer
            else colorScheme.tertiaryContainer
        },
        animationSpec = motionScheme.slowEffectsSpec()
    )
    val surface by animateColorAsState(
        if (sharedElementTransitionComplete) Color.Black
        else colorScheme.surface,
        animationSpec = motionScheme.slowEffectsSpec()
    )
    val onSurface by animateColorAsState(
        if (sharedElementTransitionComplete) Color(0xFFE3E3E3)
        else colorScheme.onSurface,
        animationSpec = motionScheme.slowEffectsSpec()
    )

    var randomX by remember {
        mutableIntStateOf(
            Random.nextInt(
                16.dp.toIntPx(density),
                windowInfo.containerSize.width - 266.dp.toIntPx(density)
            )
        )
    }
    var randomY by remember {
        mutableIntStateOf(
            Random.nextInt(
                16.dp.toIntPx(density),
                windowInfo.containerSize.height - 266.dp.toIntPx(density)
            )
        )
    }

    LaunchedEffect(timerState.timeStr[1]) { // Randomize position every minute
        if (sharedElementTransitionComplete) {
            randomX = Random.nextInt(
                16.dp.toIntPx(density),
                windowInfo.containerSize.width - 266.dp.toIntPx(density)
            )
            randomY = Random.nextInt(
                16.dp.toIntPx(density),
                windowInfo.containerSize.height - 266.dp.toIntPx(density)
            )
        }
    }

    val x by animateIntAsState(randomX, motionScheme.slowSpatialSpec())
    val y by animateIntAsState(randomY, motionScheme.slowSpatialSpec())

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(surface)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.offset {
                IntOffset(x, y)
            }
        ) {
            if (timerState.timerMode == TimerMode.FOCUS) {
                CircularProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .sharedBounds(
                            sharedContentState = this@AlwaysOnDisplay.rememberSharedContentState("focus progress"),
                            animatedVisibilityScope = LocalNavAnimatedContentScope.current
                        )
                        .size(250.dp),
                    color = primary,
                    trackColor = secondaryContainer,
                    strokeWidth = 12.dp,
                    gapSize = 8.dp,
                )
            } else {
                CircularWavyProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .sharedBounds(
                            sharedContentState = this@AlwaysOnDisplay.rememberSharedContentState("break progress"),
                            animatedVisibilityScope = LocalNavAnimatedContentScope.current
                        )
                        .size(250.dp),
                    color = primary,
                    trackColor = secondaryContainer,
                    stroke = Stroke(
                        width = with(LocalDensity.current) {
                            12.dp.toPx()
                        },
                        cap = StrokeCap.Round,
                    ),
                    trackStroke = Stroke(
                        width = with(LocalDensity.current) {
                            12.dp.toPx()
                        },
                        cap = StrokeCap.Round,
                    ),
                    wavelength = 42.dp,
                    gapSize = 8.dp
                )
            }

            Text(
                text = timerState.timeStr,
                style = TextStyle(
                    fontFamily = interClock,
                    fontSize = 56.sp,
                    letterSpacing = (-2).sp,
                    fontFeatureSettings = "tnum"
                ),
                textAlign = TextAlign.Center,
                color = onSurface,
                maxLines = 1,
                modifier = Modifier.sharedBounds(
                    sharedContentState = this@AlwaysOnDisplay.rememberSharedContentState("clock"),
                    animatedVisibilityScope = LocalNavAnimatedContentScope.current
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Preview
@Composable
private fun AlwaysOnDisplayPreview() {
    val timerState = TimerState()
    val progress = { 0.5f }
    TomatoTheme {
        SharedTransitionLayout {
            AlwaysOnDisplay(
                timerState = timerState,
                progress = progress,
                setTimerFrequency = {}
            )
        }
    }
}

fun Dp.toIntPx(density: Density) = with(density) { toPx().toInt() }
