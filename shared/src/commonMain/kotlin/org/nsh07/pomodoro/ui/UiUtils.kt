/*
 * Copyright (c) 2025-2026 Nishant Mishra
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

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.allVerticalHingeBounds
import androidx.compose.material3.adaptive.layout.HingePolicy
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.occludingVerticalHingeBounds
import androidx.compose.material3.adaptive.separatingVerticalHingeBounds
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.WindowInfo
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import org.nsh07.pomodoro.ui.settingsScreen.viewModel.SettingsAction

@Composable
fun mergePaddingValues(
    topSource: PaddingValues,
    restSource: PaddingValues
): PaddingValues {
    val layoutDirection = LocalLayoutDirection.current

    return PaddingValues(
        top = topSource.calculateTopPadding(),
        bottom = restSource.calculateBottomPadding(),
        start = restSource.calculateStartPadding(layoutDirection),
        end = restSource.calculateEndPadding(layoutDirection)
    )
}

/**
 * A [androidx.compose.runtime.DisposableEffect] that makes the app full-screen and exits
 * full-screen when this function exits composition
 */
@Composable
expect fun AodSystemBarsHandler(
    density: Density,
    windowInfo: WindowInfo,
    secureAod: Boolean,
    setTimerFrequency: (Float) -> Unit
)

/**
 * Returns and remembers a lambda that requests the system for the DND permission
 *
 * @return an empty lambda on all platforms except Android, where it returns a lambda that launches
 * a permission settings menu, and shows a Toast that instructs the user to grant the permission.
 */
@Composable
expect fun rememberRequestDndPermissionCallback(): (Boolean) -> Unit

/**
 * Returns and remembers a lambda that requests the system for the notification permission
 *
 * @return a lambda that launches the current platform's corresponding notification permission
 * dialog
 */
@Composable
expect fun rememberRequestNotificationPermissionCallback(): () -> Unit

/**
 * Returns and remembers a lambda that launches the ringtone picker
 *
 * @param alarmSoundFilePath string representation of the existing ringtone's path
 * @param onResult lambda that passes the ringtone picked by the user in an instance of
 * [SettingsAction.SaveAlarmSound]. This should essentially be a [androidx.lifecycle.ViewModel]'s
 * MVI intent handler.
 *
 * @return a lambda that launches the current platform's ringtone/media picker
 */
@Composable
expect fun rememberRingtonePickerLauncherCallback(
    alarmSoundFilePath: String?,
    onResult: (SettingsAction) -> Unit
): suspend () -> Unit

/**
 * Returns and remembers a lambda that returns the name of the current ringtone
 *
 * @return a lambda that accepts the string representation of the existing ringtone's path and
 * returns its name as a [String]
 */
@Composable
expect fun rememberRingtoneNameProviderCallback(): suspend (String?) -> String

/**
 * (copied from [androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective] with
 * minor modifications, namely the reduction of horizontalPartitionSpacerSize to 0.dp)
 *
 * Calculates the recommended [PaneScaffoldDirective] from a given [WindowAdaptiveInfo]. Use this
 * method with [androidx.compose.material3.adaptive.currentWindowAdaptiveInfo] to acquire
 * Material-recommended adaptive layout settings of the current activity window.
 *
 * See more details on the [Material design guideline site]
 * (https://m3.material.io/foundations/layout/applying-layout/window-size-classes).
 *
 * @param windowAdaptiveInfo [WindowAdaptiveInfo] that collects useful information in making layout
 *   adaptation decisions like [WindowSizeClass].
 * @param verticalHingePolicy [HingePolicy] that decides how layouts are supposed to address
 *   vertical hinges.
 * @return an [PaneScaffoldDirective] to be used to decide adaptive layout states.
 */
fun calculatePaneScaffoldDirective(
    windowAdaptiveInfo: WindowAdaptiveInfo,
    verticalHingePolicy: HingePolicy = HingePolicy.AvoidSeparating,
): PaneScaffoldDirective {
    val maxHorizontalPartitions: Int
    val horizontalPartitionSpacerSize: Dp
    val defaultPanePreferredWidth: Dp
    when (windowAdaptiveInfo.windowSizeClass.minWidthDp) {
        0 -> {
            maxHorizontalPartitions = 1
            horizontalPartitionSpacerSize = 0.dp
            defaultPanePreferredWidth = 360.dp
        }

        WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND -> {
            maxHorizontalPartitions = 1
            horizontalPartitionSpacerSize = 0.dp
            defaultPanePreferredWidth = 360.dp
        }

        WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND -> {
            maxHorizontalPartitions = 2
            horizontalPartitionSpacerSize = 0.dp
            defaultPanePreferredWidth = 360.dp
        }

        else -> {
            maxHorizontalPartitions = 3
            horizontalPartitionSpacerSize = 0.dp
            defaultPanePreferredWidth = 412.dp
        }
    }
    val maxVerticalPartitions: Int
    val verticalPartitionSpacerSize: Dp

    if (
        windowAdaptiveInfo.windowPosture.isTabletop ||
        (maxHorizontalPartitions == 1 &&
                windowAdaptiveInfo.windowSizeClass.minHeightDp ==
                WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)
    ) {
        maxVerticalPartitions = 2
        verticalPartitionSpacerSize = 24.dp
    } else {
        maxVerticalPartitions = 1
        verticalPartitionSpacerSize = 0.dp
    }

    val defaultPanePreferredHeight = 420.dp

    return PaneScaffoldDirective(
        maxHorizontalPartitions = maxHorizontalPartitions,
        horizontalPartitionSpacerSize = horizontalPartitionSpacerSize,
        maxVerticalPartitions = maxVerticalPartitions,
        verticalPartitionSpacerSize = verticalPartitionSpacerSize,
        defaultPanePreferredWidth = defaultPanePreferredWidth,
        defaultPanePreferredHeight = defaultPanePreferredHeight,
        excludedBounds = when (verticalHingePolicy) {
            HingePolicy.AvoidSeparating -> windowAdaptiveInfo.windowPosture.separatingVerticalHingeBounds
            HingePolicy.AvoidOccluding -> windowAdaptiveInfo.windowPosture.occludingVerticalHingeBounds
            HingePolicy.AlwaysAvoid -> windowAdaptiveInfo.windowPosture.allVerticalHingeBounds
            else -> emptyList()
        }
    )
}

expect fun Modifier.androidSystemGestureExclusion(): Modifier

expect fun htmlToAnnotatedString(html: String): AnnotatedString

expect fun Modifier.hideCursor(): Modifier