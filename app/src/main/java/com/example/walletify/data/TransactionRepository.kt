package com.example.walletify.data

import com.example.walletify.TransactionCategory
import com.example.walletify.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TransactionRepository(
    private val transactionDao: TransactionDao,
    private val activeWalletStateFlow: StateFlow<Wallet?>
) {
    private val _transactionStateFlow = MutableStateFlow<List<Transaction>?>(null)
    val transactionStateFlow: StateFlow<List<Transaction>?> = _transactionStateFlow.asStateFlow()
    val repositoryScope = CoroutineScope(Dispatchers.IO)

    init {
        // Update transaction state to current wallet
        CoroutineScope(Dispatchers.IO).launch {
            activeWalletStateFlow.collect { state ->
                if (state != null) {
                    updateTransactionFlow(state.id)
                }
            }
        }
    }

    private fun getWalletTransactions(walletId: Long): Flow<List<Transaction>> {
        return transactionDao.getWalletTransactions(walletId)
    }

    private fun updateTransactionFlow(walletId: Long) {
        repositoryScope.coroutineContext.cancelChildren()

        // Use background thread since we're doing database calls
        repositoryScope.launch {
            getWalletTransactions(walletId).collect { transactions ->
                _transactionStateFlow.value = transactions
            }
        }
    }

    suspend fun addTransaction(transaction: Transaction): Boolean {
        // Add transaction
        val transactionId = transactionDao.addTransaction(transaction)
        return transactionId >= 0
    }

    suspend fun transfer(amount: Double, fromWalletId: Long, toWalletId: Long, fromWalletName: String, toWalletName: String): Boolean {
        // Use withContext to be able to return
        return withContext(Dispatchers.IO) {
            val fromTransaction = Transaction(
                category = TransactionCategory.TRANSFER,
                amount = amount,
                type = TransactionType.SOURCE_TRANSFER,
                note = "Transfer to $toWalletName",
                walletId = fromWalletId,
            )
            val toTransaction = Transaction(
                category = TransactionCategory.TRANSFER,
                amount = amount,
                type = TransactionType.DESTINATION_TRANSFER,
                note = "Transfer from $fromWalletName",
                walletId = toWalletId,
            )

            // Transaction on source wallet
            val entryId1 = transactionDao.addTransaction(fromTransaction)

            // If first transaction failed, return false
            if (entryId1 < 0) {
                return@withContext false
            }

            // Transaction on destination wallet
            val entryId2 = transactionDao.addTransaction(toTransaction)

            entryId2 >= 0
        }
    }
}