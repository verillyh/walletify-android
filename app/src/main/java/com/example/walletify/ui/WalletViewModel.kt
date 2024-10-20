package com.example.walletify.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.walletify.data.UserRepository
import com.example.walletify.data.Wallet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WalletUiState(
    val walletName: String = "",
    val balance: Double = 0.0,
    val expense: Double = 0.0,
    val income: Double = 0.0,
    val userId: Long = 0,
    val id: Long = 0
)

class WalletViewModel(application: Application): AndroidViewModel(application) {
    val _allUserWallets = MutableStateFlow<List<Wallet>?>(null)
    val allUserWallets: StateFlow<List<Wallet>?> = _allUserWallets.asStateFlow()
    private val _activeWalletState = MutableStateFlow(WalletUiState())
    val activeWalletState: StateFlow<WalletUiState> = _activeWalletState.asStateFlow()
    val repository: UserRepository = UserRepository.getInstance(application)

    init {
        // Update wallet list UI state based on repo's state flow
        // Use main thread since it's only to update state
        viewModelScope.launch {
            repository.walletStateFlow.collect { walletState ->
                walletState?.let {
                    _allUserWallets.value = walletState
                }
            }
        }

        // Update active wallet UI state
        viewModelScope.launch {
            repository.activeWalletStateFlow.collect { state ->
                state?.let {
                    _activeWalletState.update {uiState ->
                        uiState.copy(
                            walletName = it.walletName,
                            balance = it.balance,
                            expense = it.expense,
                            income = it.income,
                            userId = it.userId,
                            id = it.id
                        )
                    }
                }
            }
        }
    }

    suspend fun addUserWallet(wallet: Wallet): Boolean {
        return repository.addWallet(wallet)
    }

    fun changeActiveWallet(walletName: String) {
        return repository.changeActiveWalletState(walletName)
    }
}