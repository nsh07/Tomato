/*
 * Copyright (c) 2025 Nishant Mishra
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.nsh07.pomodoro.ui.settingsScreen

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import org.nsh07.pomodoro.R
import org.nsh07.pomodoro.ui.ClickableListItem
import org.nsh07.pomodoro.ui.theme.CustomColors.listItemColors

@Composable
fun ColorSchemePickerListItem(
    color: Color,
    items: Int,
    index: Int,
    onColorChange: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by rememberSaveable { mutableStateOf(false) }

    if (showDialog) {
        ColorSchemePickerDialog(
            currentColor = color,
            setShowDialog = { showDialog = it },
            onColorChange = onColorChange
        )
    }

    ClickableListItem(
        leadingContent = {
            Icon(
                painter = painterResource(R.drawable.palette),
                contentDescription = null,
                tint = colorScheme.primary
            )
        },
        headlineContent = { Text(stringResource(R.string.color_scheme)) },
        supportingContent = {
            Text(
                if (color == Color.White) stringResource(R.string.dynamic)
                else stringResource(R.string.color)
            )
        },
        colors = listItemColors,
        items = items,
        index = index,
        modifier = modifier.fillMaxWidth()
    ) { showDialog = true }
}