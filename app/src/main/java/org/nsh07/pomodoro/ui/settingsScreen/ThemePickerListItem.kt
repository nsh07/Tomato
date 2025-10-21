/*
 * Copyright (c) 2025 Nishant Mishra
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.nsh07.pomodoro.ui.settingsScreen

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import org.nsh07.pomodoro.R
import org.nsh07.pomodoro.ui.ClickableListItem
import org.nsh07.pomodoro.ui.theme.CustomColors.listItemColors

@Composable
fun ThemePickerListItem(
    theme: String,
    themeMap: Map<String, Pair<Int, Int>>,
    reverseThemeMap: Map<String, String>,
    items: Int,
    index: Int,
    onThemeChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by rememberSaveable { mutableStateOf(false) }

    if (showDialog) {
        ThemeDialog(
            themeMap = themeMap,
            reverseThemeMap = reverseThemeMap,
            theme = theme,
            setShowThemeDialog = { showDialog = it },
            onThemeChange = onThemeChange
        )
    }

    ClickableListItem(
        leadingContent = {
            Icon(
                painter = painterResource(themeMap[theme]!!.first),
                contentDescription = null
            )
        },
        headlineContent = { Text(stringResource(R.string.theme)) },
        supportingContent = {
            Text(stringResource(themeMap[theme]!!.second))
        },
        colors = listItemColors,
        items = items,
        index = index,
        modifier = modifier.fillMaxWidth()
    ) { showDialog = true }
}