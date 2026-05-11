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
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import kotlin.math.roundToInt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.nsh07.pomodoro.ui.settingsScreen.viewModel.WidgetConfigurationViewModel
import org.nsh07.pomodoro.ui.settingsScreen.viewModel.WidgetType
import org.nsh07.pomodoro.ui.theme.CustomColors.detailPaneTopBarColors
import org.nsh07.pomodoro.ui.theme.CustomColors.listItemColors
import org.nsh07.pomodoro.ui.theme.CustomColors.topBarColors
import org.nsh07.pomodoro.ui.theme.LocalAppFonts
import org.nsh07.pomodoro.ui.theme.TomatoShapeDefaults.PANE_MAX_WIDTH
import org.nsh07.pomodoro.ui.theme.TomatoShapeDefaults.bottomListItemShape
import org.nsh07.pomodoro.ui.theme.TomatoShapeDefaults.cardShape
import org.nsh07.pomodoro.ui.theme.TomatoShapeDefaults.middleListItemShape
import org.nsh07.pomodoro.ui.theme.TomatoShapeDefaults.topListItemShape
import org.nsh07.pomodoro.ui.topBarWindowInsets
import tomato.shared.generated.resources.Res
import tomato.shared.generated.resources.background_role
import tomato.shared.generated.resources.check
import tomato.shared.generated.resources.clear
import tomato.shared.generated.resources.clocks
import tomato.shared.generated.resources.opacity
import tomato.shared.generated.resources.palette
import tomato.shared.generated.resources.refresh
import tomato.shared.generated.resources.restart
import tomato.shared.generated.resources.widgets

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WidgetConfigurationScreen(
    viewModel: WidgetConfigurationViewModel,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    @Suppress("DEPRECATION")
    val widthExpanded = currentWindowAdaptiveInfo()
        .windowSizeClass
        .isWidthAtLeastBreakpoint(WIDTH_DP_EXPANDED_LOWER_BOUND)

    val barColors = if (widthExpanded) detailPaneTopBarColors
    else topBarColors

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
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
                        Text("Configure ${state.widgetType.name.lowercase().replaceFirstChar { it.uppercase() }} Instance")
                    },
                    colors = barColors.copy(containerColor = barColors.containerColor.copy(alpha = 0.8f)),
                    scrollBehavior = scrollBehavior
                )
            },
            bottomBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = onDone,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Done")
                    }
                }
            },
            containerColor = Color.Transparent,
            modifier = modifier
                .widthIn(max = PANE_MAX_WIDTH)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        ) { innerPadding ->
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                contentPadding = innerPadding,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    Spacer(Modifier.height(14.dp))
                }

                item {
                    WidgetPreviewCard(
                        opacity = state.opacity,
                        backgroundRole = state.backgroundRole,
                        foregroundRole = state.foregroundRole,
                        headerRole = state.headerRole,
                        skipButtonRole = state.skipButtonRole,
                        onSkipButtonRole = state.onSkipButtonRole,
                        barCornerRadius = state.barCornerRadius,
                        widgetType = state.widgetType
                    )
                }

                item {
                    Spacer(Modifier.height(12.dp))
                }

                item {
                    Column(Modifier.background(listItemColors.containerColor.copy(alpha = 0.9f), topListItemShape)) {
                        ListItem(
                            leadingContent = {
                                Icon(painterResource(Res.drawable.clear), null)
                            },
                            headlineContent = {
                                Text(stringResource(Res.string.opacity))
                            },
                            supportingContent = {
                                Text("${(state.opacity * 100).toInt()}%")
                            },
                            colors = listItemColors.copy(
                                containerColor = Color.Transparent,
                                selectedContainerColor = Color.Transparent,
                                draggedContainerColor = Color.Transparent,
                            ),
                            modifier = Modifier.clip(cardShape)
                        )
                        Slider(
                            value = state.opacity,
                            onValueChange = { viewModel.updateOpacityInstant(it) },
                            modifier = Modifier
                                .padding(start = (16 * 2 + 24).dp, end = 16.dp, bottom = 12.dp)
                        )
                    }
                }

                item {
                    Column(Modifier.background(listItemColors.containerColor.copy(alpha = 0.9f), middleListItemShape)) {
                        ListItem(
                            leadingContent = {
                                Icon(painterResource(Res.drawable.palette), null)
                            },
                            headlineContent = {
                                Text(stringResource(Res.string.background_role))
                            },
                            supportingContent = {
                                Text(state.backgroundRole)
                            },
                            colors = listItemColors.copy(
                                containerColor = Color.Transparent,
                                selectedContainerColor = Color.Transparent,
                                draggedContainerColor = Color.Transparent,
                            ),
                            modifier = Modifier.clip(cardShape)
                        )
                        RoleDotsList(
                            selectedRole = state.backgroundRole,
                            onRoleSelected = { viewModel.setBackgroundRole(it) },
                            modifier = Modifier.padding(start = (16 * 2 + 24).dp, end = 16.dp, bottom = 12.dp)
                        )
                    }
                }

                item {
                    Column(Modifier.background(listItemColors.containerColor.copy(alpha = 0.9f), middleListItemShape)) {
                        ListItem(
                            leadingContent = {
                                Icon(painterResource(Res.drawable.palette), null)
                            },
                            headlineContent = {
                                Text("Foreground Role")
                            },
                            supportingContent = {
                                Text(state.foregroundRole)
                            },
                            colors = listItemColors.copy(
                                containerColor = Color.Transparent,
                                selectedContainerColor = Color.Transparent,
                                draggedContainerColor = Color.Transparent,
                            ),
                            modifier = Modifier.clip(cardShape)
                        )
                        RoleDotsList(
                            selectedRole = state.foregroundRole,
                            onRoleSelected = { viewModel.setForegroundRole(it) },
                            modifier = Modifier.padding(start = (16 * 2 + 24).dp, end = 16.dp, bottom = 12.dp)
                        )
                    }
                }

                item {
                    Column(
                        Modifier.background(
                            listItemColors.containerColor.copy(alpha = 0.9f),
                            if (state.widgetType == WidgetType.TIMER || state.widgetType == WidgetType.HISTORY)
                                middleListItemShape else bottomListItemShape
                        )
                    ) {
                        ListItem(
                            leadingContent = {
                                Icon(painterResource(Res.drawable.palette), null)
                            },
                            headlineContent = {
                                Text(if (state.widgetType == WidgetType.TIMER) "Start Button Foreground" else "Header Role")
                            },
                            supportingContent = {
                                Text(state.headerRole)
                            },
                            colors = listItemColors.copy(
                                containerColor = Color.Transparent,
                                selectedContainerColor = Color.Transparent,
                                draggedContainerColor = Color.Transparent,
                            ),
                            modifier = Modifier.clip(cardShape)
                        )
                        RoleDotsList(
                            selectedRole = state.headerRole,
                            onRoleSelected = { viewModel.setHeaderRole(it) },
                            modifier = Modifier.padding(start = (16 * 2 + 24).dp, end = 16.dp, bottom = 12.dp)
                        )
                    }
                }

                if (state.widgetType == WidgetType.TIMER) {
                    item {
                        Column(Modifier.background(listItemColors.containerColor.copy(alpha = 0.9f), middleListItemShape)) {
                            ListItem(
                                leadingContent = {
                                    Icon(painterResource(Res.drawable.palette), null)
                                },
                                headlineContent = {
                                    Text("Skip Button Background")
                                },
                                supportingContent = {
                                    Text(state.skipButtonRole)
                                },
                                colors = listItemColors.copy(
                                    containerColor = Color.Transparent,
                                    selectedContainerColor = Color.Transparent,
                                    draggedContainerColor = Color.Transparent,
                                ),
                                modifier = Modifier.clip(cardShape)
                            )
                            RoleDotsList(
                                selectedRole = state.skipButtonRole,
                                onRoleSelected = { viewModel.setSkipButtonRole(it) },
                                modifier = Modifier.padding(start = (16 * 2 + 24).dp, end = 16.dp, bottom = 12.dp)
                            )
                        }
                    }

                    item {
                        Column(Modifier.background(listItemColors.containerColor.copy(alpha = 0.9f), bottomListItemShape)) {
                            ListItem(
                                leadingContent = {
                                    Icon(painterResource(Res.drawable.palette), null)
                                },
                                headlineContent = {
                                    Text("Skip Button Foreground")
                                },
                                supportingContent = {
                                    Text(state.onSkipButtonRole)
                                },
                                colors = listItemColors.copy(
                                    containerColor = Color.Transparent,
                                    selectedContainerColor = Color.Transparent,
                                    draggedContainerColor = Color.Transparent,
                                ),
                                modifier = Modifier.clip(cardShape)
                            )
                            RoleDotsList(
                                selectedRole = state.onSkipButtonRole,
                                onRoleSelected = { viewModel.setOnSkipButtonRole(it) },
                                modifier = Modifier.padding(start = (16 * 2 + 24).dp, end = 16.dp, bottom = 12.dp)
                            )
                        }
                    }
                }

                if (state.widgetType == WidgetType.HISTORY) {
                    item {
                        Column(Modifier.background(listItemColors.containerColor.copy(alpha = 0.9f), bottomListItemShape)) {
                            ListItem(
                                leadingContent = {
                                    Icon(painterResource(Res.drawable.restart), null)
                                },
                                headlineContent = {
                                    Text("Bar Corner Radius")
                                },
                                supportingContent = {
                                    Text("${state.barCornerRadius}dp")
                                },
                                colors = listItemColors.copy(
                                    containerColor = Color.Transparent,
                                    selectedContainerColor = Color.Transparent,
                                    draggedContainerColor = Color.Transparent,
                                ),
                                modifier = Modifier.clip(cardShape)
                            )
                            Slider(
                                value = state.barCornerRadius.toFloat(),
                                onValueChange = { viewModel.setBarCornerRadius(it.roundToInt()) },
                                valueRange = 0f..16f,
                                steps = 15,
                                modifier = Modifier
                                    .padding(start = (16 * 2 + 24).dp, end = 16.dp, bottom = 12.dp)
                            )
                        }
                    }
                }

                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun WidgetPreviewCard(
    opacity: Float,
    backgroundRole: String,
    foregroundRole: String,
    headerRole: String,
    skipButtonRole: String,
    onSkipButtonRole: String,
    barCornerRadius: Int,
    widgetType: WidgetType,
    modifier: Modifier = Modifier
) {
    val backgroundColor = getRoleColor(backgroundRole)
    val foregroundColor = getRoleColor(foregroundRole)
    val headerColor = getRoleColor(headerRole)
    val skipButtonColor = getRoleColor(skipButtonRole)
    val onSkipButtonColor = getRoleColor(onSkipButtonRole)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        when (widgetType) {
            WidgetType.TIMER -> {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .background(backgroundColor.copy(alpha = opacity), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    GlancePreviewText("25:00", foregroundColor)

                    // Skip / restart button group (top-end) — independent colors
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopEnd) {
                        Box(Modifier.padding(8.dp).size(32.dp).background(skipButtonColor, CircleShape), contentAlignment = Alignment.Center) {
                            Icon(painterResource(Res.drawable.clear), null, tint = onSkipButtonColor, modifier = Modifier.size(16.dp))
                        }
                    }
                    // Play / pause button (bottom-start) — foregroundRole bg, headerRole fg
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomStart) {
                        Box(Modifier.padding(8.dp).size(40.dp).background(foregroundColor, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                            Icon(painterResource(Res.drawable.restart), null, tint = headerColor, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
            WidgetType.TODAY -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(backgroundColor.copy(alpha = opacity), RoundedCornerShape(24.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Today", style = typography.labelSmall, color = headerColor, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.weight(1f))
                            Icon(painterResource(Res.drawable.refresh), null, tint = headerColor, modifier = Modifier.size(16.dp))
                        }
                        Text("02:45", style = typography.headlineMedium, color = foregroundColor, fontFamily = LocalAppFonts.current.topBarTitle)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            repeat(4) { i ->
                                Box(Modifier.weight(1f).height(8.dp).background(foregroundColor.copy(alpha = 0.5f + i * 0.1f), RoundedCornerShape(2.dp)))
                            }
                        }
                    }
                }
            }
            WidgetType.HISTORY -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(backgroundColor.copy(alpha = opacity), RoundedCornerShape(24.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(painterResource(Res.drawable.clocks), null, tint = headerColor, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Focus History", style = typography.labelSmall, color = headerColor, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.weight(1f))
                            Icon(painterResource(Res.drawable.refresh), null, tint = headerColor, modifier = Modifier.size(16.dp))
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("04:20", style = typography.headlineSmall, color = headerColor, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.Bottom, modifier = Modifier.fillMaxWidth()) {
                            repeat(15) { i ->
                                Box(Modifier.weight(1f).height((10 + (i % 5) * 10).dp).background(foregroundColor, RoundedCornerShape(barCornerRadius.dp)))
                            }
                        }
                    }
                }
            }
            else -> {
                Text("Select a widget type", color = Color.White)
            }
        }
    }
}

@Composable
fun GlancePreviewText(text: String, color: Color) {
    Text(
        text = text,
        style = typography.displaySmall,
        fontFamily = LocalAppFonts.current.topBarTitle,
        color = color
    )
}

@Composable
fun getRoleColor(role: String): Color {
    return when (role) {
        "black" -> Color(0xFF000000)
        "primary" -> colorScheme.primary
        "onPrimary" -> colorScheme.onPrimary
        "primaryContainer" -> colorScheme.primaryContainer
        "onPrimaryContainer" -> colorScheme.onPrimaryContainer
        "secondary" -> colorScheme.secondary
        "onSecondary" -> colorScheme.onSecondary
        "secondaryContainer" -> colorScheme.secondaryContainer
        "onSecondaryContainer" -> colorScheme.onSecondaryContainer
        "tertiary" -> colorScheme.tertiary
        "onTertiary" -> colorScheme.onTertiary
        "tertiaryContainer" -> colorScheme.tertiaryContainer
        "onTertiaryContainer" -> colorScheme.onTertiaryContainer
        "error" -> colorScheme.error
        "onError" -> colorScheme.onError
        "errorContainer" -> colorScheme.errorContainer
        "onErrorContainer" -> colorScheme.onErrorContainer
        "surface" -> colorScheme.surface
        "onSurface" -> colorScheme.onSurface
        "surfaceVariant" -> colorScheme.surfaceVariant
        "onSurfaceVariant" -> colorScheme.onSurfaceVariant
        "outline" -> colorScheme.outline
        "background" -> colorScheme.background
        "onBackground" -> colorScheme.onBackground
        "inverseSurface" -> colorScheme.inverseSurface
        "inverseOnSurface" -> colorScheme.inverseOnSurface
        "inversePrimary" -> colorScheme.inversePrimary
        "white" -> Color.White
        else -> colorScheme.surface
    }
}

@Composable
fun RoleDotsList(
    selectedRole: String,
    onRoleSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val roles = listOf(
        "black", "white",
        "primary", "onPrimary", "primaryContainer", "onPrimaryContainer",
        "secondary", "onSecondary", "secondaryContainer", "onSecondaryContainer",
        "tertiary", "onTertiary", "tertiaryContainer", "onTertiaryContainer",
        "error", "onError", "errorContainer", "onErrorContainer",
        "surface", "onSurface", "surfaceVariant", "onSurfaceVariant",
        "background", "onBackground",
        "outline",
        "inverseSurface", "inverseOnSurface", "inversePrimary"
    )

    LazyRow(
        contentPadding = PaddingValues(end = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        items(roles) { role ->
            val color = getRoleColor(role)
            val isSelected = role == selectedRole
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onRoleSelected(role) }
            ) {
                if (isSelected) {
                    Icon(
                        painterResource(Res.drawable.check),
                        contentDescription = null,
                        tint = if (role in listOf("surface", "surfaceVariant", "background", "white",
                            "primaryContainer", "secondaryContainer", "tertiaryContainer", "errorContainer",
                            "onPrimary", "onSecondary", "onTertiary", "onError",
                            "inverseSurface", "inverseOnSurface", "inversePrimary"
                        )) colorScheme.primary else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
