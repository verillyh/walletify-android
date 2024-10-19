package com.example.walletify.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.walletify.data.User
import com.example.walletify.data.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
    val repository: UserRepository

    init {
        // Get repository
        repository = UserRepository.getInstance(application)

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

    suspend fun addUser(user: User): Boolean {
        return repository.addUser(user)
    }

    suspend fun updateDetails(fullName: String, phoneNumber: String, email: String): Boolean {
        return repository.updateUserDetails(fullName, phoneNumber, email, _uiState.value.id)
    }

    suspend fun login(email: String, password: String): Boolean {
        val success = repository.login(email, password)

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

    suspend fun signout(): Boolean {
        // Signal sign out to data layer
        val success = repository.signout()

        // Update UI state if success
        if (success) {
            _uiState.update { state ->
                state.copy (
                    loggedIn = false
                )
            }
        }

        return success
    }


    suspend fun deleteAccount(): Boolean {
        return repository.deleteUser(_uiState.value.id)
    }
}