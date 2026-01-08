package com.minhthong.zamu.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import com.minhthong.core.model.ControllerState
import com.minhthong.core.util.Utils.collectFlowSafely
import com.minhthong.navigation.Navigation
import com.minhthong.navigation.Screen
import com.minhthong.zamu.R
import com.minhthong.player.R as PR
import com.minhthong.zamu.databinding.FragmentMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainFragment: Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private val navController by lazy {
        val fragment = childFragmentManager.findFragmentById(R.id.main_nav_host)
        (fragment as NavHostFragment).navController
    }

    private val viewModel: MainViewModel by viewModels()

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
        handleNavigation()
    }

    private fun handleNavigation() {
        val screenId = activity?.intent?.getIntExtra(MainActivity.NAVIGATE_SCREEN_ID, 0)
        if (screenId != null && screenId != 0) {
            navigation.navigateTo(screenId)
        }
    }

    private fun setUpAppNavigation() {
        navigation.setNavController(navController)
    }

    private fun setUpCollector() {
        collectFlowSafely {
            viewModel.controllerInfoFlow.collect { info ->
                binding.bottomNavigation.showPlayerItem(isShow = info != null)
                bindMiniPlayer(controllerState = info)
            }
        }

        collectFlowSafely {
            viewModel.showMiniPlayerFlow.collect { isShow ->
                binding.llMiniPlayer.isVisible = isShow
            }
        }

        viewModel.observerPlaylist()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { destinationId ->
            navigation.navigateTo(destination = destinationId)
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNavigation.setSelectedItem(destination.id)
            binding.title.text = destination.label

            viewModel.setCurrentScreen(screenId = destination.id)
        }

        navController.currentDestination?.id?.let { destinationId ->
            binding.bottomNavigation.setSelectedItem(destinationId)
        }

        binding.ivBack.setOnClickListener {
            handleOnBackPressed()
        }
    }

    private fun bindMiniPlayer(controllerState: ControllerState?) {
        if (controllerState == null) return

        val playingItem = controllerState.playingItem
        binding.tvTrackTitle.text = playingItem.title
        binding.tvTrackTitle.isSelected = true

        binding.ivAvatar.setImageBitmap(
            playingItem.avatarImage
        )

        binding.ivPlay.setImageResource(
            if (controllerState.isPlaying) {
                PR.drawable.ic_player_pause
            } else {
                PR.drawable.ic_player_playing
            }
        )

        binding.ivPlay.setOnClickListener {
            viewModel.play()
        }

        binding.ivMoveToNext.setOnClickListener {
            viewModel.moveToNext()
        }

        binding.ivMoveToPrev.setOnClickListener {
            viewModel.moveToPrev()
        }

        binding.llMiniPlayer.setOnClickListener {
            navigation.navigateTo(Screen.PLAYER)
        }

        binding.tvTrackTitle.setOnClickListener {
            navigation.navigateTo(Screen.PLAYER)
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