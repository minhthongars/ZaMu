package com.minhthong.zamu.navigation

import android.app.Activity
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.minhthong.navigation.Navigation
import com.minhthong.navigation.Screen
import com.minhthong.zamu.main.MainActivity

class NavigationImpl(
    private val screenMap: Map<Screen, Int>
): Navigation {

    private lateinit var navController: NavController

    override fun setNavController(navController: NavController) {
        this.navController = navController
    }

    override fun navigateTo(screen: Screen) {
        val destination = screenMap[screen] ?: return
        safeNavigate(destination)
    }

    override fun navigateTo(destination: Int) {
        safeNavigate(destination)
    }

    private fun safeNavigate(destination: Int) {
        if (navController.currentDestination?.id == destination) {
            return
        }

        val navOptions = NavOptions.Builder()
            .setLaunchSingleTop(true)
            .setRestoreState(true)
            .build()

        navController.navigate(destination, null, navOptions)
    }

    override fun getActivity(): Class<out Activity> {
        return MainActivity::class.java
    }
}