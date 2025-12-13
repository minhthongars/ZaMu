package com.minhthong.playlist.data.sharePref

import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.core.content.edit

class ShuffleSharePreference(
    context: Context
) {

    companion object {
        private const val PREFS = "ShuffleSharePreference"
        private const val KEY_SHUFFLE_ENABLE = "KEY_SHUFFLE_ENABLE"
    }

    private val prefs = context.getSharedPreferences(PREFS, MODE_PRIVATE)

    fun setIsEnable(isEnable: Boolean): Boolean {
        prefs.edit { putBoolean(KEY_SHUFFLE_ENABLE, isEnable) }

        return prefs.getBoolean(KEY_SHUFFLE_ENABLE, false)
    }

    fun getIsEnable(): Boolean {
        return prefs.getBoolean(KEY_SHUFFLE_ENABLE, false)
    }

    fun observerOnChangeValueChange(onChange: (Boolean) -> Int) {
        prefs.registerOnSharedPreferenceChangeListener { _, _ ->
            val isEnable = getIsEnable()
            onChange.invoke(isEnable)
        }
    }
}