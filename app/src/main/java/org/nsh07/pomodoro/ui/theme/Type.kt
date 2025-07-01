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

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)

object AppFonts {
    val openRundeClock = FontFamily(
        Font(R.font.open_runde_bold_clock_only, FontWeight.Bold)
    )

    @OptIn(ExperimentalTextApi::class)
    val robotoFlexTitle = FontFamily(
        Font(
            R.font.roboto_flex_variable,
            variationSettings = FontVariation.Settings(
                FontVariation.width(125f),
                FontVariation.weight(1000),
                FontVariation.Setting("xtra", 500F)
            )
        )
    )
}