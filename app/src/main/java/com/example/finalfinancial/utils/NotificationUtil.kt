package com.example.finalfinancial.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.finalfinancial.activities.MainActivity
import com.example.finalfinancial.R
import com.example.finalfinancial.Repositories.PrefUtil
import com.example.finalfinancial.Repositories.TransactionRepository

object NotificationUtil {

    private const val CHANNEL_ID = "budget_notifications"
    private const val NOTIFICATION_ID = 1001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.notification_channel_name)
            val descriptionText = context.getString(R.string.notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showBudgetWarningNotification(
        context: Context,
        title: String,
        content: String
    ) {
        // Create an intent to open the app when notification is tapped
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun updateBudgetNotificationIfNeeded(context: Context) {
        val prefUtil = PrefUtil(context)
        val transactionRepository = TransactionRepository.getInstance(context)

        if (!prefUtil.shouldShowBudgetWarning) return

        val monthlyBudget = prefUtil.getMonthlyBudget()
        if (monthlyBudget <= 0) return

        val monthlyExpenses = transactionRepository.getTotalExpensesForMonth()
        val percentUsed = (monthlyExpenses / monthlyBudget * 100).toInt()
        val lastWarningThreshold = prefUtil.lastBudgetWarningThreshold

        // Check different threshold levels and notify if crossed
        when {
            percentUsed >= 100 && lastWarningThreshold < 100 -> {
                showBudgetWarningNotification(
                    context,
                    context.getString(R.string.budget_exceeded_title),
                    context.getString(R.string.budget_exceeded_message)
                )
                prefUtil.lastBudgetWarningThreshold = 100
            }
            percentUsed >= 90 && lastWarningThreshold < 90 -> {
                showBudgetWarningNotification(
                    context,
                    context.getString(R.string.budget_warning_title),
                    context.getString(R.string.budget_90_percent_message)
                )
                prefUtil.lastBudgetWarningThreshold = 90
            }
            percentUsed >= 75 && lastWarningThreshold < 75 -> {
                showBudgetWarningNotification(
                    context,
                    context.getString(R.string.budget_warning_title),
                    context.getString(R.string.budget_75_percent_message)
                )
                prefUtil.lastBudgetWarningThreshold = 75
            }
        }
    }

    // Reset the warning threshold at the beginning of a new month
    fun resetBudgetWarningThreshold(context: Context) {
        PrefUtil(context).lastBudgetWarningThreshold = 0
    }
}