package com.minhthong.navigation

import androidx.fragment.app.Fragment
import androidx.navigation.NavController

interface Navigation {
    fun setNavController(navController: NavController)

    fun navigateTo(screen: Screen)
}