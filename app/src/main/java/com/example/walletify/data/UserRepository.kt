package com.example.walletify.data

import android.app.Application
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

class UserRepository(application: Application) {
    // Initialize Daos
    private val walletDao: WalletDao = WalletifyDatabase.getDatabase(application).walletDao()
    private val userDao: UserDao = WalletifyDatabase.getDatabase(application).userDao()
    private val transactionDao: TransactionDao = WalletifyDatabase.getDatabase(application).transactionDao()
    private val walletRepository: WalletRepository
    private val _userStateFlow = MutableStateFlow<User?>(null)
    val activeWalletStateFlow: StateFlow<Wallet?>
    val walletStateFlow: StateFlow<List<Wallet>?>
    val transactionStateFlow: StateFlow<List<Transaction>?>
    val userStateFlow: StateFlow<User?> = _userStateFlow.asStateFlow()
    val userScope = CoroutineScope(Dispatchers.IO)

    // Singleton instance of UserRepository
    companion object {
        @Volatile
        private var INSTANCE: UserRepository? = null

        fun getInstance(application: Application): UserRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserRepository(application).also { INSTANCE = it }
            }
        }
    }

    init {
        // Initialize wallet repository
        walletRepository = WalletRepository(walletDao, transactionDao, userStateFlow)

        // Collect state flow from guest as default
        userScope.launch {
            addGuest()
            updateUserStateFlow(1)
        }

        // Pass state flows from wallet repository to here
        transactionStateFlow = walletRepository.transactionStateFlow
        walletStateFlow = walletRepository.walletStateFlow
        activeWalletStateFlow = walletRepository.activeWalletStateFlow
    }

    suspend fun addTransaction(transaction: Transaction): Boolean {
        val success = walletRepository.addTransaction(transaction)
        if (success) {
            Log.i("Walletify", "Transaction added!")
        }
        else {
            Log.i("Walletify", "Transaction failed")
        }
        return success
    }

    fun changeActiveWalletState(walletName: String) {
        return walletRepository.updateActiveWalletState(walletName)
    }

    suspend fun transfer(amount: Double, fromWalletName: String, toWalletName: String): Boolean {
        val success = walletRepository.transfer(amount, fromWalletName, toWalletName)
        if (success) {
            Log.i("Walletify", "Transferred from $fromWalletName to $toWalletName successful")
        }
        else {
            Log.i("Walletify", "Transfer failed")
        }
        return success
    }

    suspend fun addWallet(wallet: Wallet): Boolean {
        return walletRepository.addWallet(wallet)
    }

    suspend fun addUser(user: User): Boolean {
        // Add new user
        val userId = userDao.insert(user)

        // If fail to add, return false
        if (userId < 0) {
            Log.i("Walletify", "Failed to add user")
            return false
        }

        // Build user's default wallet
        val defaultWallet = Wallet(
            walletName = "Main",
            balance = 0.0,
            expense = 0.0,
            income = 0.0,
            userId = userId
        )


        val success = walletRepository.addWallet(defaultWallet)
        if (success) {
            Log.i("Walletify", "User and default wallet added")
        }
        else {
            Log.i("Walletify", "Failed to add default wallet")
        }

        return success
    }

    private suspend fun addGuest() {
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
            addUser(guest)
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

            val success = updateUserStateFlow(id)
            if (success) {
                Log.i("Walletify", "User details updated")
                true
            }
            else {
                Log.i("Walletify", "Failed to update user details")
                false
            }
        }
    }

    private suspend fun getUserFromEmail(email: String): User? {
        return userDao.getUserFromEmail(email)
    }

    private fun updateUserStateFlow(id: Long): Boolean  {
        // Cancel previous flow
        userScope.coroutineContext.cancelChildren()

        // Collect new state
        userScope.launch {
            getUserFromId(id).collect {user ->
                _userStateFlow.value = user
                Log.i("Walletify", "Collecting new user state...")
            }
        }
        return true
    }

    suspend fun login(email: String, password: String): Boolean {
        val user = getUserFromEmail(email)

        if (user == null) {
            Log.i("Walletify", "User not found")
            return false
        }
        // Else if found
        else if (user.password == password) {
            // Update all state flows
            updateUserStateFlow(user.id)
            Log.i("Walletify", "User logged in")
            return true
        }
        // Default case
        else {
            return false
        }
    }

    suspend fun signout(): Boolean {
        return withContext(Dispatchers.IO) {
            val guestUser = getUserFromId(1).first()
            Log.i("Walletify", "User signed out...")
            updateUserStateFlow(guestUser.id)
        }
    }

    fun getUserFromId(userId: Long): Flow<User> {
        return userDao.getUserFromId(userId)
    }

    suspend fun deleteUser(userId: Long): Boolean {
        val affectedRows = userDao.deleteUser(userId)
        if (affectedRows <= 0) {
            return false
        }

        signout()
        Log.i("Walletify", "User: $userId deleted")
        return true
    }
}