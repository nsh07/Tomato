/*
 * Copyright (c) 2025 Nishant Mishra
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.nsh07.pomodoro.data

import android.content.Context

interface AppContainer {
    val appPreferencesRepository: AppPreferenceRepository
    val appStatRepository: AppStatRepository
    val appTimerRepository: AppTimerRepository
}

class DefaultAppContainer(context: Context) : AppContainer {

    override val appPreferencesRepository: AppPreferenceRepository by lazy {
        AppPreferenceRepository(AppDatabase.getDatabase(context).preferenceDao())
    }

    override val appStatRepository: AppStatRepository by lazy {
        AppStatRepository(AppDatabase.getDatabase(context).statDao())
    }

    override val appTimerRepository: AppTimerRepository by lazy { AppTimerRepository() }

}