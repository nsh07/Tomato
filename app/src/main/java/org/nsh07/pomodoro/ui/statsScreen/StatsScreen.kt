package org.nsh07.pomodoro.ui.statsScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import org.nsh07.pomodoro.ui.theme.AppFonts.robotoFlexTitle

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun StatsScreen(modifier: Modifier = Modifier) {
    Column(modifier) {
        TopAppBar(
            title = {
                Text(
                    "Stats",
                    style = LocalTextStyle.current.copy(
                        fontFamily = robotoFlexTitle,
                        fontSize = 32.sp,
                        lineHeight = 32.sp
                    )
                )
            },
            subtitle = {},
            titleHorizontalAlignment = Alignment.CenterHorizontally
        )
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().background(colorScheme.surface)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                LoadingIndicator()
                Text("Coming Soon", style = typography.headlineSmall, fontFamily = robotoFlexTitle)
            }
        }
    }
}