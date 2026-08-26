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

package org.nsh07.pomodoro.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

fun HapticFeedback.performToggle(checked: Boolean) =
    performHapticFeedback(
        if (checked) HapticFeedbackType.ToggleOn else HapticFeedbackType.ToggleOff
    )

fun HapticFeedback.performConfirm() = performHapticFeedback(HapticFeedbackType.Confirm)

fun HapticFeedback.performReject() = performHapticFeedback(HapticFeedbackType.Reject)

fun HapticFeedback.performLongPress() = performHapticFeedback(HapticFeedbackType.LongPress)

fun HapticFeedback.performSegmentTick() = performHapticFeedback(HapticFeedbackType.SegmentTick)

fun HapticFeedback.performSegmentFrequentTick() =
    performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)

fun HapticFeedback.performVirtualKey() = performHapticFeedback(HapticFeedbackType.VirtualKey)

@Composable
fun <T> rememberSliderTickHaptics(
    frequent: Boolean = true,
    step: (Float) -> T
): (Float) -> Unit {
    val haptic = LocalHapticFeedback.current
    val currentStep by rememberUpdatedState(step)

    return remember(haptic, frequent) {
        var last: T? = null

        { value ->
            val quantised = currentStep(value)
            if (quantised != last) {
                if (last != null) {
                    if (frequent) haptic.performSegmentFrequentTick()
                    else haptic.performSegmentTick()
                }
                last = quantised
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SliderTickHaptics(state: SliderState, frequent: Boolean = false) {
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(state, haptic, frequent) {
        var last = state.value
        snapshotFlow { state.value to state.isDragging }
            .collect { (value, dragging) ->
                if (value == last) return@collect
                last = value
                if (!dragging) return@collect

                if (frequent) haptic.performSegmentFrequentTick()
                else haptic.performSegmentTick()
            }
    }
}
