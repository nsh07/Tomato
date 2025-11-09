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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.nsh07.pomodoro.R

@Composable
fun TopButton(
    buttonColors: ButtonColors,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current
    Button(
        colors = buttonColors,
        onClick = { uriHandler.openUri("https://hosted.weblate.org/engage/tomato/") },
        modifier = modifier
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painterResource(R.drawable.weblate),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )

            Text(text = stringResource(R.string.help_with_translation))
        }
    }
}

@Composable
fun BottomButton(
    buttonColors: ButtonColors,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current
    Button(
        colors = buttonColors,
        onClick = { uriHandler.openUri("https://play.google.com/store/apps/details?id=org.nsh07.pomodoro") },
        modifier = modifier
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painterResource(R.drawable.play_store),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )

            Text(text = stringResource(R.string.rate_on_google_play))
        }
    }
}