package com.example.finalfinancial.models

import androidx.annotation.DrawableRes
import com.example.finalfinancial.R

enum class Category(val displayName: String, @DrawableRes val iconResId: Int) {
    // Expense categories
    FOOD("Food", R.drawable.ic_food),
    GROCERIES("Groceries", R.drawable.ic_grocery),
    TRANSPORTATION("Transportation", R.drawable.ic_transportation),
        ENTERTAINMENT("Entertainment", R.drawable.ic_entertainment),
    SHOPPING("Shopping", R.drawable.ic_shopping),
    HEALTH("Health", R.drawable.ic_health),
    HOUSING("Housing", R.drawable.ic_housing),
    UTILITIES("Utilities", R.drawable.ic_utilities),
    EDUCATION("Education", R.drawable.ic_education),
    TRAVEL("Travel", R.drawable.ic_travel),
    OTHER_EXPENSE("Other Expense", R.drawable.ic_other),

    // Income categories
    SALARY("Salary", R.drawable.ic_salary),
    FREELANCE("Freelance", R.drawable.ic_freelance),
    INVESTMENT("Investment", R.drawable.ic_investment),
    GIFT("Gift", R.drawable.ic_gift),
    REFUND("Refund", R.drawable.ic_refund),
    OTHER_INCOME("Other Income", R.drawable.ic_other);

    companion object {
        fun getExpenseCategories(): List<Category> {
            return listOf(
                FOOD, GROCERIES, TRANSPORTATION, ENTERTAINMENT, SHOPPING,
                HEALTH, HOUSING, UTILITIES, EDUCATION, TRAVEL, OTHER_EXPENSE
            )
        }

        fun getIncomeCategories(): List<Category> {
            return listOf(
                SALARY, FREELANCE, INVESTMENT, GIFT, REFUND, OTHER_INCOME
            )
        }
    }
}