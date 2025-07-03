package org.nsh07.pomodoro.ui.timerScreen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.nsh07.pomodoro.R
import org.nsh07.pomodoro.ui.theme.AppFonts.openRundeClock
import org.nsh07.pomodoro.ui.theme.AppFonts.robotoFlexTitle
import org.nsh07.pomodoro.ui.theme.TomatoTheme
import org.nsh07.pomodoro.ui.viewModel.TimerMode
import org.nsh07.pomodoro.ui.viewModel.UiState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TimerScreen(
    uiState: UiState,
    showBrandTitle: Boolean,
    progress: () -> Float,
    resetTimer: () -> Unit,
    skipTimer: () -> Unit,
    toggleTimer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val motionScheme = motionScheme
    val haptic = LocalHapticFeedback.current

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

    LaunchedEffect(uiState.timerMode) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    Column(modifier = modifier) {
        TopAppBar(
            title = {
                AnimatedContent(
                    if (!showBrandTitle) uiState.timerMode else TimerMode.BRAND,
                    transitionSpec = {
                        slideInVertically(
                            animationSpec = motionScheme.slowSpatialSpec(),
                            initialOffsetY = { (-it * 1.25).toInt() }
                        ).togetherWith(
                            slideOutVertically(
                                animationSpec = motionScheme.slowSpatialSpec(),
                                targetOffsetY = { (it * 1.25).toInt() }
                            )
                        )
                    }
                ) {
                    when (it) {
                        TimerMode.BRAND ->
                            Text(
                                "Tomato",
                                style = TextStyle(
                                    fontFamily = robotoFlexTitle,
                                    fontSize = 32.sp,
                                    lineHeight = 32.sp,
                                    color = colorScheme.onErrorContainer
                                ),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.width(210.dp)
                            )

                        TimerMode.FOCUS ->
                            Text(
                                "Focus",
                                style = TextStyle(
                                    fontFamily = robotoFlexTitle,
                                    fontSize = 32.sp,
                                    lineHeight = 32.sp,
                                    color = colorScheme.onPrimaryContainer
                                ),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.width(210.dp)
                            )

                        TimerMode.SHORT_BREAK -> Text(
                            "Short Break",
                            style = TextStyle(
                                fontFamily = robotoFlexTitle,
                                fontSize = 32.sp,
                                lineHeight = 32.sp,
                                color = colorScheme.onTertiaryContainer
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.width(210.dp)
                        )

                        TimerMode.LONG_BREAK -> Text(
                            "Long Break",
                            style = TextStyle(
                                fontFamily = robotoFlexTitle,
                                fontSize = 32.sp,
                                lineHeight = 32.sp,
                                color = colorScheme.onTertiaryContainer
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.width(210.dp)
                        )
                    }
                }
            },
            subtitle = {},
            titleHorizontalAlignment = Alignment.CenterHorizontally
        )

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(contentAlignment = Alignment.Center) {
                    if (uiState.timerMode == TimerMode.FOCUS) {
                        CircularProgressIndicator(
                            progress = progress,
                            modifier = Modifier
                                .widthIn(max = 350.dp)
                                .fillMaxWidth(0.9f)
                                .aspectRatio(1f),
                            color = color,
                            trackColor = colorContainer,
                            strokeWidth = 16.dp,
                            gapSize = 16.dp
                        )
                    } else {
                        CircularWavyProgressIndicator(
                            progress = progress,
                            modifier = Modifier
                                .widthIn(max = 350.dp)
                                .fillMaxWidth(0.9f)
                                .aspectRatio(1f),
                            color = color,
                            trackColor = colorContainer,
                            stroke = Stroke(
                                width = with(LocalDensity.current) {
                                    16.dp.toPx()
                                },
                                cap = StrokeCap.Round,
                            ),
                            trackStroke = Stroke(
                                width = with(LocalDensity.current) {
                                    16.dp.toPx()
                                },
                                cap = StrokeCap.Round,
                            ),
                            wavelength = 60.dp,
                            gapSize = 16.dp
                        )
                    }
                    Text(
                        text = uiState.timeStr,
                        style = TextStyle(
                            fontFamily = openRundeClock,
                            fontWeight = FontWeight.Bold,
                            fontSize = 72.sp,
                            letterSpacing = (-2).sp
                        ),
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
                val interactionSources = remember { List(3) { MutableInteractionSource() } }
                ButtonGroup(
                    overflowIndicator = { state ->
                        FilledTonalIconButton(
                            onClick = {
                                if (state.isExpanded) {
                                    state.dismiss()
                                } else {
                                    state.show()
                                }
                            },
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = colorContainer
                            ),
                            shapes = IconButtonDefaults.shapes(),
                            modifier = Modifier
                                .size(64.dp, 96.dp)
                        ) {
                            Icon(
                                painterResource(R.drawable.more_vert_large),
                                contentDescription = "More",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    },
                    modifier = Modifier.padding(16.dp)
                ) {
                    customItem(
                        {
                            FilledIconToggleButton(
                                onCheckedChange = { toggleTimer() },
                                checked = uiState.timerRunning,
                                colors = IconButtonDefaults.filledIconToggleButtonColors(
                                    checkedContainerColor = color,
                                    checkedContentColor = onColor
                                ),
                                shapes = IconButtonDefaults.toggleableShapes(),
                                interactionSource = interactionSources[0],
                                modifier = Modifier
                                    .size(width = 128.dp, height = 96.dp)
                                    .animateWidth(interactionSources[0])
                            ) {
                                if (uiState.timerRunning) {
                                    Icon(
                                        painterResource(R.drawable.pause_large),
                                        contentDescription = "Pause",
                                        modifier = Modifier.size(32.dp)
                                    )
                                } else {
                                    Icon(
                                        painterResource(R.drawable.play_large),
                                        contentDescription = "Play",
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        },
                        { state ->
                            DropdownMenuItem(
                                leadingIcon = {
                                    if (uiState.timerRunning) {
                                        Icon(
                                            painterResource(R.drawable.pause),
                                            contentDescription = "Pause"
                                        )
                                    } else {
                                        Icon(
                                            painterResource(R.drawable.play),
                                            contentDescription = "Play"
                                        )
                                    }
                                },
                                text = { Text(if (uiState.timerRunning) "Pause" else "Play") },
                                onClick = {
                                    toggleTimer()
                                    state.dismiss()
                                }
                            )
                        }
                    )

                    customItem(
                        {
                            FilledTonalIconButton(
                                onClick = resetTimer,
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = colorContainer
                                ),
                                shapes = IconButtonDefaults.shapes(),
                                interactionSource = interactionSources[1],
                                modifier = Modifier
                                    .size(96.dp)
                                    .animateWidth(interactionSources[1])
                            ) {
                                Icon(
                                    painterResource(R.drawable.restart_large),
                                    contentDescription = "Restart",
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        },
                        { state ->
                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(
                                        painterResource(R.drawable.restart),
                                        "Restart"
                                    )
                                },
                                text = { Text("Restart") },
                                onClick = {
                                    resetTimer()
                                    state.dismiss()
                                }
                            )
                        }
                    )

                    customItem(
                        {
                            FilledTonalIconButton(
                                onClick = skipTimer,
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = colorContainer
                                ),
                                shapes = IconButtonDefaults.shapes(),
                                interactionSource = interactionSources[2],
                                modifier = Modifier
                                    .size(64.dp, 96.dp)
                                    .animateWidth(interactionSources[2])
                            ) {
                                Icon(
                                    painterResource(R.drawable.skip_next_large),
                                    contentDescription = "Skip to next",
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        },
                        { state ->
                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(
                                        painterResource(R.drawable.skip_next),
                                        "Skip to next"
                                    )
                                },
                                text = { Text("Skip to next") },
                                onClick = {
                                    skipTimer()
                                    state.dismiss()
                                }
                            )
                        }
                    )
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
                        else -> "Long Break"
                    },
                    style = typography.titleMediumEmphasized
                )
            }
        }
    }
}

@Preview(
    showSystemUi = true,
    device = Devices.PIXEL_9_PRO
)
@Composable
fun TimerScreenPreview() {
    val uiState = UiState(
        timeStr = "03:34", nextTimeStr = "5:00", timerMode = TimerMode.FOCUS, timerRunning = true
    )
    TomatoTheme {
        TimerScreen(
            uiState,
            false,
            { 0.3f },
            {},
            {},
            {}
        )
    }
}
