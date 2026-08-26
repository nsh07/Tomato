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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.ToggleButtonShapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.nsh07.pomodoro.ui.LocalIsPlus
import org.nsh07.pomodoro.ui.performSegmentTick
import org.nsh07.pomodoro.ui.performToggle
import org.nsh07.pomodoro.ui.theme.CustomColors.listItemColors
import org.nsh07.pomodoro.ui.theme.CustomColors.switchColors
import org.nsh07.pomodoro.ui.theme.SeededTheme
import org.nsh07.pomodoro.ui.theme.TomatoShapeDefaults.segmentedListItemShapes
import org.nsh07.pomodoro.ui.theme.TomatoTheme
import org.nsh07.pomodoro.utils.androidSdkVersionAtLeast
import tomato.shared.generated.resources.Res
import tomato.shared.generated.resources.blue
import tomato.shared.generated.resources.chartreuse
import tomato.shared.generated.resources.check
import tomato.shared.generated.resources.clear
import tomato.shared.generated.resources.color
import tomato.shared.generated.resources.color_scheme
import tomato.shared.generated.resources.colors
import tomato.shared.generated.resources.cyan
import tomato.shared.generated.resources.default_name
import tomato.shared.generated.resources.dynamic
import tomato.shared.generated.resources.dynamic_color
import tomato.shared.generated.resources.dynamic_color_desc
import tomato.shared.generated.resources.green
import tomato.shared.generated.resources.indigo
import tomato.shared.generated.resources.orange
import tomato.shared.generated.resources.palette
import tomato.shared.generated.resources.purple
import tomato.shared.generated.resources.red
import tomato.shared.generated.resources.rose
import tomato.shared.generated.resources.teal
import tomato.shared.generated.resources.yellow

@Immutable
data class ColorSchemeItem(
    val color: Color,
    val name: StringResource
)

val colorSchemes = listOf(
    ColorSchemeItem(Color(0xffffa79b), Res.string.red),
    ColorSchemeItem(Color(0xffffb2bd), Res.string.rose),
    ColorSchemeItem(Color(0xffd7bbfc), Res.string.purple),
    ColorSchemeItem(Color(0xffc5c0ff), Res.string.indigo),
    ColorSchemeItem(Color(0xffb0c6ff), Res.string.blue),
    ColorSchemeItem(Color(0xff86d1ea), Res.string.cyan),
    ColorSchemeItem(Color(0xff82d5c7), Res.string.teal),
    ColorSchemeItem(Color(0xff9cd59f), Res.string.green),
    ColorSchemeItem(Color(0xffc3cd7c), Res.string.chartreuse),
    ColorSchemeItem(Color(0xffe8c16c), Res.string.yellow),
    ColorSchemeItem(Color(0xffffb68d), Res.string.orange)
)

private val dynamicColorLabel: StringResource
    get() = if (androidSdkVersionAtLeast(31)) Res.string.dynamic else Res.string.default_name

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ColorSchemePickerListItem(
    color: Color,
    items: Int,
    index: Int,
    onColorChange: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    val isPlus = LocalIsPlus.current
    val haptic = LocalHapticFeedback.current

    if (androidSdkVersionAtLeast(31)) {
        val checked = color == Color.White
        SegmentedListItem(
            onClick = {
                haptic.performToggle(!checked)
                if (!checked) onColorChange(Color.White)
                else onColorChange(colorSchemes.first().color)
            },
            leadingContent = { Icon(painterResource(Res.drawable.colors), null) },
            content = { Text(stringResource(Res.string.dynamic_color)) },
            supportingContent = { Text(stringResource(Res.string.dynamic_color_desc)) },
            trailingContent = {
                Switch(
                    checked = checked,
                    onCheckedChange = {
                        haptic.performToggle(it)
                        if (it) onColorChange(Color.White)
                        else onColorChange(colorSchemes.first().color)
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
                    if (color == Color.White) stringResource(dynamicColorLabel)
                    else stringResource(Res.string.color)
                )
            },
            colors = listItemColors,
            enabled = isPlus,
            shapes = ListItemDefaults.segmentedShapes(
                1,
                3,
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

    ColorPickerRow(
        color = color,
        onColorChange = onColorChange,
        modifier = modifier
    )
}

@Composable
fun ColorPickerRow(
    color: Color,
    onColorChange: (Color) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = LocalIsPlus.current,
    backgroundColor: Color = animateColorAsState(listItemColors.containerColor).value,
    horizontalPadding: Dp = 48.dp
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        contentPadding = PaddingValues(horizontal = horizontalPadding),
        userScrollEnabled = enabled,
        modifier = modifier
            .background(
                backgroundColor,
                shape = shapes.extraSmall.copy(topStart = CornerSize(0), topEnd = CornerSize(0))
            )
            .padding(bottom = 8.dp)
    ) {
        item {
            DynamicColorPickerButton(
                shapes = ButtonGroupDefaults.connectedLeadingButtonShapes(),
                checked = color == Color.White,
                enabled = enabled,
                onClick = { onColorChange(Color.White) }
            )
        }
        itemsIndexed(colorSchemes) { index, it ->
            ColorPickerButton(
                items = colorSchemes.size + 1,
                index = index + 1,
                color = it.color,
                colorName = it.name,
                checked = it.color == color,
                enabled = enabled
            ) {
                onColorChange(it.color)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ColorPickerButton(
    items: Int,
    index: Int,
    color: Color,
    colorName: StringResource,
    checked: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
        SeededTheme(color) {
            ToggleButton(
                shapes = when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    items - 1 -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                },
                colors = ToggleButtonDefaults.toggleButtonColors(
                    containerColor = colorScheme.primaryContainer,
                    checkedContainerColor = colorScheme.primary,
                    checkedContentColor = colorScheme.onPrimary
                ),
                enabled = enabled,
                modifier = modifier
                    .height(40.dp)
                    .widthIn(min = 40.dp),
                checked = checked,
                onCheckedChange = {
                    if (!checked) haptic.performSegmentTick()
                    onClick()
                }
            ) {
                AnimatedContent(checked) { checked ->
                    if (checked) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(
                                painterResource(Res.drawable.check),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                stringResource(colorName),
                                style = typography.labelLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DynamicColorPickerButton(
    shapes: ToggleButtonShapes,
    checked: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    ToggleButton(
        shapes = shapes,
        colors = ToggleButtonDefaults.toggleButtonColors(
            containerColor = colorScheme.secondaryContainer,
            contentColor = colorScheme.onSecondaryContainer,
            checkedContainerColor = colorScheme.primary,
            checkedContentColor = colorScheme.onPrimary
        ),
        enabled = enabled,
        modifier = modifier
            .height(40.dp)
            .widthIn(min = 40.dp),
        checked = checked,
        onCheckedChange = {
            if (!checked) haptic.performSegmentTick()
            onClick()
        }
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AnimatedContent(checked) { checked ->
                Icon(
                    if (checked) painterResource(Res.drawable.check)
                    else painterResource(Res.drawable.colors),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                stringResource(dynamicColorLabel),
                style = typography.labelLarge
            )
        }
    }
}

@Preview
@Composable
private fun ColorPickerButtonPreview() {
    var selectedIndex by remember { mutableIntStateOf(0) }

    TomatoTheme {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier
                .background(colorScheme.inverseSurface)
        ) {
            items(colorSchemes.size) { index ->
                val (color, name) = colorSchemes[index]
                ColorPickerButton(
                    items = colorSchemes.size,
                    index = index,
                    color = color,
                    colorName = name,
                    checked = index == selectedIndex,
                    enabled = true,
                    onClick = { selectedIndex = index }
                )
            }
        }
    }
}
