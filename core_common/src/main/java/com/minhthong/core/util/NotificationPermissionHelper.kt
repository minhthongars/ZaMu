package com.minhthong.core.util

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.minhthong.core.service.MusicService
import com.minhthong.core.service.PlaybackService
import java.lang.ref.WeakReference

class NotificationPermissionHelper(
    private val wFragment: WeakReference<Fragment>
) {

    private val fragment get() = wFragment.get()!!

    private val postNotificationPermissionLauncher = fragment.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startPlaybackService()
            onGrantedCallback?.invoke()
        } else {
            onDeniedCallback?.invoke()
        }
    }

    private var onGrantedCallback: (() -> Unit)? = null
    private var onDeniedCallback: (() -> Unit)? = null

    fun requestPermissionAndStartService(
        onGranted: () -> Unit = {},
        onDenied: () -> Unit = {}
    ) {
        onGrantedCallback = onGranted
        onDeniedCallback = onDenied

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                fragment.requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (hasPermission) {
                startPlaybackService()
                onGranted()
            } else {
                postNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            startPlaybackService()
            onGranted()
        }
    }

    private fun startPlaybackService() {
        //val intent = Intent(fragment.requireActivity(), PlaybackService::class.java)
        val intent = Intent(fragment.requireActivity(), MusicService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            fragment.requireActivity().startForegroundService(intent)
        } else {
            fragment.requireActivity().startService(intent)
        }
    }

    fun cleanup() {
        wFragment.clear()
        onGrantedCallback = null
        onDeniedCallback = null
    }
}



