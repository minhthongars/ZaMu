package com.minhthong.navigation

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import androidx.navigation.NavController

interface Navigation {

    fun setNavController(navController: NavController)

    fun navigateTo(screen: Screen)

    fun navigateTo(destination: Int)

    fun appLaunchIntent(context: Context): PendingIntent
}