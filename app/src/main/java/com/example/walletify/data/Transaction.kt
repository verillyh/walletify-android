package com.example.walletify.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDate

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
    @ColumnInfo(name = "category") val category: String,
    @ColumnInfo(name = "amount") val amount: Double,
    @ColumnInfo(name = "type") val type: Char,
    @ColumnInfo(name = "note") val note: String,
//    @ColumnInfo(name = "datetime") val datetime: LocalDate = LocalDate.now(),
    @ColumnInfo(name = "user_id") val userId: Long,
    @PrimaryKey(autoGenerate = true) val id: Long = -1
)
