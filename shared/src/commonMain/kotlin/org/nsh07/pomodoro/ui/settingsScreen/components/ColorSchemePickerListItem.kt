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

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.nsh07.pomodoro.ui.theme.CustomColors.listItemColors
import org.nsh07.pomodoro.ui.theme.CustomColors.switchColors
import org.nsh07.pomodoro.ui.theme.TomatoShapeDefaults.segmentedListItemShapes
import org.nsh07.pomodoro.utils.androidSdkVersionAtLeast
import tomato.shared.generated.resources.Res
import tomato.shared.generated.resources.check
import tomato.shared.generated.resources.clear
import tomato.shared.generated.resources.color
import tomato.shared.generated.resources.color_scheme
import tomato.shared.generated.resources.colors
import tomato.shared.generated.resources.dynamic
import tomato.shared.generated.resources.dynamic_color
import tomato.shared.generated.resources.dynamic_color_desc
import tomato.shared.generated.resources.palette

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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

    if (androidSdkVersionAtLeast(31)) {
        val checked = color == colorSchemes.last()
        SegmentedListItem(
            onClick = {
                if (!checked) onColorChange(colorSchemes.last())
                else onColorChange(colorSchemes.first())
            },
            leadingContent = { Icon(painterResource(Res.drawable.colors), null) },
            content = { Text(stringResource(Res.string.dynamic_color)) },
            supportingContent = { Text(stringResource(Res.string.dynamic_color_desc)) },
            trailingContent = {
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
                                painter = painterResource(Res.drawable.check),
                                contentDescription = null,
                                modifier = Modifier.size(SwitchDefaults.IconSize),
                            )
                        } else {
                            Icon(
                                painter = painterResource(Res.drawable.clear),
                                contentDescription = null,
                                modifier = Modifier.size(SwitchDefaults.IconSize),
                            )
                        }
                    },
                    colors = switchColors
                )
            },
            colors = listItemColors,
            enabled = isPlus,
            shapes = segmentedListItemShapes(index, items),
            modifier = modifier
        )
        Spacer(Modifier.height(2.dp))
    }

    Box {
        SegmentedListItem(
            onClick = {},
            leadingContent = {
                Icon(
                    painter = painterResource(Res.drawable.palette),
                    contentDescription = null
                )
            },
            content = { Text(stringResource(Res.string.color_scheme)) },
            supportingContent = {
                Text(
                    if (color == Color.White) stringResource(Res.string.dynamic)
                    else stringResource(Res.string.color)
                )
            },
            colors = listItemColors,
            enabled = isPlus,
            shapes = ListItemDefaults.segmentedShapes(
                1,
                items,
                ListItemDefaults.shapes(
                    shape = shapes.extraSmall.copy(
                        bottomStart = CornerSize(0),
                        bottomEnd = CornerSize(0)
                    )
                )
            ),
            modifier = modifier
        )

        Box( // TODO: Workaround to disable clickable behavior of SegmentedListItem. Remove once an overload is implemented
            Modifier
                .matchParentSize()
                .clickable(false) {}
        )
    }

    LazyRow(
        contentPadding = PaddingValues(horizontal = 48.dp),
        userScrollEnabled = isPlus,
        modifier = modifier
            .background(
                animateColorAsState(listItemColors.containerColor).value,
                shape = shapes.extraSmall.copy(topStart = CornerSize(0), topEnd = CornerSize(0))
            )
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
                    painterResource(Res.drawable.check),
                    tint = Color.Black,
                    contentDescription = null
                )

                else ->
                    if (color == Color.White) Icon(
                        painterResource(Res.drawable.colors),
                        tint = Color.Black,
                        contentDescription = null
                    )
            }
        }
    }
}
