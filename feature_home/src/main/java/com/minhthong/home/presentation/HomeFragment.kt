package com.minhthong.home.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.minhthong.core.R
import com.minhthong.core.util.NotificationPermissionHelper
import com.minhthong.core.util.Utils.collectFlowSafely
import com.minhthong.home.databinding.FragmentHomeBinding
import com.minhthong.home.presentation.adapter.HomeAdapter
import com.minhthong.home.presentation.adapter.HomeAdapterClickListener
import com.minhthong.home.presentation.decorator.HomeRecyclerViewItemDecoration
import com.minhthong.navigation.Navigation
import com.minhthong.navigation.Screen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.lang.ref.WeakReference
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment: Fragment() {

    @Inject
    internal lateinit var navigation: Navigation

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    private val notificationPermissionHelper = NotificationPermissionHelper(
        wFragment = WeakReference(this)
    )

    private val audioPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            onAudioPermissionGranted()
        } else {
            onAudioPermissionDenied()
        }
    }


    private val homeListener = object : HomeAdapterClickListener {
        override fun onRetryClick(viewType: Int) {
            viewModel.retry(viewType)
        }

        override fun onTrackClick(trackId: Long) {
            viewModel.handlePlayMusic(trackId = trackId)
        }

        override fun onAddToPlaylistClick(trackId: Long) {
            viewModel.addToPlaylist(trackId = trackId)
        }

        override fun onSaveListingClick() {
            navigation.navigateTo(screen = Screen.PLAYLIST)
        }
    }

    private val homeAdapter = HomeAdapter(
        listener = homeListener
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpViews()
        collectData()
        loadData()
    }

    private fun loadData() {
        viewModel.fetchUserInfo()
        requestAudioPermission()
    }

    private fun collectData() {
        collectFlowSafely {
            viewModel.homeUiItemsFlow.collect {
                homeAdapter.submitList(it)
            }
        }

        viewModel.uiEvent.onEach { event ->
            when(event) {
                is HomeUiEvent.OpenPlayer -> {
                    navigation.navigateTo(screen = Screen.PLAYER)
                }

                is HomeUiEvent.Toast -> {
                    Toast.makeText(
                        requireContext(),
                        getText(event.messageId),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is HomeUiEvent.RequestAudioPermission -> {
                    requestAudioPermission()
                }

                is HomeUiEvent.RequestPostNotificationPermission -> {
                    requestPostNotification()
                }
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun setUpViews() {
        with(binding.recyclerView) {
            itemAnimator = null
            adapter = homeAdapter

            addItemDecoration(
                HomeRecyclerViewItemDecoration(
                    horizontalSpace = resources.getDimensionPixelSize(
                        R.dimen.spacing_md
                    ),
                    verticalSpace = resources.getDimensionPixelSize(
                        R.dimen.spacing_sm2
                    )
                )
            )
        }
    }

    private fun requestAudioPermission() {
        val hasAudioPermission = hasAudioPermission()

        if (hasAudioPermission) {
            onAudioPermissionGranted()
            return
        }

        val audioPermission = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                Manifest.permission.READ_MEDIA_AUDIO
            }
            else -> {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }
        }

        audioPermissionLauncher.launch(audioPermission)
    }

    private fun hasAudioPermission(): Boolean {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ->
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_MEDIA_AUDIO
                ) == PackageManager.PERMISSION_GRANTED

            else ->
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPostNotification() {
        notificationPermissionHelper.requestPermissionAndStartService(
            onGranted = {
                viewModel.playMusic()
            },
            onDenied = {
                onPostNotificationPermissionDenied()
            }
        )
    }

    private fun onAudioPermissionGranted() {
        viewModel.getDeviceTrack()
    }

    private fun onAudioPermissionDenied() {
        viewModel.showPermissionDenyError()
    }

    private fun onPostNotificationPermissionDenied() {
        viewModel.showToast(R.string.post_notification_denied_msg)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        notificationPermissionHelper.cleanup()
        _binding = null
    }
}