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
    private val _transactionStateFlow = MutableStateFlow<List<Transaction>?>(null)
    private val _walletStateFlow = MutableStateFlow<List<Wallet>?>(null)
    private val _activeWalletStateFlow = MutableStateFlow<Wallet?>(null)
    val activeWalletStateFlow: StateFlow<Wallet?> = _activeWalletStateFlow.asStateFlow()
    val walletStateFlow: StateFlow<List<Wallet>?> = _walletStateFlow.asStateFlow()
    val transactionStateFlow = _transactionStateFlow.asStateFlow()
    val userStateFlow: StateFlow<User?> = _userStateFlow.asStateFlow()
    val userScope = CoroutineScope(Dispatchers.IO)
    val transactionScope = CoroutineScope(Dispatchers.Default)
    val walletScope = CoroutineScope(Dispatchers.Default)
    val activeWalletScope = CoroutineScope(Dispatchers.Default)

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
//            addGuest()
            updateUserStateFlow(1)
        }

        // Start collecting transaction flow
        transactionScope.launch {
            walletRepository.transactionStateFlow.collect {transactionLists ->
                _transactionStateFlow.value = transactionLists
            }
        }

        // Start collecting wallet list flow
        walletScope.launch {
            walletRepository.walletStateFlow.collect {walletLists ->
                _walletStateFlow.value = walletLists
            }
        }

        // Start collecting active wallet flow
        activeWalletScope.launch {
            walletRepository.activeWalletStateFlow.collect { walletFlow ->
                _activeWalletStateFlow.value = walletFlow
            }
        }
    }

    suspend fun addTransaction(transaction: Transaction): Boolean {
        return walletRepository.addTransaction(transaction)
    }

    fun changeActiveWalletState(walletName: String) {
        return walletRepository.updateActiveWalletState(walletName)
    }

    suspend fun transfer(amount: Double, fromWalletName: String, toWalletName: String): Boolean {
        // TODO: Double check
        val success = userStateFlow.value?.let { walletRepository.transfer(amount, it.id, fromWalletName, toWalletName) }
        return success ?: false
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
            Log.i("Walletify", "Failed to add default wallet")
        }
        else {
            Log.i("Walletify", "User and default wallet added")
        }

        return success
    }

    suspend fun addGuest() {
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

            // TODO: Redundant??
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

    suspend fun getUserFromEmail(email: String): User? {
        return userDao.getUserFromEmail(email)
    }

    fun updateUserStateFlow(id: Long): Boolean  {
        // Cancel previous flow
        userScope.coroutineContext.cancelChildren()

        // Collect new state
        userScope.launch {
            getUserFromId(id).collect {user ->
                _userStateFlow.value = user
    //            return@collect true
            }
        }
        return true
    }

    suspend fun login(email: String, password: String): Boolean {
        // Use main thread, because we're updating state
        val user = getUserFromEmail(email)

        if (user == null) {
            return false
        }
        // Else if found
        else if (user.password == password) {
            // Update all state flows
            updateUserStateFlow(user.id)

            // TODO: Redundant??
//            walletRepository.updateActiveWalletState("Main")
            // TODO: Implement flow in transaciton repository instead
//            transactionRepository.updateTransactionFlow(user.id)

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
        return true
    }
}