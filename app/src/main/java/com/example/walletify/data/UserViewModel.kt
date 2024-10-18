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
    val id: Long = -1,
    val fullName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
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

        // Set UI state to be the current user
        viewModelScope.launch {
            repository.userStateFlow.collect { user ->
                user?.let {
                    _uiState.update { state ->
                        state.copy(
                            id = user.id,
                            fullName = user.fullName,
                            email = user.email,
                            phoneNumber = user.phoneNumber
                        )
                    }
                }
            }
        }
    }

    suspend fun addUser(user: User, walletRepository: WalletRepository): Boolean {
        return repository.addUser(user, walletRepository)
    }

    suspend fun addGuest(walletRepository: WalletRepository) {
        repository.addGuest(walletRepository)
    }

    suspend fun updateDetails(fullName: String, phoneNumber: String, email: String): Boolean {
        val success = repository.updateUserDetails(fullName, phoneNumber, email, _uiState.value.id)
        return success
    }

    suspend fun login(email: String, password: String, walletRepository: WalletRepository): Boolean {
        val success = repository.login(email, password, walletRepository)

        // If can't login
        if (!success) {
            return false
        }
        // Else if successfully logged in
        else {
            // Change state to logged in
            _uiState.update { state ->
                state.copy(
                    loggedIn = true
                )
            }
            return true
        }
    }
}