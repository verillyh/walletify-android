package com.example.walletify.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addTransaction(transaction: Transaction): Long

    @Query("SELECT * FROM transactions WHERE user_id LIKE :userId")
    fun getUserTransactions(userId: Long): Flow<List<Transaction>>
}