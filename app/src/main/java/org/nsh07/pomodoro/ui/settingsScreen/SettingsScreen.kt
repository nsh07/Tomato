package org.nsh07.pomodoro.ui.settingsScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.nsh07.pomodoro.ui.theme.AppFonts.openRundeClock
import org.nsh07.pomodoro.ui.theme.AppFonts.robotoFlexTitle
import org.nsh07.pomodoro.ui.theme.TomatoTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen(
    focusTime: Int,
    shortBreakTime: Int,
    longBreakTime: Int,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Column(modifier.nestedScroll(scrollBehavior.nestedScrollConnection)) {
        TopAppBar(
            title = {
                Text(
                    "Settings",
                    style = LocalTextStyle.current.copy(
                        fontFamily = robotoFlexTitle,
                        fontSize = 32.sp,
                        lineHeight = 32.sp
                    )
                )
            },
            subtitle = {},
            titleHorizontalAlignment = Alignment.CenterHorizontally,
            scrollBehavior = scrollBehavior
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            item {
                Text(
                    "Durations",
                    style = typography.titleSmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                )
            }
            item {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 16.dp,
                                        bottomStart = 16.dp,
                                        topEnd = 4.dp,
                                        bottomEnd = 4.dp
                                    )
                                )
                                .size(112.dp, 100.dp)
                                .background(colorScheme.surfaceContainer)
                        ) {
                            Text(
                                text = remember(focusTime) { (focusTime / 60000).toString() },
                                style = TextStyle(
                                    fontFamily = openRundeClock,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 57.sp,
                                    letterSpacing = (-2).sp
                                ),
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            "Focus",
                            style = typography.titleSmallEmphasized,
                            color = colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                    Spacer(Modifier.width(2.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .size(112.dp, 100.dp)
                                .background(colorScheme.surfaceContainer)
                        ) {
                            Text(
                                text = remember(shortBreakTime) {
                                    (shortBreakTime / 60000).toString().padStart(2, '0')
                                },
                                style = TextStyle(
                                    fontFamily = openRundeClock,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 57.sp,
                                    letterSpacing = (-2).sp
                                ),
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            "Short break",
                            style = typography.titleSmallEmphasized,
                            color = colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                    Spacer(Modifier.width(2.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 4.dp,
                                        bottomStart = 4.dp,
                                        topEnd = 16.dp,
                                        bottomEnd = 16.dp
                                    )
                                )
                                .size(112.dp, 100.dp)
                                .background(colorScheme.surfaceContainer)
                        ) {
                            Text(
                                text = remember(shortBreakTime) {
                                    (longBreakTime / 60000).toString().padStart(2, '0')
                                },
                                style = TextStyle(
                                    fontFamily = openRundeClock,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 57.sp,
                                    letterSpacing = (-2).sp
                                ),
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            "Long break",
                            style = typography.titleSmallEmphasized,
                            color = colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(
    showSystemUi = true,
    device = Devices.PIXEL_9_PRO
)
@Composable
fun SettingsScreenPreview() {
    TomatoTheme {
        SettingsScreen(
            focusTime = 88 * 60 * 1000,
            shortBreakTime = 88 * 60 * 1000,
            longBreakTime = 88 * 60 * 1000,
            modifier = Modifier.fillMaxSize()
        )
    }
}
