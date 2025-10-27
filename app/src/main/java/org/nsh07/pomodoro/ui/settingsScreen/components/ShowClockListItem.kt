package org.nsh07.pomodoro.ui.settingsScreen.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.nsh07.pomodoro.R
import org.nsh07.pomodoro.ui.theme.CustomColors.listItemColors
import org.nsh07.pomodoro.ui.theme.TomatoShapeDefaults.bottomListItemShape
import org.nsh07.pomodoro.ui.theme.TomatoShapeDefaults.middleListItemShape
import org.nsh07.pomodoro.ui.theme.TomatoShapeDefaults.topListItemShape


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ShowClockPickerListItem(
    showClock: String,
    items: Int,
    index: Int,
    onShowClockChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {

    val showClockMap: Map<String, Pair<Int, Int>> = remember {
        mapOf(
            "Both" to Pair(R.drawable.brightness_auto, R.string.show_clock_both),
            "Timer" to Pair(R.drawable.light_mode, R.string.show_clock_timer),
            "AOD" to Pair(R.drawable.dark_mode, R.string.show_clock_aod)
        )
    }

    Column(
        modifier
            .clip(
                when (index) {
                    0 -> topListItemShape
                    items - 1 -> bottomListItemShape
                    else -> middleListItemShape
                },
            ),
    ) {
        ListItem(
            leadingContent = {
                AnimatedContent(showClockMap[showClock]!!.first) {
                    Icon(
                        painter = painterResource(it),
                        contentDescription = null,
                    )
                }
            },
            headlineContent = { Text(stringResource(R.string.show_clock)) },
            colors = listItemColors,
        )

        val options = showClockMap.toList()
        val selectedIndex = options.indexOfFirst { it.first == showClock }

        Row(
            horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
            modifier = Modifier
                .background(listItemColors.containerColor)
                .padding(start = 52.dp, end = 16.dp, bottom = 8.dp)
        ) {
            options.forEachIndexed { idx, entry ->
                val isSelected = selectedIndex == idx
                ToggleButton(
                    checked = isSelected,
                    onCheckedChange = { onShowClockChange(entry.first) },
                    modifier = Modifier
                        .weight(1f)
                        .semantics { role = Role.RadioButton },
                    shapes =
                        when (idx) {
                            0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                            options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                            else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                        },
                ) {
                    Text(
                        stringResource(entry.second.second),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
