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

import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.nsh07.pomodoro.R
import org.nsh07.pomodoro.ui.theme.CustomColors.listItemColors
import org.nsh07.pomodoro.ui.theme.CustomColors.switchColors
import org.nsh07.pomodoro.ui.theme.TomatoShapeDefaults.bottomListItemShape
import org.nsh07.pomodoro.ui.theme.TomatoShapeDefaults.middleListItemShape
import org.nsh07.pomodoro.ui.theme.TomatoShapeDefaults.topListItemShape

@Composable
fun ColorSchemePickerListItem(
    color: Color,
    items: Int,
    index: Int,
    isPlus: Boolean,
    onColorChange: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    val colorSchemes = listOf(
        Color(0xfffeb4a7), Color(0xffffb3c0), Color(0xfffcaaff), Color(0xffb9c3ff),
        Color(0xff62d3ff), Color(0xff44d9f1), Color(0xff52dbc9), Color(0xff78dd77),
        Color(0xff9fd75c), Color(0xffc1d02d), Color(0xfffabd00), Color(0xffffb86e),
        Color.White
    )
    val zeroCorner = remember { CornerSize(0) }

    Column(
        modifier
            .clip(
                when (index) {
                    0 -> topListItemShape
                    items - 1 -> bottomListItemShape
                    else -> middleListItemShape
                }
            )
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ListItem(
                leadingContent = {
                    Icon(
                        painterResource(R.drawable.colors),
                        null
                    )
                },
                headlineContent = { Text(stringResource(R.string.dynamic_color)) },
                supportingContent = { Text(stringResource(R.string.dynamic_color_desc)) },
                trailingContent = {
                    val checked = color == colorSchemes.last()
                    Switch(
                        checked = checked,
                        onCheckedChange = {
                            if (it) onColorChange(colorSchemes.last())
                            else onColorChange(colorSchemes.first())
                        },
                        enabled = isPlus,
                        thumbContent = {
                            if (checked) {
                                Icon(
                                    painter = painterResource(R.drawable.check),
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize),
                                )
                            } else {
                                Icon(
                                    painter = painterResource(R.drawable.clear),
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize),
                                )
                            }
                        },
                        colors = switchColors
                    )
                },
                colors = listItemColors,
                modifier = Modifier.clip(middleListItemShape)
            )
            Spacer(Modifier.height(2.dp))
        }

        ListItem(
            leadingContent = {
                Icon(
                    painter = painterResource(R.drawable.palette),
                    contentDescription = null,
                    tint = colorScheme.primary
                )
            },
            headlineContent = { Text(stringResource(R.string.color_scheme)) },
            supportingContent = {
                Text(
                    if (color == Color.White) stringResource(R.string.dynamic)
                    else stringResource(R.string.color)
                )
            },
            colors = listItemColors,
            modifier = Modifier.clip(
                RoundedCornerShape(
                    topStart = middleListItemShape.topStart,
                    topEnd = middleListItemShape.topEnd,
                    zeroCorner,
                    zeroCorner
                )
            )
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 48.dp),
            userScrollEnabled = isPlus,
            modifier = Modifier
                .background(listItemColors.containerColor)
                .padding(bottom = 8.dp)
        ) {
            items(colorSchemes.dropLast(1)) {
                ColorPickerButton(
                    color = it,
                    isSelected = it == color,
                    enabled = isPlus,
                    modifier = Modifier.padding(4.dp)
                ) {
                    onColorChange(it)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ColorPickerButton(
    color: Color,
    isSelected: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    IconButton(
        shapes = IconButtonDefaults.shapes(),
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = color,
            disabledContainerColor = color.copy(0.3f)
        ),
        enabled = enabled,
        modifier = modifier.size(48.dp),
        onClick = onClick
    ) {
        AnimatedContent(isSelected) { isSelected ->
            when (isSelected) {
                true -> Icon(
                    painterResource(R.drawable.check),
                    tint = Color.Black,
                    contentDescription = null
                )

                else ->
                    if (color == Color.White) Icon(
                        painterResource(R.drawable.colors),
                        tint = Color.Black,
                        contentDescription = null
                    )
            }
        }
    }
}
