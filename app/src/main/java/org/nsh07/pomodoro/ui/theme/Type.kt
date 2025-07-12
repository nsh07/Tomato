/*
 * Copyright (c) 2025 Nishant Mishra
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.nsh07.pomodoro.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.nsh07.pomodoro.R
import org.nsh07.pomodoro.ui.theme.AppFonts.robotoFlexHeadline
import org.nsh07.pomodoro.ui.theme.AppFonts.robotoFlexTitle

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = robotoFlexHeadline,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = robotoFlexTitle,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
    )
)

object AppFonts {
    val openRundeClock = FontFamily(
        Font(R.font.open_runde_bold_clock_only, FontWeight.Bold)
    )

    @OptIn(ExperimentalTextApi::class)
    val robotoFlexTopBar = FontFamily(
        Font(
            R.font.roboto_flex_variable,
            variationSettings = FontVariation.Settings(
                FontVariation.width(125f),
                FontVariation.weight(1000),
                FontVariation.grade(0),
                FontVariation.Setting("XOPQ", 96F),
                FontVariation.Setting("XTRA", 500F),
                FontVariation.Setting("YOPQ", 79F),
                FontVariation.Setting("YTAS", 750F),
                FontVariation.Setting("YTDE", -203F),
                FontVariation.Setting("YTFI", 738F),
                FontVariation.Setting("YTLC", 514F),
                FontVariation.Setting("YTUC", 712F)
            )
        )
    )

    @OptIn(ExperimentalTextApi::class)
    val robotoFlexHeadline = FontFamily(
        Font(
            R.font.roboto_flex_variable,
            variationSettings = FontVariation.Settings(
                FontVariation.width(130f),
                FontVariation.weight(600),
                FontVariation.grade(0)
            )
        )
    )

    @OptIn(ExperimentalTextApi::class)
    val robotoFlexTitle = FontFamily(
        Font(
            R.font.roboto_flex_variable,
            variationSettings = FontVariation.Settings(
                FontVariation.width(130f),
                FontVariation.weight(700),
                FontVariation.grade(0)
            )
        )
    )
}