package com.example.walletify.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TransactionRepository(private val transactionDao: TransactionDao) {
    private val _transactionStateFlow = MutableStateFlow<List<Transaction>?>(null)
    val transactionStateFlow: StateFlow<List<Transaction>?> = _transactionStateFlow.asStateFlow()
    val repositoryScope = CoroutineScope(Dispatchers.IO)

    init {
        // Update transaction state to guest's transactions as default
        updateTransactionFlow(1)
    }

    fun getUserTransactions(userId: Long): Flow<List<Transaction>> {
        return transactionDao.getUserTransactions(userId)
    }

    fun updateTransactionFlow(userId: Long) {
        repositoryScope.coroutineContext.cancelChildren()

        repositoryScope.launch {
            getUserTransactions(userId).collect { transactions ->
                _transactionStateFlow.value = transactions
            }
        }
    }

    suspend fun addTransaction(transaction: Transaction, walletRepository: WalletRepository, currentBalance: Double, currentExpense: Double, currentIncome: Double): Boolean {
        // Add transaction
        val transactionId = transactionDao.addTransaction(transaction)

        if (transactionId < 0) {
            return false
        }

        var result = -1

        // Update wallet
        when (transaction.type) {
            'E' -> {
                result = walletRepository.updateWallet(
                    balance = currentBalance - transaction.amount,
                    expense = currentExpense + transaction.amount,
                    income = currentIncome,
                    userId = transaction.userId
                )
            }
            'I' -> {
                result = walletRepository.updateWallet(
                    balance = currentBalance + transaction.amount,
                    expense = currentExpense,
                    income = currentIncome + transaction.amount,
                    userId = transaction.userId
                )
            }
        }

        return result > 0
    }
}