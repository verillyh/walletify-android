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
import kotlinx.coroutines.withContext

class WalletRepository(private val walletDao: WalletDao) {
    private val _walletStateFlow = MutableStateFlow<List<Wallet>?>(null)
    val walletStateFlow: StateFlow<List<Wallet>?> = _walletStateFlow.asStateFlow()
    val repositoryScope = CoroutineScope(Dispatchers.IO)


    init {
        // Initialize wallet state
        // 1 for guest default wallet
        updateWalletState(1)
    }

   private fun getWalletsFromUserId(userId: Long): Flow<List<Wallet>> {
       val wallet = walletDao.getUserWallets(userId)

       return wallet
   }

    fun updateWalletState(userId: Long) {
        // Remove previous flow processes
        repositoryScope.coroutineContext.cancelChildren()

        repositoryScope.launch {
            getWalletsFromUserId(userId).collect { wallet ->
                _walletStateFlow.value = wallet
            }
        }
    }

    suspend fun getWalletId(userId: Long, walletName: String): Long {
        return withContext(Dispatchers.IO) {
            walletDao.getWalletId(userId, walletName)
        }
    }

    suspend fun addWallet(wallet: Wallet): Boolean {
        val walletId = walletDao.addWallet(wallet)
        if (walletId < 0) {
            return false
        }
        return true
    }

    suspend fun updateWallet(balance: Double, expense: Double, income: Double, walletId: Long): Int {
        // Update wallet in database
        val affectedRows = walletDao.updateWallet(
            balance = balance,
            expense = expense,
            income = income,
            id = walletId
        )

        // If no rows were affected, then return -1
        if (affectedRows <= 0 ){
            return -1
        }

        // Update wallet state to reflect in UI
        return affectedRows
    }
}