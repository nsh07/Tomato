package org.nsh07.pomodoro.ui.theme

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

object CustomColors {
    var black = false

    @OptIn(ExperimentalMaterial3Api::class)
    val topBarColors: TopAppBarColors
        @Composable get() =
            TopAppBarDefaults.topAppBarColors(
                containerColor = if (!black) colorScheme.surfaceContainer else colorScheme.surface,
                scrolledContainerColor = if (!black) colorScheme.surfaceContainer else colorScheme.surface
            )

    val listItemColors: ListItemColors
        @Composable get() =
            ListItemDefaults.colors(containerColor = if (!black) colorScheme.surfaceBright else colorScheme.surfaceContainerHigh)

    val selectedListItemColors: ListItemColors
        @Composable get() =
            ListItemDefaults.colors(
                containerColor = colorScheme.secondaryContainer,
                headlineColor = colorScheme.secondary,
                leadingIconColor = colorScheme.onSecondaryContainer,
                supportingColor = colorScheme.onSecondaryFixedVariant,
                trailingIconColor = colorScheme.onSecondaryFixedVariant
            )
}