/*
 * Copyright (c) 2025 Nishant Mishra
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.nsh07.pomodoro.ui.settingsScreen

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.nsh07.pomodoro.R
import org.nsh07.pomodoro.service.TimerService
import org.nsh07.pomodoro.ui.settingsScreen.viewModel.SettingsViewModel
import org.nsh07.pomodoro.ui.theme.AppFonts.robotoFlexTopBar
import org.nsh07.pomodoro.ui.theme.CustomColors.listItemColors
import org.nsh07.pomodoro.ui.theme.CustomColors.topBarColors
import org.nsh07.pomodoro.ui.theme.TomatoShapeDefaults.bottomListItemShape
import org.nsh07.pomodoro.ui.theme.TomatoShapeDefaults.middleListItemShape
import org.nsh07.pomodoro.ui.theme.TomatoShapeDefaults.topListItemShape
import org.nsh07.pomodoro.ui.theme.TomatoTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenRoot(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
) {
    val context = LocalContext.current
    val focusTimeInputFieldState = rememberSaveable(saver = TextFieldState.Saver) {
        viewModel.focusTimeTextFieldState
    }
    val shortBreakTimeInputFieldState = rememberSaveable(saver = TextFieldState.Saver) {
        viewModel.shortBreakTimeTextFieldState
    }
    val longBreakTimeInputFieldState = rememberSaveable(saver = TextFieldState.Saver) {
        viewModel.longBreakTimeTextFieldState
    }

    val alarmEnabled by viewModel.alarmEnabled.collectAsStateWithLifecycle()
    val vibrateEnabled by viewModel.vibrateEnabled.collectAsStateWithLifecycle()
    val alarmSound by viewModel.alarmSound.collectAsStateWithLifecycle()

    val sessionsSliderState = rememberSaveable(
        saver = SliderState.Saver(
            viewModel.sessionsSliderState.onValueChangeFinished,
            viewModel.sessionsSliderState.valueRange
        )
    ) {
        viewModel.sessionsSliderState
    }

    SettingsScreen(
        focusTimeInputFieldState = focusTimeInputFieldState,
        shortBreakTimeInputFieldState = shortBreakTimeInputFieldState,
        longBreakTimeInputFieldState = longBreakTimeInputFieldState,
        sessionsSliderState = sessionsSliderState,
        alarmEnabled = alarmEnabled,
        vibrateEnabled = vibrateEnabled,
        alarmSound = alarmSound,
        onAlarmEnabledChange = viewModel::saveAlarmEnabled,
        onVibrateEnabledChange = viewModel::saveVibrateEnabled,
        onAlarmSoundChanged = {
            viewModel.saveAlarmSound(it)
            Intent(context, TimerService::class.java).apply {
                action = TimerService.Actions.RESET.toString()
                context.startService(this)
            }
        },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SettingsScreen(
    focusTimeInputFieldState: TextFieldState,
    shortBreakTimeInputFieldState: TextFieldState,
    longBreakTimeInputFieldState: TextFieldState,
    sessionsSliderState: SliderState,
    alarmEnabled: Boolean,
    vibrateEnabled: Boolean,
    alarmSound: Uri?,
    onAlarmEnabledChange: (Boolean) -> Unit,
    onVibrateEnabledChange: (Boolean) -> Unit,
    onAlarmSoundChanged: (Uri?) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val switchColors = SwitchDefaults.colors(
        checkedIconColor = colorScheme.primary,
    )

    var selectedSoundUri by remember { mutableStateOf(alarmSound) }
    var selectedSoundName by remember { mutableStateOf("...") }
    val context = LocalContext.current

    val ringtonePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    result.data?.getParcelableExtra(
                        RingtoneManager.EXTRA_RINGTONE_PICKED_URI,
                        Uri::class.java
                    )
                } else {
                    @Suppress("DEPRECATION")
                    result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
                }
            selectedSoundUri = uri
            onAlarmSoundChanged(uri)
        }
    }

    // 3. The Intent to launch the picker
    val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
        // We want to show only alarm sounds
        putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
        // A title for the picker
        putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Alarm sound")

        // If a sound is already selected, show it as checked
        putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, selectedSoundUri)
    }

    val switchItems = remember(alarmEnabled, vibrateEnabled) {
        listOf(
            SettingsSwitchItem(
                checked = alarmEnabled,
                icon = R.drawable.alarm_on,
                label = "Alarm",
                description = "Ring alarm when a timer completes",
                onClick = onAlarmEnabledChange
            ),
            SettingsSwitchItem(
                checked = vibrateEnabled,
                icon = R.drawable.mobile_vibrate,
                label = "Vibrate",
                description = "Vibrate when a timer completes",
                onClick = onVibrateEnabledChange
            )
        )
    }

    LaunchedEffect(selectedSoundUri) {
        selectedSoundName =
            RingtoneManager.getRingtone(context, selectedSoundUri)
                .getTitle(context)
    }

    Column(modifier.nestedScroll(scrollBehavior.nestedScrollConnection)) {
        TopAppBar(
            title = {
                Text(
                    "Settings",
                    style = LocalTextStyle.current.copy(
                        fontFamily = robotoFlexTopBar,
                        fontSize = 32.sp,
                        lineHeight = 32.sp
                    )
                )
            },
            subtitle = {},
            colors = topBarColors,
            titleHorizontalAlignment = Alignment.CenterHorizontally,
            scrollBehavior = scrollBehavior
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier
                .background(topBarColors.containerColor)
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
                                topStart = topListItemShape.topStart,
                                bottomStart = topListItemShape.topStart,
                                topEnd = topListItemShape.bottomStart,
                                bottomEnd = topListItemShape.bottomStart
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
                            shape = RoundedCornerShape(middleListItemShape.topStart),
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
                                topStart = bottomListItemShape.topStart,
                                bottomStart = bottomListItemShape.topStart,
                                topEnd = bottomListItemShape.bottomStart,
                                bottomEnd = bottomListItemShape.bottomStart
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
                        Text("Session length")
                    },
                    supportingContent = {
                        Column {
                            Text("Focus intervals in one session: ${sessionsSliderState.value.toInt()}")
                            Slider(
                                state = sessionsSliderState,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    },
                    colors = listItemColors,
                    modifier = Modifier.clip(topListItemShape)
                )
            }
            item {
                ListItem(
                    leadingContent = {
                        Icon(painterResource(R.drawable.alarm), null)
                    },
                    headlineContent = { Text("Alarm sound") },
                    supportingContent = { Text(selectedSoundName) },
                    colors = listItemColors,
                    modifier = Modifier
                        .clip(bottomListItemShape)
                        .clickable(onClick = { ringtonePickerLauncher.launch(intent) })
                )
            }
            item { Spacer(Modifier.height(12.dp)) }
            itemsIndexed(switchItems) { index, item ->
                ListItem(
                    leadingContent = {
                        Icon(painterResource(item.icon), contentDescription = null)
                    },
                    headlineContent = { Text(item.label) },
                    supportingContent = { Text(item.description) },
                    trailingContent = {
                        Switch(
                            checked = item.checked,
                            onCheckedChange = { item.onClick(it) },
                            thumbContent = {
                                if (item.checked) {
                                    Icon(
                                        painter = painterResource(R.drawable.check),
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize),
                                    )
                                } else {
                                    Icon(
                                        painter = painterResource(R.drawable.clear),
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize),
                                    )
                                }
                            },
                            colors = switchColors
                        )
                    },
                    colors = listItemColors,
                    modifier = Modifier
                        .clip(
                            when (index) {
                                0 -> topListItemShape
                                switchItems.lastIndex -> bottomListItemShape
                                else -> middleListItemShape
                            }
                        )
                )
            }
            item {
                var expanded by remember { mutableStateOf(false) }
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier
                        .padding(vertical = 6.dp)
                        .fillMaxWidth()
                ) {
                    FilledTonalIconToggleButton(
                        checked = expanded,
                        onCheckedChange = { expanded = it },
                        shapes = IconButtonDefaults.toggleableShapes(),
                        modifier = Modifier.width(52.dp)
                    ) {
                        Icon(
                            painterResource(R.drawable.info),
                            null
                        )
                    }
                    AnimatedVisibility(expanded) {
                        Text(
                            "A \"session\" is a sequence of pomodoro intervals that contain focus" +
                                    " intervals, short break intervals, and a long break interval. The " +
                                    "last break of a session is always a long break.",
                            style = typography.bodyMedium,
                            color = colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
            sessionsSliderState = rememberSliderState(value = 3f, steps = 3, valueRange = 1f..5f),
            alarmEnabled = true,
            vibrateEnabled = true,
            alarmSound = Settings.System.DEFAULT_ALARM_ALERT_URI,
            onAlarmEnabledChange = {},
            onVibrateEnabledChange = {},
            onAlarmSoundChanged = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}

data class SettingsSwitchItem(
    val checked: Boolean,
    @DrawableRes val icon: Int,
    val label: String,
    val description: String,
    val onClick: (Boolean) -> Unit
)
