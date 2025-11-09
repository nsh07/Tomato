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

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.nsh07.pomodoro.BuildConfig
import org.nsh07.pomodoro.R
import org.nsh07.pomodoro.ui.theme.AppFonts.robotoFlexTopBar

// Taken from https://github.com/shub39/Grit/blob/master/app/src/main/java/com/shub39/grit/core/presentation/settings/ui/component/AboutApp.kt
@Composable
fun AboutCard(
    isPlus: Boolean,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.primaryContainer,
            contentColor = colorScheme.onPrimaryContainer
        ),
        shape = shapes.extraLarge
    ) {
        val buttonColors = ButtonDefaults.buttonColors(
            containerColor = colorScheme.onPrimaryContainer,
            contentColor = colorScheme.primaryContainer
        )

        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column {
                Text(
                    if (!isPlus) stringResource(R.string.app_name)
                    else stringResource(R.string.app_name_plus),
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = robotoFlexTopBar
                )
                Text(text = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
            }

            Spacer(modifier = Modifier.weight(1f))

            Row {
                IconButton(
                    onClick = {
                        Toast.makeText(context, "Coming soon...", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(
                        painterResource(R.drawable.discord),
                        contentDescription = "Discord",
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(
                    onClick = { uriHandler.openUri("https://github.com/nsh07/Tomato") }
                ) {
                    Icon(
                        painterResource(R.drawable.github),
                        contentDescription = "GitHub",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        FlowRow(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TopButton(buttonColors)
            BottomButton(buttonColors)
        }
    }
}