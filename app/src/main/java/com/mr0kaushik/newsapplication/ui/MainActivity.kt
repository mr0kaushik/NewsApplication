package com.mr0kaushik.newsapplication.ui

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.mr0kaushik.newsapplication.R
import com.mr0kaushik.newsapplication.db.ArticleDatabase
import com.mr0kaushik.newsapplication.repository.ArticleRepository
import com.mr0kaushik.newsapplication.ui.viewmodels.ArticleViewModel
import com.mr0kaushik.newsapplication.ui.viewmodels.ArticleViewModelProviderFactory
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {
    companion object {
        private const val TAG = "MainActivity"
    }

    lateinit var viewModel: ArticleViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = ArticleRepository(ArticleDatabase(this))
        val viewModelProviderFactory = ArticleViewModelProviderFactory(application, repository)
        viewModel =
            ViewModelProvider(this, viewModelProviderFactory).get(ArticleViewModel::class.java)

        setContentView(R.layout.activity_main)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPreferences?.let { setCountryCode(it) }
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

//        val tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
//        val countryCode = tm.simCountryIso

        bottomNavigationView.setupWithNavController(navHostFragment.findNavController())

    }

    private fun setCountryCode(sharedPreferences: SharedPreferences) {
        viewModel.resetTopHeadlinesPageNumber()
        viewModel.countryCode = sharedPreferences.getString(
            getString(R.string.pref_country_code_key),
            getString(R.string.pref_country_code_default)
        ).toString()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key.equals(getString(R.string.pref_country_code_key))) {
            sharedPreferences?.let {
                setCountryCode(it)
                viewModel.getTopHeadlines()
            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        PreferenceManager.getDefaultSharedPreferences(this)
            .unregisterOnSharedPreferenceChangeListener(this)
    }


}