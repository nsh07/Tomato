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

package org.nsh07.pomodoro.widget.config

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import org.nsh07.pomodoro.R
import org.nsh07.pomodoro.ui.theme.CustomColors.listItemColors
import org.nsh07.pomodoro.ui.theme.LocalAppFonts
import org.nsh07.pomodoro.ui.theme.TomatoShapeDefaults.PANE_MAX_WIDTH
import org.nsh07.pomodoro.ui.theme.TomatoShapeDefaults.bottomListItemShape
import org.nsh07.pomodoro.ui.theme.TomatoShapeDefaults.cardShape
import org.nsh07.pomodoro.ui.theme.TomatoShapeDefaults.middleListItemShape
import org.nsh07.pomodoro.ui.theme.TomatoShapeDefaults.topListItemShape
import org.nsh07.pomodoro.ui.topBarWindowInsets
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WidgetConfigurationScreen(
    isPlus: Boolean,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WidgetConfigurationViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

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
                            stringResource(R.string.widgets),
                            fontFamily = LocalAppFonts.current.topBarTitle
                        )
                    },
                    subtitle = {
                        Text(
                            "Configure ${
                                state.widgetType.name.lowercase()
                                    .replaceFirstChar { it.uppercase() }
                            } Instance"
                        )
                    },
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
//                        backgroundRole = state.backgroundRole,
//                        foregroundRole = state.foregroundRole,
//                        headerRole = state.headerRole,
//                        skipButtonRole = state.skipButtonRole,
//                        onSkipButtonRole = state.onSkipButtonRole,
//                        barCornerRadius = state.barCornerRadius,
                        widgetType = state.widgetType
                    )
                }

                item {
                    Spacer(Modifier.height(12.dp))
                }

                item {
                    Column(
                        Modifier.background(
                            listItemColors.containerColor.copy(alpha = 0.9f),
                            topListItemShape
                        )
                    ) {
                        ListItem(
                            leadingContent = {
                                Icon(painterResource(R.drawable.clear), null)
                            },
                            headlineContent = {
                                Text(stringResource(R.string.opacity))
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
                    Column(
                        Modifier.background(
                            listItemColors.containerColor.copy(alpha = 0.9f),
                            middleListItemShape
                        )
                    ) {
                        ListItem(
                            leadingContent = {
                                Icon(painterResource(R.drawable.palette), null)
                            },
                            headlineContent = {
                                Text(stringResource(R.string.background_role))
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
                            modifier = Modifier.padding(
                                start = (16 * 2 + 24).dp,
                                end = 16.dp,
                                bottom = 12.dp
                            )
                        )
                    }
                }

                item {
                    Column(
                        Modifier.background(
                            listItemColors.containerColor.copy(alpha = 0.9f),
                            middleListItemShape
                        )
                    ) {
                        ListItem(
                            leadingContent = {
                                Icon(painterResource(R.drawable.palette), null)
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
                            modifier = Modifier.padding(
                                start = (16 * 2 + 24).dp,
                                end = 16.dp,
                                bottom = 12.dp
                            )
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
                                Icon(painterResource(R.drawable.palette), null)
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
                            modifier = Modifier.padding(
                                start = (16 * 2 + 24).dp,
                                end = 16.dp,
                                bottom = 12.dp
                            )
                        )
                    }
                }

                if (state.widgetType == WidgetType.TIMER) {
                    item {
                        Column(
                            Modifier.background(
                                listItemColors.containerColor.copy(alpha = 0.9f),
                                middleListItemShape
                            )
                        ) {
                            ListItem(
                                leadingContent = {
                                    Icon(painterResource(R.drawable.palette), null)
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
                                modifier = Modifier.padding(
                                    start = (16 * 2 + 24).dp,
                                    end = 16.dp,
                                    bottom = 12.dp
                                )
                            )
                        }
                    }

                    item {
                        Column(
                            Modifier.background(
                                listItemColors.containerColor.copy(alpha = 0.9f),
                                bottomListItemShape
                            )
                        ) {
                            ListItem(
                                leadingContent = {
                                    Icon(painterResource(R.drawable.palette), null)
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
                                modifier = Modifier.padding(
                                    start = (16 * 2 + 24).dp,
                                    end = 16.dp,
                                    bottom = 12.dp
                                )
                            )
                        }
                    }
                }

                if (state.widgetType == WidgetType.HISTORY) {
                    item {
                        Column(
                            Modifier.background(
                                listItemColors.containerColor.copy(alpha = 0.9f),
                                bottomListItemShape
                            )
                        ) {
                            ListItem(
                                leadingContent = {
                                    Icon(painterResource(R.drawable.restart), null)
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
    modifier: Modifier = Modifier,
    background: Color = colorScheme.surfaceContainer,
    onSurface: Color = colorScheme.onSurface,
    onSurfaceVariant: Color = colorScheme.onSurfaceVariant,
    tertiary: Color = colorScheme.tertiary,
    primary: Color = colorScheme.primary,
    onTertiary: Color = colorScheme.onTertiary,
    onPrimary: Color = colorScheme.onPrimary,
    widgetType: WidgetType
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        when (widgetType) {
            WidgetType.TIMER -> {
                TimerWidgetPreview(
                    opacity = opacity,
                    background = background,
                    tertiary = tertiary,
                    onTertiary = onTertiary,
                    primary = primary,
                    onPrimary = onPrimary,
                    modifier = Modifier.size(200.dp)
                )
            }

            WidgetType.TODAY -> {
                TodayAppWidgetPreview(
                    background = background,
                    onSurface = onSurface,
                    onSurfaceVariant = onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }

            WidgetType.HISTORY -> {
                HistoryWidgetPreview(
                    background = background,
                    onSurface = onSurface,
                    onSurfaceVariant = onSurfaceVariant,
                    primary = primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }

            else -> {
                Text("Select a widget type", color = Color.White)
            }
        }
    }
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
                        painterResource(R.drawable.check),
                        contentDescription = null,
                        tint = if (role in listOf(
                                "surface",
                                "surfaceVariant",
                                "background",
                                "white",
                                "primaryContainer",
                                "secondaryContainer",
                                "tertiaryContainer",
                                "errorContainer",
                                "onPrimary",
                                "onSecondary",
                                "onTertiary",
                                "onError",
                                "inverseSurface",
                                "inverseOnSurface",
                                "inversePrimary"
                            )
                        ) colorScheme.primary else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
