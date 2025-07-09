/*
 *     Copyright (c) 2025 Nishant Mishra
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.nsh07.pomodoro.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stat")
data class Stat(
    @PrimaryKey
    val date: String,
    val focusTime: Int,
    val breakTime: Int
)
