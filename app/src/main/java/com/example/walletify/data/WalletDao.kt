package com.example.walletify.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {
    @Query("SELECT * FROM wallet WHERE user_id LIKE :userId")
    fun getUserWallet(userId: Long): Flow<Wallet>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addWallet(wallet: Wallet): Long

    @Query("UPDATE wallet SET " +
            "balance = :balance, " +
            "expense = :expense, " +
            "income = :income " +
            "WHERE user_id LIKE :userId")
    suspend fun updateWallet(balance: Double, expense: Double, income: Double, userId: Long): Int
}