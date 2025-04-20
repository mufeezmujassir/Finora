package com.example.finalfinancial.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.finalfinancial.R
import com.example.finalfinancial.databinding.ActivityMainBinding
import com.example.finalfinancial.fragments.DashboardFragment
import com.example.finalfinancial.fragments.SettingsFragment
import com.example.finalfinancial.fragments.StatisticsFragment
import com.example.finalfinancial.fragments.TransactionsFragment
import com.example.finalfinancial.utils.BudgetAlarmReceiver
import com.example.finalfinancial.utils.NotificationUtil
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up notification channel
        NotificationUtil.createNotificationChannel(this)

        // Set up bottom navigation
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    loadFragment(DashboardFragment())
                    true
                }
                R.id.nav_transactions -> {
                    loadFragment(TransactionsFragment())
                    true
                }
                R.id.nav_statistics -> {
                    loadFragment(StatisticsFragment())
                    true
                }
                R.id.nav_settings -> {
                    loadFragment(SettingsFragment())
                    true
                }
                else -> false
            }
        }

        // Set default fragment
        if (savedInstanceState == null) {
            binding.bottomNavigation.selectedItemId = R.id.nav_dashboard
        }

        // Check budget status and update notifications
        NotificationUtil.updateBudgetNotificationIfNeeded(this)
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}