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

package org.nsh07.pomodoro.ui.timerScreen

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import kotlinx.coroutines.flow.collectLatest
import org.nsh07.pomodoro.ui.timerScreen.viewModel.TimerMode
import org.nsh07.pomodoro.ui.timerScreen.viewModel.TimerState
import kotlin.math.abs

@Composable
fun SharedTransitionScope.TimerProgressIndicator(
    timerState: TimerState,
    progress: () -> Float,
    color: Color,
    trackColor: Color,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 16.dp,
    gapSize: Dp = 8.dp,
    wavelength: Dp = 60.dp,
    animate: Boolean = true,
) {
    val displayProgress = if (animate) {
        rememberAnimatedTimerProgress(
            timerRunning = timerState.timerRunning,
            timerMode = timerState.timerMode,
            totalTime = timerState.totalTime,
            progress = progress,
        )
    } else {
        progress
    }

    val density = LocalDensity.current
    if (timerState.timerMode == TimerMode.FOCUS) {
        CircularProgressIndicator(
            progress = displayProgress,
            modifier = Modifier
                .sharedBounds(
                    sharedContentState = rememberSharedContentState("focus progress"),
                    animatedVisibilityScope = LocalNavAnimatedContentScope.current
                )
                .then(modifier),
            color = color,
            trackColor = trackColor,
            strokeWidth = strokeWidth,
            gapSize = gapSize
        )
    } else {
        val stroke = remember(density, strokeWidth) {
            Stroke(
                width = with(density) { strokeWidth.toPx() },
                cap = StrokeCap.Round,
            )
        }
        CircularWavyProgressIndicator(
            progress = displayProgress,
            modifier = Modifier
                .sharedBounds(
                    sharedContentState = rememberSharedContentState("break progress"),
                    animatedVisibilityScope = LocalNavAnimatedContentScope.current
                )
                .then(modifier),
            color = color,
            trackColor = trackColor,
            stroke = stroke,
            trackStroke = stroke,
            wavelength = wavelength,
            gapSize = gapSize
        )
    }
}

@Composable
private fun rememberAnimatedTimerProgress(
    timerRunning: Boolean,
    timerMode: TimerMode,
    totalTime: Long,
    progress: () -> Float,
): () -> Float {
    val motionScheme = motionScheme
    val animatedProgress = remember { Animatable(progress().coerceIn(0f, 1f)) }
    var resyncCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(timerRunning, timerMode, totalTime, resyncCount) {
        if (timerRunning && totalTime > 0L) {
            val current = progress().coerceIn(0f, 1f)
            animatedProgress.snapTo(current)
            val remainingMs = ((1f - current) * totalTime).toLong().coerceAtLeast(0L)
            if (remainingMs > 0L) {
                animatedProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = remainingMs.coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
                        easing = LinearEasing
                    )
                )
            }
        }
    }

    LaunchedEffect(timerRunning, totalTime) {
        snapshotFlow { progress().coerceIn(0f, 1f) }.collectLatest { latest ->
            if (!timerRunning) {
                if (animatedProgress.value != latest) {
                    animatedProgress.animateTo(
                        targetValue = latest,
                        animationSpec = motionScheme.defaultEffectsSpec()
                    )
                }
            } else if (totalTime > 0L) {
                val driftThreshold = (1500f / totalTime).coerceAtLeast(0.005f)
                if (latest < animatedProgress.value - 0.001f ||
                    abs(animatedProgress.value - latest) > driftThreshold
                ) {
                    resyncCount++
                }
            }
        }
    }

    return { animatedProgress.value }
}
