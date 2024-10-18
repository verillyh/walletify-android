package com.example.walletify.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TransactionsViewModel(application: Application, walletViewModel: WalletViewModel): AndroidViewModel(application) {
    private val _allUserTransactions = MutableStateFlow<List<Transaction>?>(null)
    val allUserTransactions: StateFlow<List<Transaction>?> = _allUserTransactions.asStateFlow()
    val repository: TransactionRepository

    init {
        val transactionDao = WalletifyDatabase.getDatabase(application).transactionDao()
        repository = TransactionRepository(transactionDao, walletViewModel)

        // Collect flow from repository
        viewModelScope.launch {
            repository.transactionStateFlow.collect { transactionState ->
                _allUserTransactions.value = transactionState
            }
        }
    }

    suspend fun addUserTransaction(transaction: Transaction, walletRepository: WalletRepository, currentBalance: Double, currentExpense: Double, currentIncome: Double): Boolean {
        // Add transaction
        val success = repository.addTransaction(transaction, walletRepository, currentBalance, currentExpense, currentIncome)
        if (!success) {
            return false
        }
        return true
    }

    suspend fun transfer(amount: Double, fromWalletName: String, toWalletName: String, userId: Long): Boolean {
        return repository.transfer(amount, fromWalletName, toWalletName, userId)
    }

    // Factory
    companion object {
        fun provideFactory(application: Application, walletViewModel: WalletViewModel): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(TransactionsViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return TransactionsViewModel(application, walletViewModel) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}