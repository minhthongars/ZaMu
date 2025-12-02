package com.minhthong.zamu.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.minhthong.zamu.R
import com.minhthong.zamu.databinding.FragmentMainBinding

class MainFragment: Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private val navController by lazy {
        val host = childFragmentManager.findFragmentById(R.id.main_nav_host) as NavHostFragment
        host.navController
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { destinationId ->
            safeNavigate(destination = destinationId)
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNavigation.setSelectedItem(destination.id)
        }

        navController.currentDestination?.id?.let { destinationId ->
            binding.bottomNavigation.setSelectedItem(destinationId)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun safeNavigate(destination: Int) {
        if (navController.currentDestination?.id == destination) return

        val navOptions = NavOptions.Builder()
            .setLaunchSingleTop(true)
            .setRestoreState(true)
            .setPopUpTo(
                navController.graph.startDestinationId,
                inclusive = false,
                saveState = true
            )
            .build()

        navController.navigate(destination, null, navOptions)
    }
}