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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Window

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ApplicationScope.AppWindow() =
    Window(
        onCloseRequest = ::exitApplication,
        title = "Tomato"
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            CircularWavyProgressIndicator()
        }
    }