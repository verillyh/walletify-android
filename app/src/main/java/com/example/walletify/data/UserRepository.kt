package com.example.walletify.data

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserRepository(private val userDao: UserDao) {
    val readAllData: Flow<List<User>> = userDao.getAllData()
    private val _userStateFlow = MutableStateFlow<User?>(null)
    val userStateFlow: StateFlow<User?> = _userStateFlow.asStateFlow()
    val repositoryScope = CoroutineScope(Dispatchers.IO)

    init {
        // Collect state flow from guest as default
        updateUserStateFlow(1)
    }

    suspend fun addUser(user: User, walletRepository: WalletRepository): Boolean {
        return withContext(Dispatchers.IO) {
            // Add new user
            val userId = userDao.insert(user)

            // If fail to add, return false
            if (userId < 0) {
                return@withContext false
            }

            val defaultWallet = Wallet(
                walletName = "Main",
                balance = 0.0,
                expense = 0.0,
                income = 0.0,
                userId = userId
            )

            walletRepository.addWallet(defaultWallet)
            Log.i("Walletify", "User and default wallet added")
            true
        }
    }

    suspend fun addGuest(walletRepository: WalletRepository) {
        // Get guest user
        val result = getUserFromId(1).firstOrNull()
        // Build guest profile
        val guest = User(
            fullName = "guest",
            email = "guest@guest.com",
            phoneNumber = "guest",
            password = "guest",
            id = 1
        )
        // If no guest user, add it
        if (result == null) {
            addUser(guest, walletRepository)
        }
    }

    suspend fun updateUserDetails(fullName: String, phoneNumber: String, email: String, id: Long): Boolean {
        return withContext(Dispatchers.IO) {
            // Update database
            val affectedRow = userDao.updateUserDetails(fullName, email, phoneNumber, id)

            // If no rows were affected, return false
            if (affectedRow <= 0) {
                return@withContext false
            }

            updateUserStateFlow(id)
            Log.i("Walletify", "User details updated")
            true
        }
    }

    private suspend fun getUserIdFromEmail(email: String): Long? {
        val result = userDao.getUserIdFromEmail(email) ?: return null

        return result
    }

    suspend fun getUserFromEmail(email: String): User? {
        val userId = getUserIdFromEmail(email) ?: return null

        return userDao.getUserFromId(userId).first()
    }

    fun updateUserStateFlow(id: Long) {
        // Cancel previous flow
        repositoryScope.coroutineContext.cancelChildren()

        // Collect new state
        repositoryScope.launch {
            val user = getUserFromId(id).firstOrNull()
            if (user != null) {
                _userStateFlow.value = user
            }
        }
    }

    suspend fun login(email: String, password: String, walletRepository: WalletRepository, transactionRepository: TransactionRepository): Boolean {
        return withContext(Dispatchers.IO) {
            val user = getUserFromEmail(email)

            if (user == null) {
                false
            }
            // Else if found
            else if (user.password == password) {
                // Update all state flows
                updateUserStateFlow(user.id)
                walletRepository.updateWalletState(user.id)
                transactionRepository.updateTransactionFlow(user.id)

                true
            }
            // Default case
            else {
                false
            }
        }
    }

    fun getUserFromId(userId: Long): Flow<User> {
        return userDao.getUserFromId(userId)
    }
}