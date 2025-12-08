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
import com.minhthong.core.R
import com.minhthong.core.util.Utils.collectFlowSafely
import com.minhthong.home.databinding.FragmentHomeBinding
import com.minhthong.home.presentation.adapter.HomeAdapter
import com.minhthong.home.presentation.adapter.HomeAdapterClickListener
import com.minhthong.home.presentation.decorator.HomeRecyclerViewItemDecoration
import com.minhthong.navigation.Navigation
import com.minhthong.navigation.Screen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment: Fragment() {

    @Inject
    internal lateinit var navigation: Navigation

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    private val homeListener = object : HomeAdapterClickListener {
        override fun onRetryClick(viewType: Int) {
            viewModel.retry(viewType)
        }

        override fun onTrackClick(trackId: Long) {
            viewModel.onTrackClick(trackId = trackId)
        }

        override fun onSaveClick(trackId: Long) {
        }

        override fun onSaveListingClick() {
            navigation.navigateTo(Screen.FAVORITE)
        }
    }

    private val homeAdapter = HomeAdapter(
        listener = homeListener
    )

    private val permissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            val granted = result.values.all { it }
            if (granted) {
                onPermissionGranted()
            } else {
                onPermissionDenied()
            }
        }

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
            viewModel.adapterItemsFlow.collect {
                homeAdapter.submitList(it)
            }
        }

        collectFlowSafely {
            viewModel.uiEvent.collect { event ->
                when(event) {
                    is HomeUiEvent.OpenPlayer -> {
                        navigation.navigateTo(screen = Screen.PLAYER)
                    }

                    is HomeUiEvent.Toast -> {
                        Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
                    }

                    is HomeUiEvent.RequestAudioPermission -> {
                        requestAudioPermission()
                    }
                }
            }
        }
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
        if (hasAudioPermission()) {
            onPermissionGranted()
            return
        }

        val permissions = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                arrayOf(Manifest.permission.READ_MEDIA_AUDIO)
            }
            else -> {
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        permissionLauncher.launch(permissions)
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

    private fun onPermissionGranted() {
        viewModel.getDeviceTrack()
    }

    private fun onPermissionDenied() {
        viewModel.showPermissionDenyError()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}