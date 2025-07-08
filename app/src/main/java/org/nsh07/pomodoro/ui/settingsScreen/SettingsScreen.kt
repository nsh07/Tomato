package org.nsh07.pomodoro.ui.settingsScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.nsh07.pomodoro.R
import org.nsh07.pomodoro.ui.theme.AppFonts.robotoFlexTitle
import org.nsh07.pomodoro.ui.theme.TomatoTheme
import org.nsh07.pomodoro.ui.viewModel.SettingsViewModel

@Composable
fun SettingsScreenRoot(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
) {
    val focusTimeInputFieldState = rememberSaveable(saver = TextFieldState.Saver) {
        viewModel.focusTimeTextFieldState
    }
    val shortBreakTimeInputFieldState = rememberSaveable(saver = TextFieldState.Saver) {
        viewModel.shortBreakTimeTextFieldState
    }
    val longBreakTimeInputFieldState = rememberSaveable(saver = TextFieldState.Saver) {
        viewModel.longBreakTimeTextFieldState
    }

    SettingsScreen(
        focusTimeInputFieldState = focusTimeInputFieldState,
        shortBreakTimeInputFieldState = shortBreakTimeInputFieldState,
        longBreakTimeInputFieldState = longBreakTimeInputFieldState,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SettingsScreen(
    focusTimeInputFieldState: TextFieldState,
    shortBreakTimeInputFieldState: TextFieldState,
    longBreakTimeInputFieldState: TextFieldState,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val sessionsSliderState = rememberSliderState(value = 3f, steps = 3, valueRange = 1f..5f)

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
            colors = TopAppBarDefaults.topAppBarColors(containerColor = colorScheme.surfaceContainer),
            titleHorizontalAlignment = Alignment.CenterHorizontally,
            scrollBehavior = scrollBehavior
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier
                .background(colorScheme.surfaceContainer)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(Modifier.height(12.dp))
            }
            item {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            "Focus",
                            style = typography.titleSmallEmphasized
                        )
                        MinuteInputField(
                            state = focusTimeInputFieldState,
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                bottomStart = 16.dp,
                                topEnd = 4.dp,
                                bottomEnd = 4.dp
                            ),
                            imeAction = ImeAction.Next
                        )
                    }
                    Spacer(Modifier.width(2.dp))
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            "Short break",
                            style = typography.titleSmallEmphasized
                        )
                        MinuteInputField(
                            state = shortBreakTimeInputFieldState,
                            shape = RoundedCornerShape(4.dp),
                            imeAction = ImeAction.Next
                        )
                    }
                    Spacer(Modifier.width(2.dp))
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            "Long break",
                            style = typography.titleSmallEmphasized
                        )
                        MinuteInputField(
                            state = longBreakTimeInputFieldState,
                            shape = RoundedCornerShape(
                                topStart = 4.dp,
                                bottomStart = 4.dp,
                                topEnd = 16.dp,
                                bottomEnd = 16.dp
                            ),
                            imeAction = ImeAction.Done
                        )
                    }
                }
            }
            item {
                Spacer(Modifier.height(12.dp))
            }
            item {
                ListItem(
                    leadingContent = {
                        Icon(
                            painterResource(R.drawable.clocks),
                            null
                        )
                    },
                    headlineContent = {
                        Text("Sessions")
                    },
                    supportingContent = {
                        Column {
                            Text("${sessionsSliderState.value.toInt()} sessions before a long break")
                            Slider(
                                state = sessionsSliderState,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    },
                    modifier = Modifier.clip(shapes.large)
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
fun SettingsScreenPreview() {
    TomatoTheme {
        SettingsScreen(
            focusTimeInputFieldState = rememberTextFieldState((25 * 60 * 1000).toString()),
            shortBreakTimeInputFieldState = rememberTextFieldState((5 * 60 * 1000).toString()),
            longBreakTimeInputFieldState = rememberTextFieldState((15 * 60 * 1000).toString()),
            modifier = Modifier.fillMaxSize()
        )
    }
}
