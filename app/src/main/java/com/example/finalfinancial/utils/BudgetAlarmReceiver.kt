package com.example.finalfinancial.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.finalfinancial.utils.NotificationUtil
import java.util.*

class BudgetAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                // Reset notification tracking when device restarts
                NotificationUtil.resetBudgetWarningThreshold(context)
            }
            ACTION_CHECK_BUDGET -> {
                // Check and update budget notifications
                NotificationUtil.updateBudgetNotificationIfNeeded(context)
            }
            ACTION_RESET_MONTHLY -> {
                // Reset threshold at start of month
                NotificationUtil.resetBudgetWarningThreshold(context)
            }
        }
    }

    companion object {
        const val ACTION_CHECK_BUDGET = "com.example.moneymind.CHECK_BUDGET"
        const val ACTION_RESET_MONTHLY = "com.example.moneymind.RESET_MONTHLY"
    }
}
