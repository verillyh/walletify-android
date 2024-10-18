package com.example.walletify.data

import com.example.walletify.TransactionCategory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TransactionRepository(private val transactionDao: TransactionDao, private val walletViewModel: WalletViewModel) {
    private val _transactionStateFlow = MutableStateFlow<List<Transaction>?>(null)
    val transactionStateFlow: StateFlow<List<Transaction>?> = _transactionStateFlow.asStateFlow()
    val repositoryScope = CoroutineScope(Dispatchers.IO)
    val repositoryMainScope = CoroutineScope(Dispatchers.Main)

    init {
        // Update transaction state to current wallet
        repositoryMainScope.launch {
            walletViewModel.uiState.collect { state ->
               updateTransactionFlow(state.id)
            }
        }
    }

    fun getWalletTransactions(walletId: Long): Flow<List<Transaction>> {
        return transactionDao.getWalletTransactions(walletId)
    }

    fun updateTransactionFlow(walletId: Long) {
        repositoryScope.coroutineContext.cancelChildren()

        repositoryScope.launch {
            getWalletTransactions(walletId).collect { transactions ->
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
                    walletId = transaction.walletId
                )
            }
            'I' -> {
                result = walletRepository.updateWallet(
                    balance = currentBalance + transaction.amount,
                    expense = currentExpense,
                    income = currentIncome + transaction.amount,
                    walletId = transaction.walletId
                )
            }
        }

        return result > 0
    }

    suspend fun transfer(amount: Double, fromWalletName: String, toWalletName: String, userId: Long): Boolean {
        return withContext(Dispatchers.IO) {
            // Get wallet ids
            val fromWalletId = walletViewModel.repository.getWalletId(userId, fromWalletName)
            val toWalletId = walletViewModel.repository.getWalletId(userId, toWalletName)

            // Transaction on source wallet
            transactionDao.addTransaction(
                Transaction(
                    category = TransactionCategory.TRANSFER,
                    amount = amount,
                    type = 'F',
                    note = "Transfer to $toWalletName",
                    walletId = fromWalletId
                )
            )
            // Transaction on destination wallet
            transactionDao.addTransaction(
                Transaction(
                    category = TransactionCategory.TRANSFER,
                    amount = amount,
                    type = 'T',
                    note = "Transfer from $fromWalletName",
                    walletId = toWalletId,
                )
            )

            // Update wallet values
            // TODO: Maybe have the wallet state change automatically on every transaction?
            walletViewModel.uiState.value.apply {
                walletViewModel.repository.updateWallet(
                    balance = balance - amount,
                    expense = expense + amount,
                    income = income,
                    walletId = fromWalletId
                )
                walletViewModel.repository.updateWallet(
                    balance = balance + amount,
                    expense = expense,
                    income = income + amount,
                    walletId = toWalletId
                )
            }

            true
        }
    }
}