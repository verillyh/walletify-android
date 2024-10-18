package com.example.walletify.data

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class WalletUiState(
    val walletName: String = "",
    val balance: Double = 0.0,
    val expense: Double = 0.0,
    val income: Double = 0.0,
    val userId: Long = 0,
    val id: Long = 0
)

class WalletViewModel(application: Application): AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()
    val repository: WalletRepository

    init {
        val walletDao = WalletifyDatabase.getDatabase(application).walletDao()
        repository = WalletRepository(walletDao)

        // Update UI state based on repo's state flow
        // Use main thread since it's only to update state
        viewModelScope.launch {
            repository.walletStateFlow.collect { walletState ->
                walletState?.let {
                    _uiState.update { state ->
                        state.copy(
                            walletName = walletState.walletName,
                            balance = walletState.balance,
                            expense = walletState.expense,
                            income = walletState.income,
                            userId = walletState.userId,
                            id = walletState.id
                        )
                    }
                }
            }
        }
    }
}