package com.example.walletify.data

import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {
    val readAllData: Flow<List<User>> = userDao.getAllData()

    suspend fun addUser(user: User): Long {
        return userDao.insert(user)
    }

    suspend fun getUserFromEmail(email: String): User {
        val result = userDao.getUserFromEmail(email)
        return result
    }

    suspend fun getUserFromId(userId: Int): User {
        return userDao.getUserFromId(userId)
    }

    suspend fun updateUserDetails(fullName: String, email: String, phoneNumber: String, id: Long) {
        userDao.updateUserDetails(fullName, email, phoneNumber, id)
    }
}