package com.example.finalfinancial.models

import java.io.Serializable
import java.util.*

data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val amount: Double,
    val category: Category,
    val date: Long = System.currentTimeMillis(),
    val isIncome: Boolean
) : Serializable