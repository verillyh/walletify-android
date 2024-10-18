package com.example.walletify.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.Flow

class TransactionsViewModel(application: Application): AndroidViewModel(application) {
    lateinit var allUserTransactions: Flow<List<Transaction>>
    val repository: TransactionRepository

    init {
        val transactionDao = WalletifyDatabase.getDatabase(application).transactionDao()
        repository = TransactionRepository(transactionDao)
    }

    fun getUserTransactions(userId: Long): Flow<List<Transaction>> {
        allUserTransactions = repository.getUserTransactions(userId)
        return allUserTransactions
    }

    suspend fun addUserTransaction(transaction: Transaction, walletRepository: WalletRepository, currentBalance: Double, currentExpense: Double, currentIncome: Double): Boolean {
        // Add transaction
        val success = repository.addTransaction(transaction, walletRepository, currentBalance, currentExpense, currentIncome)
        if (!success) {
            return false
        }
        return true
    }
}