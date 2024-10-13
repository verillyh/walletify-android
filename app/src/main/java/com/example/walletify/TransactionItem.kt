package com.example.walletify

data class TransactionItem(
    val imageId: Int,
    val category: String,
    val cost: Double,
    val note: String,
    val datetime: String
)
