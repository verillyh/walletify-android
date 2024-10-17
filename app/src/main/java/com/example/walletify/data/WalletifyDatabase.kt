package com.example.walletify.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [User::class, Transaction::class, Wallet::class], version = 1, exportSchema = false)
public abstract class WalletifyDatabase: RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun transactionDao(): TransactionDao
    abstract fun walletDao(): WalletDao


    // Use as singleton to enforce 1 instance for the database
    companion object {
        @Volatile
        private var INSTANCE: WalletifyDatabase? = null

        fun getDatabase(context: Context): WalletifyDatabase {
            val tempInstance = INSTANCE
            // Return current instance if already built
            if (tempInstance != null) {
                return tempInstance
            }

            // Lock the function
            synchronized(this) {
                // Build database
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WalletifyDatabase::class.java,
                    "walletify_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}