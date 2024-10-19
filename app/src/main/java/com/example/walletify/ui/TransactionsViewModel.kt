package com.example.walletify.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.walletify.data.Transaction
import com.example.walletify.data.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TransactionsViewModel(application: Application): AndroidViewModel(application) {
    private val _allUserTransactions = MutableStateFlow<List<Transaction>?>(null)
    val allUserTransactions: StateFlow<List<Transaction>?> = _allUserTransactions.asStateFlow()
    // Initialize repository
    val repository: UserRepository = UserRepository.getInstance(application)

    init {

        // Collect flow from repository
        viewModelScope.launch {
            repository.transactionStateFlow.collect { transactionState ->
                _allUserTransactions.value = transactionState
            }
        }
    }

    suspend fun addUserTransaction(transaction: Transaction): Boolean {
        // Add transaction
        return repository.addTransaction(transaction)
    }

    suspend fun transfer(amount: Double, fromWalletName: String, toWalletName: String): Boolean {
        return repository.transfer(amount, fromWalletName, toWalletName)
    }
}