package com.example.walletify.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.walletify.TransactionCategory
import java.time.LocalDateTime

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("user_id"),
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Transaction(
    @ColumnInfo(name = "category") val category: TransactionCategory,
    @ColumnInfo(name = "amount") val amount: Double,
    @ColumnInfo(name = "type") val type: Char,
    @ColumnInfo(name = "note") val note: String,
    @ColumnInfo(name = "datetime") val datetime: LocalDateTime = LocalDateTime.now(),
    @ColumnInfo(name = "user_id") val userId: Long,
    // TODO: Maybe change to wallet id?
    @PrimaryKey(autoGenerate = true) val id: Long = 0
)
