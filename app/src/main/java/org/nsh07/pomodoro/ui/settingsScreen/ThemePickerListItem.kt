/*
 * Copyright (c) 2025 Nishant Mishra
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.nsh07.pomodoro.ui.settingsScreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import org.nsh07.pomodoro.ui.theme.CustomColors.listItemColors

@Composable
fun ThemePickerListItem(
    theme: String,
    themeMap: Map<String, Pair<Int, String>>,
    reverseThemeMap: Map<String, String>,
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

    ListItem(
        leadingContent = {
            Icon(
                painter = painterResource(themeMap[theme]!!.first),
                contentDescription = null
            )
        },
        headlineContent = { Text("Theme") },
        supportingContent = {
            Text(themeMap[theme]!!.second)
        },
        colors = listItemColors,
        modifier = modifier
            .fillMaxWidth()
            .clickable { showDialog = true }
    )
}