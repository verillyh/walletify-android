package com.example.walletify.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
public interface UserDao {

    @Query("SELECT * FROM users")
    fun getAllData(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE email LIKE :email")
    suspend fun getUserFromEmail(email: String): User

    @Query("SELECT * FROM users WHERE id LIKE :userId")
    suspend fun getUserFromId(userId: Int): User

    @Query("UPDATE users SET " +
            "full_name = :fullName, " +
            "email = :email, " +
            "phone_number = :phoneNumber " +
            "WHERE id = :id")
    suspend fun updateUserDetails(fullName: String, email: String, phoneNumber: String, id: Long)

    // Return long, to check if successfully inserted or not
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User): Long
}