package com.example.walletify.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.walletify.TransactionCategory
import com.example.walletify.TransactionType
import java.time.LocalDateTime

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = Wallet::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("wallet_id"),
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Transaction(
    @ColumnInfo(name = "category") val category: TransactionCategory,
    @ColumnInfo(name = "amount") val amount: Double,
    @ColumnInfo(name = "type") val type: TransactionType,
    @ColumnInfo(name = "note") val note: String,
    @ColumnInfo(name = "datetime") val datetime: LocalDateTime = LocalDateTime.now(),
    @ColumnInfo(name = "wallet_id") val walletId: Long,
    @PrimaryKey(autoGenerate = true) val id: Long = 0
)
