package com.example.finalfinancial.Repositories

import android.content.Context
import android.content.SharedPreferences

class PrefUtil(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    var currency: String?
        get() = prefs.getString(KEY_CURRENCY, DEFAULT_CURRENCY)
        set(value) = prefs.edit().putString(KEY_CURRENCY, value).apply()

    fun getMonthlyBudget(): Double {
        return prefs.getFloat(KEY_MONTHLY_BUDGET, 0f).toDouble()
    }

    fun setMonthlyBudget(budget: Double) {
        prefs.edit().putFloat(KEY_MONTHLY_BUDGET, budget.toFloat()).apply()
    }

    var shouldShowBudgetWarning: Boolean
        get() = prefs.getBoolean(KEY_SHOW_BUDGET_WARNING, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_BUDGET_WARNING, value).apply()

    var lastBudgetWarningThreshold: Int
        get() = prefs.getInt(KEY_LAST_BUDGET_WARNING, 0)
        set(value) = prefs.edit().putInt(KEY_LAST_BUDGET_WARNING, value).apply()

    companion object {
        private const val PREF_NAME = "money_mind_prefs"
        private const val KEY_CURRENCY = "currency"
        private const val KEY_MONTHLY_BUDGET = "monthly_budget"
        private const val KEY_SHOW_BUDGET_WARNING = "show_budget_warning"
        private const val KEY_LAST_BUDGET_WARNING = "last_budget_warning"

        private const val DEFAULT_CURRENCY = "$"
    }
}
