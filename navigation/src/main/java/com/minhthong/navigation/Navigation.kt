package com.minhthong.navigation

import android.app.Activity
import androidx.navigation.NavController

interface Navigation {

    fun setNavController(navController: NavController)

    fun navigateTo(screen: Screen)

    fun navigateTo(destination: Int)

    fun getActivity(): Class<out Activity>
}