package com.minhthong.zamu.navigation

import androidx.navigation.NavController
import com.minhthong.navigation.Navigation
import com.minhthong.navigation.Screen

class NavigationImpl(
    private val screenMap: Map<Screen, Int>
): Navigation {

    private lateinit var navController: NavController

    override fun setNavController(navController: NavController) {
        this.navController = navController
    }

    override fun navigateTo(screen: Screen) {
        safeNavigate(
            destination = screenMap.getValue(screen)
        )
    }

    private fun safeNavigate(destination: Int) {
        if (navController.currentDestination?.id == destination) {
            return
        }
        navController.navigate(destination)
    }
}