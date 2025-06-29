package org.nsh07.pomodoro.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.nsh07.pomodoro.R
import org.nsh07.pomodoro.ui.theme.AppFonts.interDisplayBlack
import org.nsh07.pomodoro.ui.theme.AppFonts.openRundeClock

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppScreen(modifier: Modifier = Modifier) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Focus",
                        style = TextStyle(
                            fontFamily = interDisplayBlack,
                            fontSize = 32.sp,
                            lineHeight = 32.sp,
                            color = colorScheme.onPrimaryContainer
                        )
                    )
                },
                subtitle = {},
                titleHorizontalAlignment = Alignment.CenterHorizontally
            )
        },
        modifier = modifier.fillMaxSize()
    ) { insets ->
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(insets)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { 0.3f },
                        modifier = Modifier.size(350.dp),
                        strokeWidth = 32.dp,
                        gapSize = 32.dp
                    )
                    Box(Modifier.width(220.dp)) {
                        Text(
                            text = "08:34",
                            style = TextStyle(
                                fontFamily = openRundeClock,
                                fontWeight = FontWeight.Bold,
                                fontSize = 76.sp,
                                letterSpacing = (-2).sp
                            ),
                            maxLines = 1,
                            autoSize = TextAutoSize.StepBased()
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    FilledTonalIconButton(
                        onClick = { /*TODO*/ },
                        shapes = IconButtonDefaults.shapes(),
                        modifier = Modifier.size(96.dp)
                    ) {
                        Icon(
                            painterResource(R.drawable.restart_large),
                            contentDescription = "Restart"
                        )
                    }
                    FilledIconButton(
                        onClick = { /*TODO*/ },
                        shapes = IconButtonDefaults.shapes(),
                        modifier = Modifier.size(width = 128.dp, height = 96.dp)
                    ) {
                        Icon(
                            painterResource(R.drawable.pause_large),
                            contentDescription = "Pause"
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Up next", style = typography.titleSmall)
                Text(
                    "5:00",
                    style = TextStyle(
                        fontFamily = openRundeClock,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        lineHeight = 28.sp,
                        color = colorScheme.tertiary
                    )
                )
                Text("Short break", style = typography.titleMediumEmphasized)
            }
        }
    }
}