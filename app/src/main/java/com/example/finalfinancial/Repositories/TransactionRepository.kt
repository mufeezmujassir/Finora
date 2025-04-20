package com.example.finalfinancial.Repositories

import android.content.Context
import android.content.SharedPreferences
import com.example.finalfinancial.models.Category
import com.example.finalfinancial.models.Transaction
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*
import java.util.Calendar.*
import kotlin.collections.ArrayList

class TransactionRepository private constructor(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        TRANSACTIONS_PREF_NAME, Context.MODE_PRIVATE
    )
    private val gson = Gson()

    fun getAllTransactions(): List<Transaction> {
        val transactionsJson = sharedPreferences.getString(KEY_TRANSACTIONS, null) ?: return ArrayList()
        val type = object : TypeToken<List<Transaction>>() {}.type
        return gson.fromJson(transactionsJson, type) ?: ArrayList()
    }

    fun addTransaction(transaction: Transaction) {
        val transactions = getAllTransactions().toMutableList()
        transactions.add(transaction)
        saveTransactions(transactions)
    }

    fun updateTransaction(transaction: Transaction) {
        val transactions = getAllTransactions().toMutableList()
        val index = transactions.indexOfFirst { it.id == transaction.id }
        if (index != -1) {
            transactions[index] = transaction
            saveTransactions(transactions)
        }
    }

    fun deleteTransaction(transactionId: String) {
        val transactions = getAllTransactions().toMutableList()
        transactions.removeAll { it.id == transactionId }
        saveTransactions(transactions)
    }

    private fun saveTransactions(transactions: List<Transaction>) {
        val transactionsJson = gson.toJson(transactions)
        sharedPreferences.edit().putString(KEY_TRANSACTIONS, transactionsJson).apply()
    }

    fun getTransactionsForMonth(year: Int = -1, month: Int = -1): List<Transaction> {
        val calendar = Calendar.getInstance()
        val currentYear = if (year == -1) calendar.get(YEAR) else year
        val currentMonth = if (month == -1) calendar.get(MONTH) else month

        val startOfMonth = Calendar.getInstance().apply {
            set(currentYear, currentMonth, 1, 0, 0, 0)
            set(MILLISECOND, 0)
        }.timeInMillis

        val endOfMonth = Calendar.getInstance().apply {
            set(currentYear, currentMonth, getActualMaximum(DAY_OF_MONTH), 23, 59, 59)
            set(MILLISECOND, 999)
        }.timeInMillis

        return getAllTransactions().filter {
            it.date in startOfMonth..endOfMonth
        }
    }

    fun getTotalIncomeForMonth(year: Int = -1, month: Int = -1): Double {
        return getTransactionsForMonth(year, month)
            .filter { it.isIncome }
            .sumOf { it.amount }
    }

    fun getTotalExpensesForMonth(year: Int = -1, month: Int = -1): Double {
        return getTransactionsForMonth(year, month)
            .filter { !it.isIncome }
            .sumOf { it.amount }
    }

    fun getExpensesByCategory(year: Int = -1, month: Int = -1): Map<Category, Double> {
        val expensesByCategory = mutableMapOf<Category, Double>()

        getTransactionsForMonth(year, month)
            .filter { !it.isIncome }
            .forEach { transaction ->
                val currentAmount = expensesByCategory[transaction.category] ?: 0.0
                expensesByCategory[transaction.category] = currentAmount + transaction.amount
            }

        return expensesByCategory
    }

    fun clearAllData() {
        sharedPreferences.edit().remove(KEY_TRANSACTIONS).apply()
    }

    companion object {
        private const val TRANSACTIONS_PREF_NAME = "transactions_prefs"
        private const val KEY_TRANSACTIONS = "all_transactions"

        @Volatile
        private var instance: TransactionRepository? = null

        fun getInstance(context: Context): TransactionRepository {
            return instance ?: synchronized(this) {
                instance ?: TransactionRepository(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}