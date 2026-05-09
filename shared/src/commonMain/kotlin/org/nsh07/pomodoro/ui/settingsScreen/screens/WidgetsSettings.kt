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

package org.nsh07.pomodoro.ui.settingsScreen.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.nsh07.pomodoro.ui.mergePaddingValues
import org.nsh07.pomodoro.ui.settingsScreen.components.PlusDivider
import org.nsh07.pomodoro.ui.settingsScreen.viewModel.SettingsAction
import org.nsh07.pomodoro.ui.settingsScreen.viewModel.SettingsState
import org.nsh07.pomodoro.ui.theme.CustomColors.detailPaneTopBarColors
import org.nsh07.pomodoro.ui.theme.CustomColors.listItemColors
import org.nsh07.pomodoro.ui.theme.CustomColors.topBarColors
import org.nsh07.pomodoro.ui.theme.LocalAppFonts
import org.nsh07.pomodoro.ui.theme.TomatoShapeDefaults.PANE_MAX_WIDTH
import org.nsh07.pomodoro.ui.theme.TomatoShapeDefaults.bottomListItemShape
import org.nsh07.pomodoro.ui.theme.TomatoShapeDefaults.cardShape
import org.nsh07.pomodoro.ui.theme.TomatoShapeDefaults.topListItemShape
import org.nsh07.pomodoro.ui.topBarWindowInsets
import tomato.shared.generated.resources.Res
import tomato.shared.generated.resources.arrow_back
import tomato.shared.generated.resources.back
import tomato.shared.generated.resources.background_role
import tomato.shared.generated.resources.check
import tomato.shared.generated.resources.clear
import tomato.shared.generated.resources.opacity
import tomato.shared.generated.resources.palette
import tomato.shared.generated.resources.settings
import tomato.shared.generated.resources.widgets

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WidgetsSettings(
    settingsState: SettingsState,
    contentPadding: PaddingValues,
    isPlus: Boolean,
    opacitySliderState: SliderState,
    onAction: (SettingsAction) -> Unit,
    setShowPaywall: (Boolean) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val widthExpanded = currentWindowAdaptiveInfo()
        .windowSizeClass
        .isWidthAtLeastBreakpoint(WIDTH_DP_EXPANDED_LOWER_BOUND)

    val barColors = if (widthExpanded) detailPaneTopBarColors
    else topBarColors

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(barColors.containerColor)
    ) {
        Scaffold(
            topBar = {
                LargeFlexibleTopAppBar(
                    windowInsets = topBarWindowInsets(),
                    title = {
                        Text(
                            stringResource(Res.string.widgets),
                            fontFamily = LocalAppFonts.current.topBarTitle
                        )
                    },
                    subtitle = {
                        Text(stringResource(Res.string.settings))
                    },
                    navigationIcon = {
                        if (!widthExpanded)
                            FilledTonalIconButton(
                                onClick = onBack,
                                shapes = IconButtonDefaults.shapes(),
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = listItemColors.containerColor
                                )
                            ) {
                                Icon(
                                    painterResource(Res.drawable.arrow_back),
                                    stringResource(Res.string.back)
                                )
                            }
                    },
                    colors = barColors,
                    scrollBehavior = scrollBehavior
                )
            },
            containerColor = barColors.containerColor,
            modifier = modifier
                .widthIn(max = PANE_MAX_WIDTH)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        ) { innerPadding ->
            val insets = mergePaddingValues(innerPadding, contentPadding)
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                contentPadding = insets,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    Spacer(Modifier.height(14.dp))
                }

                item {
                    WidgetPreviewCard(
                        opacity = settingsState.widgetOpacity,
                        backgroundRole = settingsState.widgetBackgroundRole
                    )
                }

                item {
                    Spacer(Modifier.height(12.dp))
                }

                item {
                    Column(Modifier.background(listItemColors.containerColor, topListItemShape)) {
                        ListItem(
                            leadingContent = {
                                Icon(painterResource(Res.drawable.clear), null)
                            },
                            headlineContent = {
                                Text(stringResource(Res.string.opacity))
                            },
                            supportingContent = {
                                Text("${(opacitySliderState.value * 100).toInt()}%")
                            },
                            colors = listItemColors,
                            modifier = Modifier.clip(cardShape)
                        )
                        Slider(
                            state = opacitySliderState,
                            enabled = isPlus,
                            modifier = Modifier
                                .padding(start = (16 * 2 + 24).dp, end = 16.dp, bottom = 12.dp)
                        )
                    }
                }

                if (!isPlus) {
                    item { PlusDivider(setShowPaywall) }
                }

                item {
                    Column(Modifier.background(listItemColors.containerColor, bottomListItemShape)) {
                        ListItem(
                            leadingContent = {
                                Icon(painterResource(Res.drawable.palette), null)
                            },
                            headlineContent = {
                                Text(stringResource(Res.string.background_role))
                            },
                            supportingContent = {
                                Text(settingsState.widgetBackgroundRole)
                            },
                            colors = listItemColors,
                            modifier = Modifier.clip(cardShape)
                        )
                        RoleDotsList(
                            selectedRole = settingsState.widgetBackgroundRole,
                            enabled = isPlus,
                            onRoleSelected = { onAction(SettingsAction.SaveWidgetBackgroundRole(it)) },
                            modifier = Modifier.padding(start = (16 * 2 + 24).dp, end = 16.dp, bottom = 12.dp)
                        )
                    }
                }

                item { Spacer(Modifier.height(12.dp)) }
            }
        }
    }
}

@Composable
fun WidgetPreviewCard(
    opacity: Float,
    backgroundRole: String,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (backgroundRole) {
        "surface" -> colorScheme.surface
        "surfaceVariant" -> colorScheme.surfaceVariant
        "primaryContainer" -> colorScheme.primaryContainer
        "secondaryContainer" -> colorScheme.secondaryContainer
        "tertiaryContainer" -> colorScheme.tertiaryContainer
        else -> colorScheme.surface
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .background(colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(backgroundColor.copy(alpha = opacity), RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "25:00",
                style = typography.headlineMedium,
                fontFamily = LocalAppFonts.current.topBarTitle,
                color = if (opacity < 0.5f && backgroundColor == colorScheme.surface) colorScheme.onSurfaceVariant else colorScheme.onSurface
            )
        }
    }
}

@Composable
fun RoleDotsList(
    selectedRole: String,
    enabled: Boolean,
    onRoleSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val roles = listOf(
        "surface" to colorScheme.surface,
        "surfaceVariant" to colorScheme.surfaceVariant,
        "primaryContainer" to colorScheme.primaryContainer,
        "secondaryContainer" to colorScheme.secondaryContainer,
        "tertiaryContainer" to colorScheme.tertiaryContainer,
        "accent2_800" to Color(0xFF3B4D3C) // Approximation for dark theme accent2_800
    )

    LazyRow(
        contentPadding = PaddingValues(end = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        items(roles) { (role, color) ->
            val isSelected = role == selectedRole
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(animateColorAsState(if (enabled) color else color.copy(alpha = 0.3f)).value)
                    .clickable(enabled = enabled) { onRoleSelected(role) }
            ) {
                if (isSelected) {
                    Icon(
                        painterResource(Res.drawable.check),
                        contentDescription = null,
                        tint = if (role == "surface" || role == "surfaceVariant") colorScheme.primary else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
