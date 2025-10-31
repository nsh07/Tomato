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

package org.nsh07.pomodoro.billing

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.nsh07.pomodoro.R
import org.nsh07.pomodoro.ui.theme.AppFonts.robotoFlexTopBar

@Composable
fun TomatoPlusPaywallDialog(
    isPlus: Boolean,
    onDismiss: () -> Unit
) {
    val uriHandler = LocalUriHandler.current

    BackHandler(enabled = true, onDismiss)

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.surface)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painterResource(R.drawable.bmc),
                null,
                tint = colorScheme.onSurface
            )
            Spacer(Modifier.height(16.dp))
            Text(
                stringResource(R.string.tomato_foss),
                style = typography.headlineSmall,
                fontFamily = robotoFlexTopBar,
                color = colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.tomato_foss_desc, "BuyMeACoffee"),
                textAlign = TextAlign.Center,
                color = colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = { uriHandler.openUri("https://coff.ee/nsh07") }) {
                Text(stringResource(R.string.bmc))
            }
        }
    }
}