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

package org.nsh07.pomodoro.ui.settingsScreen.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.animate
import androidx.compose.foundation.style.focused
import androidx.compose.foundation.style.rememberUpdatedStyleState
import androidx.compose.foundation.style.styleable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.nsh07.pomodoro.ui.theme.CustomColors.listItemColors

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalFoundationStyleApi::class)
@Composable
fun MinuteInputField(
    minutes: String,
    onMinutesChange: (String) -> Unit,
    enabled: Boolean,
    shape: Shape,
    modifier: Modifier = Modifier,
    maxDigits: Int = 2,
    imeAction: ImeAction = ImeAction.Next
) {
    val interactionSource = remember { MutableInteractionSource() }
    val styleState = rememberUpdatedStyleState(interactionSource) {
        it.isEnabled = enabled
    }
    val colorScheme = colorScheme
    val spatialSpec = motionScheme.defaultSpatialSpec<Float>()
    val containerColor by animateColorAsState(
        when {
            !minutes.isValidMinutesInput() -> colorScheme.errorContainer
            styleState.isFocused -> colorScheme.primaryContainer
            else -> listItemColors.containerColor
        },
        motionScheme.defaultEffectsSpec()
    )
    val textColor by animateColorAsState(
        when {
            !minutes.isValidMinutesInput() -> colorScheme.onErrorContainer
            !enabled -> colorScheme.outlineVariant
            styleState.isFocused -> colorScheme.onPrimaryContainer
            else -> colorScheme.onSurfaceVariant
        },
        motionScheme.defaultEffectsSpec()
    )

    BasicTextField(
        value = minutes,
        onValueChange = {
            val trimmed = it.trimStart('0')
            if (trimmed.isMinutesInput(maxDigits)) onMinutesChange(trimmed)
        },
        enabled = enabled,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.NumberPassword,
            imeAction = imeAction
        ),
        textStyle = TextStyle(
            fontFamily = typography.bodyLarge.fontFamily,
            fontSize = 56.sp,
            lineHeight = 64.sp,
            letterSpacing = (-1).sp,
            color = textColor,
            textAlign = TextAlign.Center
        ),
        interactionSource = interactionSource,
        cursorBrush = SolidColor(textColor.copy(alpha = 0.5f)),
        decorationBox = { innerTextField ->
            val width by animateDpAsState(
                if (minutes.length < 3) 112.dp else 140.dp,
                motionScheme.defaultSpatialSpec()
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier = modifier
                    .size(width, 100.dp)
                    .styleable(styleState) {
                        shape(shape)
                        background(containerColor)
                        focused { animate(spatialSpec) { shape(RoundedCornerShape(28.dp)) } }
                    }
            ) { innerTextField() }
        }
    )
}
