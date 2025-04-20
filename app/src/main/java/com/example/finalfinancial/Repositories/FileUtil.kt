package com.example.finalfinancial.Repositories

import android.content.Context
import android.net.Uri
import com.example.finalfinancial.models.Transaction
import com.example.finalfinancial.Repositories.TransactionRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*

object FileUtil {

    private const val BACKUP_FILENAME = "moneymind_backup_"
    private val gson = Gson()
    private val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())

    fun exportTransactions(context: Context, transactions: List<Transaction>): String {
        val timestamp = dateFormat.format(Date())
        val fileName = "$BACKUP_FILENAME$timestamp.json"
        val transactionsJson = gson.toJson(transactions)

        context.openFileOutput(fileName, Context.MODE_PRIVATE).use {
            it.write(transactionsJson.toByteArray())
        }

        return fileName
    }

    fun importTransactions(context: Context, uri: Uri): List<Transaction>? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val reader = BufferedReader(InputStreamReader(inputStream))
                val stringBuilder = StringBuilder()
                var line: String?

                while (reader.readLine().also { line = it } != null) {
                    stringBuilder.append(line)
                }

                val type = object : TypeToken<List<Transaction>>() {}.type
                gson.fromJson<List<Transaction>>(stringBuilder.toString(), type)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun restoreFromBackup(
        context: Context,
        transactionRepository: TransactionRepository,
        uri: Uri
    ): Boolean {
        val transactions = importTransactions(context, uri)

        return if (transactions != null) {
            // Clear existing data and add imported transactions
            transactionRepository.clearAllData()
            transactions.forEach { transactionRepository.addTransaction(it) }
            true
        } else {
            false
        }
    }

    fun getBackupFilename(): String {
        val timestamp = dateFormat.format(Date())
        return "$BACKUP_FILENAME$timestamp.json"
    }
}
