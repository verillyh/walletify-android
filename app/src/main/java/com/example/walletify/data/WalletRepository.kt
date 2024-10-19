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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WalletRepository(
    private val walletDao: WalletDao,
    transactionDao: TransactionDao,
    private val userStateFlow: StateFlow<User?>
) {
    private val transactionRepository: TransactionRepository
    private val _activeWalletStateFlow = MutableStateFlow<Wallet?>(null)
    private val _transactionStateFlow = MutableStateFlow<List<Transaction>?>(null)
    private val _walletStateFlow = MutableStateFlow<List<Wallet>?>(null)
    val activeWalletStateFlow: StateFlow<Wallet?> = _activeWalletStateFlow.asStateFlow()
    val transactionStateFlow: StateFlow<List<Transaction>?> = _transactionStateFlow.asStateFlow()
    val walletStateFlow: StateFlow<List<Wallet>?> = _walletStateFlow.asStateFlow()
    val walletScope = CoroutineScope(Dispatchers.IO)
    // TODO: Maybe use wallet scope instead? Or should use Dispatchers.Default?
    val transactionScope = CoroutineScope(Dispatchers.IO)
    // Use default, since we're not doing any database calls, nor updating UI
    val activeWalletScope = CoroutineScope(Dispatchers.Default)


    init {
        transactionRepository = TransactionRepository(transactionDao, activeWalletStateFlow)

        // Listen for user changes
        CoroutineScope(Dispatchers.IO).launch {
            userStateFlow.collect {
                if (it != null) {
                    updateWalletState(it.id)
                }
            }
        }

        // Listen for new transactions
        transactionScope.launch {
            transactionRepository.transactionStateFlow.collect { transactionLists ->
                _transactionStateFlow.value = transactionLists
            }
        }
    }

    private fun getWalletsFromUserId(userId: Long): Flow<List<Wallet>> {
       val wallet = walletDao.getUserWallets(userId)

       return wallet
    }

    // Active wallet -> Use when changing wallet
    fun updateActiveWalletState(walletName: String) {
        activeWalletScope.coroutineContext.cancelChildren()

        // Search wallet state, and collect wallet based on name
        activeWalletScope.launch {
            _walletStateFlow.collect { state ->
                _activeWalletStateFlow.value = _walletStateFlow.first()?.find { it.walletName == walletName }
            }
        }
    }

    // Wallet list -> Use when changing user
    private fun updateWalletState(userId: Long) {
        // Remove previous flow processes
        walletScope.coroutineContext.cancelChildren()
        activeWalletScope.coroutineContext.cancelChildren()

        // Collect wallet lists from user ID
        walletScope.launch {
            getWalletsFromUserId(userId).collect { wallet ->
                _walletStateFlow.value = wallet
            }
        }
        updateActiveWalletState("Main")
    }


    private fun getWalletId(walletName: String): Long {
        // Return found wallet id, else return -1
        _walletStateFlow.value?.let { wallet ->
            return wallet.find { it.walletName == walletName}?.id ?: -1
        }

        // Default case
        return -1
    }

    suspend fun addWallet(wallet: Wallet): Boolean {
        val walletId = walletDao.addWallet(wallet)
        if (walletId < 0) {
            return false
        }

        // Add initial balance transaction
        val success = addTransaction(Transaction(
            category = TransactionCategory.INITIAL_BALANCE,
            amount = wallet.balance,
            type = TransactionType.INITIAL_BALANCE,
            note = "Initial Balance",
            walletId = walletId
        ))

        return success
    }

    private suspend fun updateWallet(balance: Double, expense: Double, income: Double, walletId: Long): Boolean {
        // Update wallet in database
        val affectedRows = walletDao.updateWallet(
            balance = balance,
            expense = expense,
            income = income,
            id = walletId
        )

        return affectedRows > 0
    }

    suspend fun transfer(amount: Double, fromWalletName: String, toWalletName: String): Boolean {
        // Get ids
        val fromWalletId = getWalletId(fromWalletName)
        val toWalletId = getWalletId(toWalletName)

        // Process transfer on each wallet
        val success = transactionRepository.transfer(amount, fromWalletId, toWalletId, fromWalletName, toWalletName)

        if (!success) {
            return false
        }

        // TODO: Maybe have this automatically done when there's an entry in transaction?
        // Update wallet after transaction
        activeWalletStateFlow.value?.apply {
            val updateSourceWalletSuccess = updateWallet(
                balance = balance - amount,
                expense = expense + amount,
                income = income,
                walletId = fromWalletId
            )

            if (!updateSourceWalletSuccess) {
                return false
            }

            val updateDestWalletSuccess = updateWallet(
                balance = balance + amount,
                expense = expense,
                income = income + amount,
                walletId = toWalletId
            )
            return updateDestWalletSuccess
        }

        // If active wallet state is empty, then return false
        return false
    }

    suspend fun addTransaction(transaction: Transaction): Boolean  {
        var success = transactionRepository.addTransaction(transaction)

        if (!success) {
            return false
        }
        // Reuse variable
        success = false

        // Update wallet
        activeWalletStateFlow.value?.apply {
            when (transaction.type) {
                TransactionType.EXPENSE, TransactionType.SOURCE_TRANSFER -> {
                    success = updateWallet(
                        balance = balance - transaction.amount,
                        expense = expense + transaction.amount,
                        income = income,
                        walletId = transaction.walletId
                    )
                }

                TransactionType.INCOME, TransactionType.DESTINATION_TRANSFER -> {
                    success = updateWallet(
                        balance = balance + transaction.amount,
                        expense = expense,
                        income = income + transaction.amount,
                        walletId = transaction.walletId
                    )
                }

                TransactionType.INITIAL_BALANCE -> {
                    success = updateWallet(
                        balance = transaction.amount,
                        expense = 0.0,
                        income = transaction.amount,
                        walletId = transaction.walletId
                    )
                }
            }
        }

        return success
    }
}