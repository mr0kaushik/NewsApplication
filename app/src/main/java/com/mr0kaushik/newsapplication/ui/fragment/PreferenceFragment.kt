package com.mr0kaushik.newsapplication.ui.fragment

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.CheckBoxPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.mr0kaushik.newsapplication.R

class PreferenceFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_news)


        for (i in 0 until preferenceScreen.preferenceCount) {
            val pref = preferenceScreen.getPreference(i)
            if (pref !is CheckBoxPreference)
                preferenceScreen.sharedPreferences.getString(pref.key, "")
                    ?.let { setPreferenceSummary(pref, it) }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)

    }

    override fun onDestroy() {
        super.onDestroy()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        key?.let {
            this.findPreference<Preference>(key)?.let { pref ->
                if (pref !is CheckBoxPreference)
                    preferenceScreen.sharedPreferences.getString(pref.key, "")
                        ?.let { value -> setPreferenceSummary(pref, value) }
            }
        }
    }

    private fun setPreferenceSummary(preference: Preference, value: String) {
        when (preference) {
            is ListPreference -> {
                val index = preference.findIndexOfValue(value)
                if (index >= 0) {
                    preference.setSummary(preference.entries[index])
                }
            }
        }
    }

}