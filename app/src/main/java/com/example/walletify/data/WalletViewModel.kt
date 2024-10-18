package com.example.walletify.data

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

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
                    _allUserWallets.value = walletState
                }
                Log.i("Walletify", "Wallet list state changed")
            }
        }

        viewModelScope.launch {
            changeActiveWallet(1, "Main")
        }
    }

    suspend fun addUserWallet(wallet: Wallet): Boolean {
        return repository.addWallet(wallet)
    }

    suspend fun getWalletId(userId: Long, walletName: String): Long {
        return repository.getWalletId(userId, walletName)
    }

    fun changeActiveWallet(userId: Long, walletName: String) {
        viewModelScope.launch {
            val walletId = getWalletId(userId, walletName)
            _allUserWallets.collect {walletList ->
                // Get wallet to be active
                val activeWallet = walletList?.find { it.id == walletId }

                // Update the UI state
                activeWallet?.let {
                    _uiState.update { state ->
                        state.copy(
                            walletName = activeWallet.walletName,
                            balance = String.format(
                                Locale("en", "AU"),
                                "%.2f",
                                activeWallet.balance
                            )
                                .toDouble(),
                            expense = String.format(
                                Locale("en", "AU"),
                                "%.2f",
                                activeWallet.expense
                            )
                                .toDouble(),
                            income = String.format(Locale("en", "AU"), "%.2f", activeWallet.income)
                                .toDouble(),
                            userId = activeWallet.userId,
                            id = activeWallet.id
                        )
                    }
                }
                Log.i("Walletify", "Wallet UI state changed")
            }
        }
    }
}