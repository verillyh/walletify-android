package com.example.walletify.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallet")
data class Wallet (
    @ColumnInfo(name = "wallet_name") val walletName: String,
    @ColumnInfo(name = "balance") val balance: Double,
    @ColumnInfo(name = "expense") val expense: Double,
    @ColumnInfo(name = "income") val income: Double,
    @ColumnInfo(name = "user_id") val userId: Long,
    @PrimaryKey(autoGenerate = true) val id: Long = 0
)