package com.minhthong.zamu.main

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.minhthong.zamu.R
import com.minhthong.zamu.databinding.ViewCustomBottomNavBinding

class BottomNavigationBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding: ViewCustomBottomNavBinding
    private var selectedItemId: Int = 0
    private var onItemSelectedListener: ((Int) -> Unit)? = null

    private val navItems = listOf(
        NavItem(
            destinationId = R.id.homeFragment,
            viewId = R.id.nav_item_home,
            iconResId = R.drawable.ic_home,
            label = "Home",
        ),
        NavItem(
            destinationId = R.id.playerFragment,
            viewId = R.id.nav_item_player,
            iconResId = R.drawable.ic_music,
            label = "Player"
        ),
        NavItem(
            destinationId = R.id.playlistFragment,
            viewId = R.id.nav_item_playlist,
            iconResId = R.drawable.ic_playlist,
            label = "Playlist"
        ),
        NavItem(
            destinationId = R.id.settingsFragment,
            viewId = R.id.nav_item_settings,
            iconResId = R.drawable.ic_setting,
            label = "Settings"
        )
    )

    init {
        orientation = HORIZONTAL
        binding = ViewCustomBottomNavBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )
        
        setupNavItems()
        applyWindowInsets()
    }

    private fun setupNavItems() {
        navItems.forEach { navItem ->
            val itemView = binding.root.findViewById<View>(navItem.viewId)
            itemView.setOnClickListener {
                setSelectedItem(navItem.destinationId)
                onItemSelectedListener?.invoke(navItem.destinationId)
            }

            val iconView = itemView.findViewById<ImageView>(R.id.nav_item_icon)
            
            iconView?.setImageResource(navItem.iconResId)
        }

        setSelectedItem(R.id.homeFragment)
    }

    fun setSelectedItem(destinationId: Int) {
        if (selectedItemId == destinationId) return

        val previousItem = navItems.find { it.destinationId == selectedItemId }
        val newItem = navItems.find { it.destinationId == destinationId }

        previousItem?.let { deselectItem(it) }
        newItem?.let { selectItem(it) }

        selectedItemId = destinationId
    }

    private fun selectItem(navItem: NavItem) {
        val itemView = binding.root.findViewById<View>(navItem.viewId) ?: return
        val iconView = itemView.findViewById<ImageView>(R.id.nav_item_icon)

        itemView.isSelected = true
        iconView?.isSelected = true
    }

    private fun deselectItem(navItem: NavItem) {
        val itemView = binding.root.findViewById<View>(navItem.viewId) ?: return
        val iconView = itemView.findViewById<ImageView>(R.id.nav_item_icon)

        itemView.isSelected = false
        iconView?.isSelected = false
    }

    fun setOnItemSelectedListener(listener: (Int) -> Unit) {
        onItemSelectedListener = listener
    }

    fun showPlayerItem(isShow: Boolean) {
        binding.navItemPlayer.root.isVisible = isShow
    }

    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            setPadding(
                paddingLeft,
                paddingTop,
                paddingRight,
                systemBars.bottom
            )
            WindowInsetsCompat.CONSUMED
        }
    }

    private data class NavItem(
        val destinationId: Int,
        val viewId: Int,
        val iconResId: Int,
        val label: String,
    )
}

