package com.example.walletify.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WalletDao {
    @Query("SELECT * FROM wallet WHERE user_id LIKE :userId")
    suspend fun getUserWallet(userId: Long): Wallet

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addWallet(wallet: Wallet): Long
}