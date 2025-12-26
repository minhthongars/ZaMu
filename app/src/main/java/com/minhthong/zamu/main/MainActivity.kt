package com.minhthong.zamu.main

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.minhthong.core.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity: AppCompatActivity() {

    companion object {
        const val PREFS = "app_prefs"
        const val KEY_THEME = "selected_theme"
        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"
        const val THEME_DRACULA = "dracula"

        const val NAVIGATE_SCREEN_ID = "navigate"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        applyTheme()

        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            attachFragment()
        }

        enableEdgeToEdge()

        setupWindowInsets()
    }

    override fun onResume() {
        super.onResume()
        configureStatusBarAppearance()
    }

    private fun attachFragment() {
        supportFragmentManager.beginTransaction()
            .replace(
                android.R.id.content,
                MainFragment()
            )
            .commit()
    }

    private fun applyTheme() {
        setTheme(R.style.Theme_App_Dark)
        val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
        when (prefs.getString(KEY_THEME, THEME_LIGHT)) {
            THEME_LIGHT -> setTheme(R.style.Theme_App_Light)
            THEME_DARK -> setTheme(R.style.Theme_App_Dark)
            THEME_DRACULA -> setTheme(R.style.Theme_App_Dracula)
            else -> setTheme(R.style.Theme_App_Light)
        }
    }

    /*private fun saveThemeAndRecreate(value: String) {
        val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
        prefs.edit { putString(KEY_THEME, value) }
        recreate()
    }

    private fun findMainFragment(): Fragment? {
        return supportFragmentManager.findFragmentById(android.R.id.content)
    }

    private fun getNavControllerFromMainFragment(mainFragment: Fragment?): NavController? {
        if (mainFragment !is MainFragment) return null
        
        val navHostFragment = mainFragment.childFragmentManager.findFragmentById(AppR.id.main_nav_host)
        return (navHostFragment as? NavHostFragment)?.navController
    }*/

    private fun enableEdgeToEdge() {
        WindowCompat.setDecorFitsSystemWindows(window, false)

        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
    }

    private fun configureStatusBarAppearance() {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
        val theme = prefs.getString(KEY_THEME, THEME_LIGHT)
        
        when (theme) {
            THEME_LIGHT -> {
                windowInsetsController.isAppearanceLightStatusBars = true
                windowInsetsController.isAppearanceLightNavigationBars = true
            }
            THEME_DARK, THEME_DRACULA -> {
                windowInsetsController.isAppearanceLightStatusBars = false
                windowInsetsController.isAppearanceLightNavigationBars = false
            }
        }
    }

    private fun setupWindowInsets() {
        val root = findViewById<View>(android.R.id.content) ?: return

        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            WindowInsetsCompat.CONSUMED
        }
    }
}