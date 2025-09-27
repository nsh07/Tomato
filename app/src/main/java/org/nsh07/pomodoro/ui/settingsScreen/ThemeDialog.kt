/*
 * Copyright (c) 2025 Nishant Mishra
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.nsh07.pomodoro.ui.settingsScreen

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import org.nsh07.pomodoro.R
import org.nsh07.pomodoro.ui.theme.CustomColors.listItemColors
import org.nsh07.pomodoro.ui.theme.CustomColors.selectedListItemColors
import org.nsh07.pomodoro.ui.theme.TomatoShapeDefaults.bottomListItemShape
import org.nsh07.pomodoro.ui.theme.TomatoShapeDefaults.middleListItemShape
import org.nsh07.pomodoro.ui.theme.TomatoShapeDefaults.topListItemShape

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ThemeDialog(
    themeMap: Map<String, Pair<Int, String>>,
    reverseThemeMap: Map<String, String>,
    theme: String,
    setShowThemeDialog: (Boolean) -> Unit,
    onThemeChange: (String) -> Unit
) {
    val selectedOption =
        remember { mutableStateOf(themeMap[theme]!!.second) }

    BasicAlertDialog(
        onDismissRequest = { setShowThemeDialog(false) }
    ) {
        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight(),
            shape = shapes.extraLarge,
            color = colorScheme.surfaceContainer,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Choose theme",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.selectableGroup()
                ) {
                    themeMap.entries.forEachIndexed { index: Int, pair: Map.Entry<String, Pair<Int, String>> ->
                        val text = pair.value.second
                        val selected = text == selectedOption.value

                        ListItem(
                            leadingContent = {
                                AnimatedContent(selected) {
                                    if (it)
                                        Icon(painterResource(R.drawable.check), null)
                                    else
                                        Icon(painterResource(pair.value.first), null)
                                }
                            },
                            headlineContent = {
                                Text(text = text, style = MaterialTheme.typography.bodyLarge)
                            },
                            colors = if (!selected) listItemColors else selectedListItemColors,
                            modifier = Modifier
                                .height(64.dp)
                                .clip(
                                    when (index) {
                                        0 -> topListItemShape
                                        themeMap.size - 1 -> bottomListItemShape
                                        else -> middleListItemShape
                                    }
                                )
                                .selectable(
                                    selected = (text == selectedOption.value),
                                    onClick = {
                                        selectedOption.value = text
                                        onThemeChange(reverseThemeMap[selectedOption.value]!!)
                                    },
                                    role = Role.RadioButton
                                )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(
                    shapes = ButtonDefaults.shapes(),
                    onClick = { setShowThemeDialog(false) },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Ok")
                }
            }
        }
    }
}