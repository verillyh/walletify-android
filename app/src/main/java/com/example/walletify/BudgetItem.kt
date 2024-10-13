package com.example.walletify

data class BudgetItem(
    val imageId: Int,
    val category: String,
    val totalBudget: Double,
    val budgetLeft: Double
)
