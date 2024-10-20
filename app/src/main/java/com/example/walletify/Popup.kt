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
import com.example.walletify.ui.WalletViewModel
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class Popup(
    private val context: Context,
    private val rootView: ViewGroup,
    private val userViewModel: UserViewModel,
    private val walletViewModel: WalletViewModel,
    private val transactionsViewModel: TransactionsViewModel,
    private val lifecycleScope: LifecycleCoroutineScope
) {
    fun showEntryView() {
        // Initialize variables
        val entryLayout = LayoutInflater.from(context).inflate(R.layout.new_entry, rootView, false)
        val popupWindow = PopupWindow(
            entryLayout,
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT,
            true
        )

        val layoutContainer = entryLayout.findViewById<LinearLayout>(R.id.entry_container)
        val expenseLayout = LayoutInflater.from(context).inflate(R.layout.entry_expense, layoutContainer, false)
        val incomeLayout = LayoutInflater.from(context).inflate(R.layout.entry_income, layoutContainer, false)
        val budgetLayout = LayoutInflater.from(context).inflate(R.layout.entry_budget, layoutContainer, false)
        val addCategories = entryLayout.findViewById<MaterialButtonToggleGroup>(R.id.add_categories)

        // Set expense category input
        val expenseCategoryDropdown = expenseLayout.findViewById<AutoCompleteTextView>(R.id.expense_category)
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
        expenseLayout.findViewById<Button>(R.id.entry_expense_add).setOnClickListener {
            onAddEntry(expenseLayout, TransactionType.EXPENSE)
            popupWindow.dismiss()
        }

        // On add income
        incomeLayout.findViewById<Button>(R.id.entry_income_add).setOnClickListener {
            onAddEntry(incomeLayout, TransactionType.INCOME)
            popupWindow.dismiss()
        }

        // Expense layout as default
        switchEntryView(layoutContainer, expenseLayout)

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
        entryLayout.findViewById<ImageView>(R.id.close_popup).setOnClickListener {
            popupWindow.dismiss()
        }

        // Show at center of screen
        popupWindow.showAtLocation(rootView, Gravity.CENTER, 0, 0)
    }

    fun showWalletPopup() {
        // Initialize variables
        val optionsLayout = LayoutInflater.from(context).inflate(R.layout.wallet_options, rootView, false)
        val popupWindow = PopupWindow(
            optionsLayout,
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT,
            true
        )

        val layoutContainer = optionsLayout.findViewById<LinearLayout>(R.id.entry_container)
        val changeLayout = LayoutInflater.from(context).inflate(R.layout.change_wallet, layoutContainer, false)
        val transferLayout = LayoutInflater.from(context).inflate(R.layout.transfer_wallet, layoutContainer, false)
        val addLayout = LayoutInflater.from(context).inflate(R.layout.add_wallet, layoutContainer, false)
        val walletOptions = optionsLayout.findViewById<MaterialButtonToggleGroup>(R.id.wallet_options)

        inflateAllWalletsAsOptions(changeLayout, R.id.change_wallet_dropdown)
        inflateAllWalletsAsOptions(transferLayout, R.id.from_wallet_dropdown)
        inflateAllWalletsAsOptions(transferLayout, R.id.to_wallet_dropdown)

        // Change wallet logic
        changeLayout.findViewById<AutoCompleteTextView>(R.id.change_wallet_dropdown).setOnItemClickListener { parent, view, index, id ->
            val selected = parent.getItemAtPosition(index).toString()
            walletViewModel.changeActiveWallet(selected)
            popupWindow.dismiss()
        }

        // Transfer wallet logic
        transferLayout.findViewById<Button>(R.id.transfer_wallet_add).setOnClickListener {
            onTransferWallet(transferLayout)
            popupWindow.dismiss()
        }

        // Add wallet logic
        addLayout.findViewById<Button>(R.id.add_wallet).setOnClickListener {
            onAddWallet(addLayout)
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
        optionsLayout.findViewById<ImageView>(R.id.close_wallet_popup)?.setOnClickListener {
            popupWindow.dismiss()
        }

        // Show at center of screen
        popupWindow.showAtLocation(rootView, Gravity.CENTER, 0, 0)
    }

    private fun onTransferWallet(layout: View) {
        val amount = layout.findViewById<TextInputEditText>(R.id.amount_input_edit_text).text.toString().toDouble()
        val fromWalletName = layout.findViewById<AutoCompleteTextView>(R.id.from_wallet_dropdown).text.toString()
        val toWalletName = layout.findViewById<AutoCompleteTextView>(R.id.to_wallet_dropdown).text.toString()


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

    private fun onAddWallet(parentLayout: View) {
        val walletInitialBalance = parentLayout.findViewById<TextInputEditText>(R.id.amount_input_edit_text).text.toString().toDouble()
        val walletName = parentLayout.findViewById<TextInputEditText>(R.id.wallet_input_edit_text).text.toString()
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
        val amount = parentLayout.findViewById<TextInputEditText>(R.id.amount_input_edit_text).text.toString()
        val note = parentLayout.findViewById<TextInputEditText>(R.id.note_input_edit_text).text.toString()
        var category = TransactionCategory.INCOME

        // If of type expense, populate category text field
        if (type == TransactionType.EXPENSE) {
            val selected = parentLayout.findViewById<AutoCompleteTextView>(R.id.expense_category).text.toString()
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
