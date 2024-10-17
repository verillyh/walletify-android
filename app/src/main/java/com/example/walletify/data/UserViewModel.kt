package com.example.walletify.data

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.util.copy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class UserUiState(
    val id: Long = 1,
    val fullName: String = "guest",
    val email: String = "guest@guest.com",
    val phoneNumber: String = "guest",
    val loggedIn: Boolean = false
)

class UserViewModel(application: Application): AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(UserUiState())
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

    val allUserData: Flow<List<User>>
    val repository: UserRepository

    init {
        // Get user DAO from database
        val userDao = WalletifyDatabase.getDatabase(application).userDao()

        // Get repository
        repository = UserRepository(userDao)
        // Get the user's table data
        allUserData = repository.readAllData
    }

    suspend fun addUser(user: User, walletRepository: WalletRepository): Boolean {
        // Launch async with coroutine.
        // Dispatchers.IO to use background thread instead of UI/main thread
        return withContext(Dispatchers.IO) {
            // Add new user
            val userId = repository.addUser(user)

            // If fail to add, return false
            if (userId < 0) {
                return@withContext false
            }

            // Add default wallet
            val walletId = walletRepository.addWallet(Wallet(
                balance = 0.0,
                expense = 0.0,
                income = 0.0,
                userId = userId
            ))

            // If wallet is successfully added, return true
            walletId >= 0
        }
    }

    suspend fun addGuest(walletRepository: WalletRepository) {
        // Get guest user
        val result = repository.getUserFromId(1)
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

    suspend fun updateDetails(fullName: String, phoneNumber: String, email: String): Boolean {
        // Dispatchers IO to use background threads instead of UI/main thread
        return withContext(Dispatchers.IO) {
            try {
                // Update database
                val userId = _uiState.value.id
                repository.updateUserDetails(fullName, email, phoneNumber, userId)

                // Update UI state
                updateUiState(fullName, phoneNumber, email, userId, true)
            }
            // If something goes wrong, log and return false
            catch (e: Exception) {
                Log.e("Walletify", e.toString())
                return@withContext false
            }
            true
        }
    }

    fun updateUiState(fullName: String, phoneNumber: String, email: String, id: Long, logged: Boolean) {
        // Update the UI state
        try {
            _uiState.update { state ->
                state.copy(
                    id = id,
                    fullName = fullName,
                    email = email,
                    phoneNumber = phoneNumber,
                    loggedIn = logged
                )
            }
        }
        // If an exception occurs, log and return false
        catch(e: Exception) {
            Log.e("Walletify", e.toString())
        }
    }

    suspend fun login(email: String, password: String): Boolean {
        return withContext(Dispatchers.IO) {
            // Get user based on email
            val user = repository.getUserFromEmail(email)

            // If no user is found
            if (user == null) {
                false
            }
            // Else if found
            else if (user.password == password) {
                updateUiState(
                    fullName = user.fullName,
                    phoneNumber = user.phoneNumber,
                    email = user.email,
                    id = user.id,
                    logged = true
                )
                true
            }
            // Default case
            else {
                false
            }
        }

    }
}