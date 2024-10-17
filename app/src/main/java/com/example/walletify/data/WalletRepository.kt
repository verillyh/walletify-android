package com.example.walletify.data

class WalletRepository(private val walletDao: WalletDao) {
    suspend fun getUserWallet(userId: Long): Wallet {
        return walletDao.getUserWallet(userId)
    }

    suspend fun addWallet(wallet: Wallet): Long {
        return walletDao.addWallet(wallet)
    }
}