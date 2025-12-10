package com.minhthong.navigation

import androidx.navigation.NavController

interface Navigation {

    fun setNavController(navController: NavController)

    fun navigateTo(screen: Screen)

    fun navigateTo(destination: Int)
}