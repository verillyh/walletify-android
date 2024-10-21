package com.example.walletify.data

import android.util.Log
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

class WalletRepository(
    private val walletDao: WalletDao,
    transactionDao: TransactionDao,
    private val userStateFlow: StateFlow<User?>
) {
    private val transactionRepository: TransactionRepository
    private val _activeWalletStateFlow = MutableStateFlow<Wallet?>(null)
    private val _walletStateFlow = MutableStateFlow<List<Wallet>?>(null)
    val activeWalletStateFlow: StateFlow<Wallet?> = _activeWalletStateFlow.asStateFlow()
    val walletStateFlow: StateFlow<List<Wallet>?> = _walletStateFlow.asStateFlow()
    val transactionStateFlow: StateFlow<List<Transaction>?>
    val walletScope = CoroutineScope(Dispatchers.IO)
    // Use default, since we're not doing any database calls, nor updating UI
    val activeWalletScope = CoroutineScope(Dispatchers.Default)


    init {
        transactionRepository = TransactionRepository(transactionDao, activeWalletStateFlow)

        // Listen for user changes
        CoroutineScope(Dispatchers.IO).launch {
            userStateFlow.collect {
                if (it != null) {
                    updateWalletState(it.id)
                    Log.i("Walletify", "Collecting wallet state...")
                }
            }
        }

        // Get transaction state flow from transaction repository
        transactionStateFlow = transactionRepository.transactionStateFlow
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
                _activeWalletStateFlow.value = getWalletByName(walletName)
                Log.i("Walletify", "Active wallet state changed")
            }
        }
    }

    private suspend fun getWalletByName(walletName: String): Wallet? {
        return _walletStateFlow.first()?.find { it.walletName == walletName }
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

        if (success) {
            Log.i("Walletify", "Wallet added")
        }
        else {
            Log.i("Walletify", "Fail to add wallet")
        }

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
        val fromWallet = getWalletByName(fromWalletName)
        val toWallet = getWalletByName(toWalletName)

        val success = transactionRepository.transfer(amount,
            fromWallet?.id ?: -1,
            toWallet?.id ?: -1,
            fromWalletName,
            toWalletName
        )

        if (success == false || success == null) {
            return false
        }

        // TODO: Maybe have this automatically done when there's an entry in transaction?
        // Update source wallet
        fromWallet?.let {
            val updateSourceWalletSuccess = updateWallet(
                balance = fromWallet.balance - amount,
                expense = fromWallet.expense + amount,
                income = fromWallet.income,
                walletId = fromWallet.id
            )
            if (!updateSourceWalletSuccess) {
                return false
            }
        }

        // Update destination wallet
        toWallet?.let {
            val updateDestWalletSuccess = updateWallet(
                balance = toWallet.balance + amount,
                expense = toWallet.expense,
                income = toWallet.income + amount,
                walletId = toWallet.id
            )
            return updateDestWalletSuccess
        }

        // Return false if fromWallet or toWallet is null
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