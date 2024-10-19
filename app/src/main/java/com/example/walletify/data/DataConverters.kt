package com.example.walletify.data

import androidx.room.TypeConverter
import com.example.walletify.TransactionCategory
import com.example.walletify.TransactionType
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class DataConverters {
    private val datetimeFormat = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy, HH:mm", Locale("en", "AU"))

    // Store enum as string
    @TypeConverter
    fun fromTransactionCategory(category: TransactionCategory): String {
        return category.name
    }

    // Convert stored string back to enum
    @TypeConverter
    fun toTransactionCategory(category: String): TransactionCategory {
        return TransactionCategory.valueOf(category)
    }

    // Convert LocalDate to a string
    @TypeConverter
    fun fromLocalDateTime(date: LocalDateTime?): String? {
        return date?.format(datetimeFormat)
    }

    // Convert string back to LocalDate
    @TypeConverter
    fun toLocalDateTime(dateString: String?): LocalDateTime? {
        return dateString?.let {
            LocalDateTime.parse(it, datetimeFormat)
        }
    }

    // Convert TransactionType to Char
    @TypeConverter
    fun fromTransactionType(type: TransactionType): Char {
        return type.typeCode
    }

    // Convert Char to TransactionType
    @TypeConverter
    fun toTransactionType(type: Char): TransactionType {
        return TransactionType.entries.first { it.typeCode == type }
    }
}