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

package org.nsh07.pomodoro

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowScope
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.nsh07.pomodoro.utils.OS
import org.nsh07.pomodoro.utils.currentOS
import tomato.shared.generated.resources.Res
import tomato.shared.generated.resources.app_name
import tomato.shared.generated.resources.clear
import tomato.shared.generated.resources.exit
import tomato.shared.generated.resources.window_maximize
import tomato.shared.generated.resources.window_minimize
import tomato.shared.generated.resources.window_restore

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WindowScope.AppTitleBar(
    windowFloating: Boolean,
    onMinimize: () -> Unit,
    onMaximizeRestore: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) = WindowDraggableArea {
    val isMacOS = currentOS == OS.MACOS
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isMacOS) Modifier.height(28.dp)
                else Modifier
            )
    ) {
        CompositionLocalProvider(LocalContentColor provides colorScheme.onSurface) {
            Text(
                stringResource(Res.string.app_name),
                style = typography.titleSmall,
                color = colorScheme.onSurface
            )

            if (!isMacOS) {
                Row(
//                horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    TitleBarButton(
                        onClick = onMinimize,
                        iconPainter = painterResource(Res.drawable.window_minimize),
                        contentDescription = null
                    )

                    TitleBarButton(
                        onClick = onMaximizeRestore,
                        iconPainter = painterResource(
                            if (windowFloating) Res.drawable.window_maximize
                            else Res.drawable.window_restore
                        ),
                        contentDescription = null
                    )

                    TitleBarButton(
                        onClick = onClose,
                        iconPainter = painterResource(Res.drawable.clear),
                        contentDescription = stringResource(Res.string.exit),
                        hoveredContainerColor = colorScheme.error,
                        hoveredContentColor = colorScheme.onError
                    )
                }
            }
        }
    }
}

@Composable
private fun TitleBarButton(
    onClick: () -> Unit,
    iconPainter: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    hoveredContainerColor: Color = colorScheme.onSurfaceVariant.copy(0.08f),
    hoveredContentColor: Color = LocalContentColor.current
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val hoverFraction by animateFloatAsState(if (isHovered) 1f else 0f)
    val containerColor = lerp(Color.Transparent, hoveredContainerColor, hoverFraction)
    val contentColor = lerp(LocalContentColor.current, hoveredContentColor, hoverFraction)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .background(containerColor)
            .size(32.dp)
            .clickable(interactionSource = interactionSource, onClick = onClick)
    ) {
        Icon(
            iconPainter,
            contentDescription,
            tint = contentColor,
            modifier = Modifier.size(18.dp)
        )
    }
}
