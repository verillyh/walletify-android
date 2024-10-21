package com.example.walletify

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.walletify.databinding.FragmentHomeBinding
import com.example.walletify.ui.TransactionsViewModel
import com.example.walletify.ui.WalletViewModel
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Home.newInstance] factory method to
 * create an instance of this fragment.
 */
class Home : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val homeBinding = FragmentHomeBinding.inflate(inflater, container, false)
        val layout = homeBinding.root
        val balance = homeBinding.currentBalance
        val expense = homeBinding.currentMonthExpense
        val income = homeBinding.currentMonthIncome
        val transactionList = homeBinding.transactionRecyclerView
        val transactionViewModel: TransactionsViewModel by activityViewModels()
        val walletViewModel: WalletViewModel by activityViewModels()
        val appBar = activity?.findViewById<MaterialToolbar>(R.id.topAppBar)

        // Set recycler view
        val adapter = TransactionItemAdapter()
        transactionList.layoutManager = LinearLayoutManager(activity)
        transactionList.adapter = adapter

        // Update cashflow, income, expense
        lifecycleScope.launch {
            walletViewModel.activeWalletState.collect { state ->
                // Main balance screen
                balance.text = String.format("$" + state.balance.toString())
                expense.text = String.format("$" + state.expense.toString())
                income.text = String.format("$" + state.income.toString())

                // Update subtitle for wallet name
                appBar?.subtitle = state.walletName
            }
        }

        // Update list if there is an update
        lifecycleScope.launch {
            transactionViewModel.allUserTransactions.collect { transactionState ->
                transactionState?.let { adapter.setData(it) }
            }
        }


        return layout
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Home.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Home().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}