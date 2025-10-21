package org.nsh07.pomodoro.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed class Screen : NavKey {
    @Serializable
    object Timer : Screen()

    @Serializable
    object AOD : Screen()

    @Serializable
    object Settings : Screen()

    @Serializable
    object Stats : Screen()
}

data class NavItem(
    val route: Screen,
    @param:DrawableRes val unselectedIcon: Int,
    @param:DrawableRes val selectedIcon: Int,
    @param:StringRes val label: Int
)