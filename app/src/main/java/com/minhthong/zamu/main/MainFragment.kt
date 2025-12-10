package com.minhthong.zamu.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.minhthong.core.util.Utils.collectFlowSafely
import com.minhthong.core.player.PlayerManager
import com.minhthong.navigation.Navigation
import com.minhthong.zamu.R
import com.minhthong.zamu.databinding.FragmentMainBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainFragment: Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private val navController by lazy {
        val fragment = childFragmentManager.findFragmentById(R.id.main_nav_host)
        (fragment as NavHostFragment).navController
    }

    @Inject
    internal lateinit var playerManager: PlayerManager

    @Inject
    internal lateinit var navigation: Navigation

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

        setupBackPressHandler()
        setUpAppNavigation()
        setupBottomNavigation()
        setUpCollector()
    }

    private fun setUpAppNavigation() {
        navigation.setNavController(navController)
    }

    private fun setUpCollector() {
        collectFlowSafely {
            playerManager.hasSetPlaylistFlow.collect {
                binding.bottomNavigation.showPlayerItem(isShow = it)
            }
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { destinationId ->
            navigation.navigateTo(destination = destinationId)
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNavigation.setSelectedItem(destination.id)
            binding.title.text = destination.label
        }

        navController.currentDestination?.id?.let { destinationId ->
            binding.bottomNavigation.setSelectedItem(destinationId)
        }

        binding.ivBack.setOnClickListener {
            handleOnBackPressed()
        }
    }

    private fun setupBackPressHandler() {
        activity?.onBackPressedDispatcher?.addCallback(
            owner = this,
            onBackPressedCallback = object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    this@MainFragment.handleOnBackPressed()
                }
            }
        )
    }

    private fun handleOnBackPressed() {
        val currentDestination = navController.currentDestination?.id
        val isAtHome = currentDestination == R.id.homeFragment

        if (!isAtHome) {
            navigation.navigateTo(R.id.homeFragment)
        } else {
            activity?.finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}