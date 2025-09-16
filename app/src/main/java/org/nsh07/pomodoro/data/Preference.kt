/*
 * Copyright (c) 2025 Nishant Mishra
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.nsh07.pomodoro.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Class for storing boolean preferences in the app's database
 */
@Entity(tableName = "boolean_preference")
data class BooleanPreference(
    @PrimaryKey
    val key: String,
    val value: Boolean
)

/**
 * Class for storing integer preferences in the app's database
 */
@Entity(tableName = "int_preference")
data class IntPreference(
    @PrimaryKey
    val key: String,
    val value: Int
)

/**
 * Class for storing string preferences in the app's database
 */
@Entity(tableName = "string_preference")
data class StringPreference(
    @PrimaryKey
    val key: String,
    val value: String
)