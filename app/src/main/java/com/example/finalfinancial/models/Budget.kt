package com.example.finalfinancial.models

data class Budget(
    val amount: Double,
    val month: Int,  // 1-12 for Jan-Dec
    val year: Int
)