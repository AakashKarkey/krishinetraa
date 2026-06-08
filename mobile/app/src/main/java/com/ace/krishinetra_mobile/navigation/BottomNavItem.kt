package com.ace.krishinetra_mobile.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.ace.krishinetra_mobile.R

sealed class BottomNavItem(
    val route: String,
    @StringRes val labelRes: Int,
    @DrawableRes val iconRes: Int
) {
    data object Home : BottomNavItem(
        route = "home",
        labelRes = R.string.nav_home,
        iconRes = R.drawable.ic_nav_home
    )

    data object Analyze : BottomNavItem(
        route = "analyze",
        labelRes = R.string.nav_analyze,
        iconRes = R.drawable.ic_nav_analyze
    )

    data object Chat : BottomNavItem(
        route = "chat",
        labelRes = R.string.nav_chat,
        iconRes = R.drawable.ic_nav_chat
    )

    data object Profile : BottomNavItem(
        route = "profile",
        labelRes = R.string.nav_profile,
        iconRes = R.drawable.ic_nav_profile
    )

    companion object {
        val items = listOf(Home, Analyze, Chat, Profile)
    }
}
