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

    suspend fun updateUserDetails(fullName: String, email: String, phoneNumber: String, id: Int) {
        userDao.updateUserDetails(fullName, email, phoneNumber, id)
    }
}