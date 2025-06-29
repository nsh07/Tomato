package org.nsh07.pomodoro.ui.timerScreen

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.nsh07.pomodoro.R
import org.nsh07.pomodoro.ui.theme.AppFonts.interDisplayBlack
import org.nsh07.pomodoro.ui.theme.AppFonts.openRundeClock
import org.nsh07.pomodoro.ui.viewModel.TimerMode
import org.nsh07.pomodoro.ui.viewModel.UiState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TimerScreen(
    uiState: UiState,
    resetTimer: () -> Unit,
    toggleTimer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color by animateColorAsState(
        if (uiState.timerMode == TimerMode.FOCUS) colorScheme.primary
        else colorScheme.tertiary,
        animationSpec = motionScheme.slowEffectsSpec()
    )
    val onColor by animateColorAsState(
        if (uiState.timerMode == TimerMode.FOCUS) colorScheme.onPrimary
        else colorScheme.onTertiary,
        animationSpec = motionScheme.slowEffectsSpec()
    )
    val colorContainer by animateColorAsState(
        if (uiState.timerMode == TimerMode.FOCUS) colorScheme.secondaryContainer
        else colorScheme.tertiaryContainer,
        animationSpec = motionScheme.slowEffectsSpec()
    )
    val onColorContainer by animateColorAsState(
        if (uiState.timerMode == TimerMode.FOCUS) colorScheme.onPrimaryContainer
        else colorScheme.onTertiaryContainer,
        animationSpec = motionScheme.slowEffectsSpec()
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (uiState.timerMode) {
                            TimerMode.FOCUS -> "Focus"
                            TimerMode.SHORT_BREAK -> "Short Break"
                            TimerMode.LONG_BREAK -> "Long Break"
                        },
                        style = TextStyle(
                            fontFamily = interDisplayBlack,
                            fontSize = 32.sp,
                            lineHeight = 32.sp,
                            color = onColorContainer
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
                        progress = { (uiState.totalTime.toFloat() - uiState.remainingTime) / uiState.totalTime },
                        modifier = Modifier.size(350.dp),
                        color = color,
                        trackColor = colorContainer,
                        strokeWidth = 16.dp,
                        gapSize = 16.dp
                    )
//                    Box {
                    Text(
                        text = uiState.timeStr,
                        style = TextStyle(
                            fontFamily = openRundeClock,
                            fontWeight = FontWeight.Bold,
                            fontSize = 76.sp,
                            letterSpacing = (-2).sp
                        ),
                        maxLines = 1
//                            autoSize = TextAutoSize.StepBased(stepSize = 24.sp)
                    )
//                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    FilledTonalIconButton(
                        onClick = resetTimer,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = colorContainer),
                        shapes = IconButtonDefaults.shapes(),
                        modifier = Modifier.size(96.dp)
                    ) {
                        Icon(
                            painterResource(R.drawable.restart_large),
                            contentDescription = "Restart"
                        )
                    }
                    FilledIconToggleButton(
                        onCheckedChange = { toggleTimer() },
                        checked = uiState.timerRunning,
                        colors = IconButtonDefaults.filledIconToggleButtonColors(
                            checkedContainerColor = color,
                            checkedContentColor = onColor
                        ),
                        shapes = IconButtonDefaults.toggleableShapes(),
                        modifier = Modifier.size(width = 128.dp, height = 96.dp)
                    ) {
                        if (uiState.timerRunning) {
                            Icon(
                                painterResource(R.drawable.pause_large),
                                contentDescription = "Pause"
                            )
                        } else {
                            Icon(
                                painterResource(R.drawable.play_large),
                                contentDescription = "Play"
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Up next", style = typography.titleSmall)
                Text(
                    uiState.nextTimeStr,
                    style = TextStyle(
                        fontFamily = openRundeClock,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        lineHeight = 28.sp,
                        color = if (uiState.nextTimerMode == TimerMode.FOCUS) colorScheme.primary else colorScheme.tertiary
                    )
                )
                Text(
                    when (uiState.nextTimerMode) {
                        TimerMode.FOCUS -> "Focus"
                        TimerMode.SHORT_BREAK -> "Short Break"
                        TimerMode.LONG_BREAK -> "Long Break"
                    },
                    style = typography.titleMediumEmphasized
                )
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun TimerScreenPreview() {
    val uiState = UiState(
        timeStr = "08:34", nextTimeStr = "5:00"
    )
    TimerScreen(uiState, {}, {})
}
