package com.example.walletify

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.ArrayAdapter
import androidx.lifecycle.LifecycleCoroutineScope
import com.example.walletify.data.Transaction
import com.example.walletify.ui.TransactionsViewModel
import com.example.walletify.ui.UserViewModel
import com.example.walletify.data.Wallet
import com.example.walletify.databinding.AddWalletBinding
import com.example.walletify.databinding.ChangeWalletBinding
import com.example.walletify.databinding.EntryExpenseBinding
import com.example.walletify.databinding.EntryIncomeBinding
import com.example.walletify.databinding.NewEntryBinding
import com.example.walletify.databinding.TransferWalletBinding
import com.example.walletify.databinding.WalletOptionsBinding
import com.example.walletify.ui.WalletViewModel
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import java.time.LocalDateTime

class Popup(
    private val context: Context,
    private val rootView: ViewGroup,
    private val userViewModel: UserViewModel,
    private val walletViewModel: WalletViewModel,
    private val transactionsViewModel: TransactionsViewModel,
    private val lifecycleScope: LifecycleCoroutineScope,

) {
    private val newEntryBinding = NewEntryBinding.inflate(LayoutInflater.from(context)  )
    private val expenseBinding =  EntryExpenseBinding.inflate(LayoutInflater.from(context))
    private val incomeBinding = EntryIncomeBinding.inflate(LayoutInflater.from(context))
    private val addWalletBinding = AddWalletBinding.inflate(LayoutInflater.from(context))
    private val transferWalletBinding = TransferWalletBinding.inflate(LayoutInflater.from(context))
    private val changeWalletBinding = ChangeWalletBinding.inflate(LayoutInflater.from(context))
    private val walletOptionsBinding = WalletOptionsBinding.inflate(LayoutInflater.from(context))

    fun showEntryView() {
        // Initialize variables
        val entryLayout = newEntryBinding.root
        val popupWindow = PopupWindow(
            entryLayout,
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT,
            true
        )

        val layoutContainer = newEntryBinding.entryContainer
        val expenseLayout = expenseBinding.root
        val incomeLayout = incomeBinding.root
//        val budgetLayout = LayoutInflater.from(context).inflate(R.layout.entry_budget, layoutContainer, false)
        val addCategories = newEntryBinding.addCategories

        // Set expense category input
        val expenseCategoryDropdown = expenseBinding.expenseCategory
        // Display all categories except income and transfer
        val expenseCategories = TransactionCategory.entries
            .filter { it.displayName != "Income" && it.displayName != "Transfer" && it.displayName != "Initial Balance"}
            .map { it.displayName }
        expenseCategoryDropdown.setAdapter(ArrayAdapter(
            context,
            android.R.layout.simple_dropdown_item_1line,
            expenseCategories
        ))
        expenseCategoryDropdown.setText(expenseCategories[0], false)

        // On add expense
        expenseBinding.entryExpenseAdd.setOnClickListener {
            onAddEntry(expenseLayout, TransactionType.EXPENSE)
            popupWindow.dismiss()
        }

        // On add income
        incomeBinding.entryIncomeAdd.setOnClickListener {
            onAddEntry(incomeLayout, TransactionType.INCOME)
            popupWindow.dismiss()
        }

        // Change wallet as default
        newEntryBinding.entryExpense.performClick()

        // Listen for change in category type
        addCategories.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked){
                when (checkedId) {
                    R.id.entry_expense -> switchEntryView(layoutContainer, expenseLayout)
                    R.id.entry_income -> switchEntryView(layoutContainer, incomeLayout)
//                    R.id.entry_budget -> switchEntryView(layoutContainer, budgetLayout)
                }
            }
        }

        // Dismiss when cancel is clicked
        newEntryBinding.closePopup.setOnClickListener {
            popupWindow.dismiss()
        }

        // Show at center of screen
        popupWindow.showAtLocation(rootView, Gravity.CENTER, 0, 0)
    }

    fun showWalletPopup() {
        // Initialize variables
        val popupWindow = PopupWindow(
            walletOptionsBinding.root,
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT,
            true
        )

        val layoutContainer = walletOptionsBinding.entryContainer
        val changeLayout = changeWalletBinding.root
        val transferLayout = transferWalletBinding.root
        val addLayout = addWalletBinding.root
        val walletOptions = walletOptionsBinding.walletOptions

        inflateAllWalletsAsOptions(changeLayout, R.id.change_wallet_dropdown)
        inflateAllWalletsAsOptions(transferLayout, R.id.from_wallet_dropdown)
        inflateAllWalletsAsOptions(transferLayout, R.id.to_wallet_dropdown)

        // Change wallet logic
        changeWalletBinding.changeWalletDropdown.setOnItemClickListener { parent, view, index, id ->
            val selected = parent.getItemAtPosition(index).toString()
            walletViewModel.changeActiveWallet(selected)
            popupWindow.dismiss()
        }

        // Transfer wallet logic
        transferWalletBinding.transferWalletAdd.setOnClickListener {
            onTransferWallet()
            popupWindow.dismiss()
        }

        // Add wallet logic
        addWalletBinding.addWallet.setOnClickListener {
            onAddWallet()
            popupWindow.dismiss()
        }

        // Expense layout as default
        switchEntryView(layoutContainer, changeLayout)

        // Listen for change in category type
        walletOptions.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked){
                when (checkedId) {
                    R.id.change_wallet -> switchEntryView(layoutContainer, changeLayout)
                    R.id.transfer_wallet -> switchEntryView(layoutContainer, transferLayout)
                    R.id.add_wallet -> switchEntryView(layoutContainer, addLayout)
                }
            }
        }

        // Dismiss when cancel is clicked
        walletOptionsBinding.closeWalletPopup.setOnClickListener {
            popupWindow.dismiss()
        }

        // Show at center of screen
        popupWindow.showAtLocation(rootView, Gravity.CENTER, 0, 0)
    }

    private fun onTransferWallet() {
        val amount = transferWalletBinding.amountInputEditText.text.toString().toDouble()
        val fromWalletName = transferWalletBinding.fromWalletDropdown.text.toString()
        val toWalletName = transferWalletBinding.toWalletDropdown.text.toString()


        lifecycleScope.launch {
            val success = transactionsViewModel.transfer(amount, fromWalletName, toWalletName)

            if (!success) {
                    Toast.makeText(context, "Transferred success!", Toast.LENGTH_SHORT)
                        .show()
            }
            else {
                Toast.makeText(context, "Something went wrong, please try again", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun inflateAllWalletsAsOptions(layout: View, optionId: Int) {
        // Use findViewById for dynamic access
        val dropdown = layout.findViewById<AutoCompleteTextView>(optionId)

        // Get wallet list from viewmodel, and show as option
        lifecycleScope.launch {
            walletViewModel.allUserWallets.collect { walletList ->
                val walletNames = walletList?.map { it.walletName }
                walletNames?.let {
                    dropdown.setAdapter(
                        ArrayAdapter(
                        layout.context,
                        android.R.layout.simple_dropdown_item_1line,
                        walletNames
                        )
                    )
                    // Set first wallet as default option
                    dropdown.setText(walletViewModel.activeWalletState.value.walletName, false)
                }

            }
        }
    }

    private fun switchEntryView(layoutContainer: ViewGroup, layout: View) {
        layoutContainer.removeAllViews()
        layoutContainer.addView(layout)
    }

    private fun onAddWallet() {
        val walletInitialBalance = addWalletBinding.amountInputEditText.text.toString().toDouble()
        val walletName = addWalletBinding.walletInputEditText.text.toString()
        val userId = userViewModel.uiState.value.id

        val wallet = Wallet(
            walletName = walletName,
            balance = walletInitialBalance,
            expense = 0.0,
            income = 0.0,
            userId = userId
        )

        lifecycleScope.launch {
            val success = walletViewModel.addUserWallet(wallet)

            if (success) {
                Toast.makeText(context, "Wallet successfully added", Toast.LENGTH_SHORT)
                    .show()
            }
            else {
                Toast.makeText(context, "Something went wrong, please try again", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        walletViewModel.changeActiveWallet(wallet.walletName)
    }

    private fun onAddEntry(parentLayout: View, type: TransactionType) {
        // Use findViewById for dynamic access
        val amount = parentLayout.findViewById<TextInputEditText>(R.id.amount_input_edit_text).text.toString()
        val note = parentLayout.findViewById<TextInputEditText>(R.id.note_input_edit_text).text.toString()
        var category = TransactionCategory.INCOME

        // If of type expense, populate category text field
        if (type == TransactionType.EXPENSE) {
            val selected = expenseBinding.expenseCategory.text.toString()
            if (selected == "") {
                Toast.makeText(context, "Please select a category", Toast.LENGTH_SHORT)
                    .show()
                return
            }
            category = TransactionCategory.entries.first { it.displayName == selected }
        }

        // Build transaction
        val transaction = Transaction(
            category = category,
            amount = amount.toDouble(),
            type = type,
            note = note,
            datetime = LocalDateTime.now(),
            walletId = walletViewModel.activeWalletState.value.id
        )

        // Add transaction
        lifecycleScope.launch {
            var success = false
            walletViewModel.activeWalletState.value.apply {
                success = transactionsViewModel.addUserTransaction(transaction)
            }

            if (success) {
                Toast.makeText(context, "Transaction added!", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(context, "Something went wrong, please try again", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}
