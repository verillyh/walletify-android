package com.example.walletify.data

import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val transactionDao: TransactionDao) {
    fun getUserTransactions(userId: Long): Flow<List<Transaction>> {
        return transactionDao.getUserTransactions(userId)
    }

    suspend fun addTransaction(transaction: Transaction): Long {
        return transactionDao.addTransaction(transaction)
    }
}