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

package org.nsh07.pomodoro.ui.settingsScreen.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import org.nsh07.pomodoro.ui.theme.AppFonts.googleFlex600
import org.nsh07.pomodoro.ui.theme.CustomColors.listItemColors

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MinuteInputField(
    state: TextFieldState,
    enabled: Boolean,
    shape: Shape,
    modifier: Modifier = Modifier,
    inputTransformation: MinutesInputTransformation = MinutesInputTransformation2Digits,
    imeAction: ImeAction = ImeAction.Next
) {
    BasicTextField(
        state = state,
        enabled = enabled,
        lineLimits = TextFieldLineLimits.SingleLine,
        inputTransformation = inputTransformation,
//        outputTransformation = MinutesOutputTransformation,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.NumberPassword,
            imeAction = imeAction
        ),
        textStyle = TextStyle(
            fontFamily = googleFlex600,
            fontSize = 57.sp,
            letterSpacing = (-2).sp,
            color = if (enabled) colorScheme.onSurfaceVariant else colorScheme.outlineVariant,
            textAlign = TextAlign.Center
        ),
        cursorBrush = SolidColor(colorScheme.onSurface),
        decorator = { innerTextField ->
            val text = state.text
            val width by animateDpAsState(
                if (text.length < 3) 112.dp else 140.dp,
                motionScheme.defaultSpatialSpec()
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier = modifier
                    .size(width, 100.dp)
                    .background(
                        animateColorAsState(
                            if (text.isNotEmpty())
                                listItemColors.containerColor
                            else colorScheme.errorContainer,
                            motionScheme.defaultEffectsSpec()
                        ).value,
                        shape
                    )
            ) { innerTextField() }
        }
    )
}