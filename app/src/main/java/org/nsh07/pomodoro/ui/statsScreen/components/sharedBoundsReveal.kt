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

package org.nsh07.pomodoro.ui.statsScreen.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SharedTransitionDefaults
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.navigation3.ui.LocalNavAnimatedContentScope

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Modifier.sharedBoundsReveal(
    sharedContentState: SharedTransitionScope.SharedContentState,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope = LocalNavAnimatedContentScope.current,
    boundsTransform: BoundsTransform = SharedTransitionDefaults.BoundsTransform,
    enter: EnterTransition = fadeIn(),
    exit: ExitTransition = fadeOut(),
    resizeMode: SharedTransitionScope.ResizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
    clipShape: Shape = MaterialTheme.shapes.largeIncreased,
    renderInOverlayDuringTransition: Boolean = true,
): Modifier {
    with(sharedTransitionScope) {
        return this@sharedBoundsReveal
            .sharedBounds(
                sharedContentState = sharedContentState,
                animatedVisibilityScope = animatedVisibilityScope,
                boundsTransform = boundsTransform,
                enter = enter,
                exit = exit,
                resizeMode = resizeMode,
                clipInOverlayDuringTransition = OverlayClip(clipShape),
                renderInOverlayDuringTransition = renderInOverlayDuringTransition,
            )
//            .skipToLookaheadSize()
//            .skipToLookaheadPosition()
    }
}
