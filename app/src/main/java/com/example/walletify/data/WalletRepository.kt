package com.example.walletify.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class WalletRepository(private val walletDao: WalletDao) {
    private val _walletStateFlow = MutableStateFlow<Wallet?>(null)
    val walletStateFlow: StateFlow<Wallet?> = _walletStateFlow.asStateFlow()
    val repositoryScope = CoroutineScope(Dispatchers.IO)


    init {
        // Initialize wallet state
        // 1 for guest default wallet
        updateWalletState(1)
    }

   private fun getWalletFromUserId(userId: Long): Flow<Wallet> {
       val wallet = walletDao.getUserWallet(userId)

       return wallet
   }

    fun updateWalletState(userId: Long) {
        // Remove previous flow processes
        repositoryScope.coroutineContext.cancelChildren()

        repositoryScope.launch {
            getWalletFromUserId(userId).collect { wallet ->
                _walletStateFlow.value = wallet
            }

        }
    }

    suspend fun addWallet(wallet: Wallet): Boolean {
        val walletId = walletDao.addWallet(wallet)
        if (walletId < 0) {
            return false
        }
        return true
    }

    suspend fun updateWallet(balance: Double, expense: Double, income: Double, userId: Long): Int {
        // Update wallet in database
        val affectedRows = walletDao.updateWallet(
            balance = balance,
            expense = expense,
            income = income,
            userId = userId
        )

        // If no rows were affected, then return -1
        if (affectedRows <= 0 ){
            return -1
        }

        // Update wallet state to reflect in UI
        return affectedRows
    }
}