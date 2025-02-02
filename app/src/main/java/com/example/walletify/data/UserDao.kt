package com.example.walletify.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email LIKE :email")
    suspend fun getUserFromEmail(email: String): User?

    @Query("SELECT * FROM users WHERE id LIKE :userId")
    fun getUserFromId(userId: Long): Flow<User>

    @Query("UPDATE users SET " +
            "full_name = :fullName, " +
            "email = :email, " +
            "phone_number = :phoneNumber " +
            "WHERE id = :id")
    suspend fun updateUserDetails(fullName: String, email: String, phoneNumber: String, id: Long): Int

    // Return long, to check if successfully inserted or not
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(user: User): Long

    @Query("DELETE from users WHERE id LIKE :userId")
    suspend fun deleteUser(userId: Long): Int
}