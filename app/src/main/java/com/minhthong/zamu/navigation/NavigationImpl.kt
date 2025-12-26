package com.minhthong.zamu.navigation

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.minhthong.navigation.Navigation
import com.minhthong.navigation.Screen
import com.minhthong.zamu.R
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

    override fun appLaunchIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP

            putExtra(MainActivity.NAVIGATE_SCREEN_ID, R.id.playerFragment)
        }

        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}