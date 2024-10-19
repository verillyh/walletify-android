package com.example.walletify

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.walletify.ui.WalletViewModel
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Budgets.newInstance] factory method to
 * create an instance of this fragment.
 */
class Budgets : Fragment() {
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
        val layout = inflater.inflate(R.layout.fragment_budgets, container, false)
        val budgetItemContainer = layout.findViewById<RecyclerView>(R.id.budget_container)
        val data = mutableListOf<BudgetItem>()
        val appBar = activity?.findViewById<MaterialToolbar>(R.id.topAppBar)
        val walletViewModel: WalletViewModel by activityViewModels()

        // Update subtitle for wallet name
        lifecycleScope.launch {
            walletViewModel.activeWalletState.collect { state ->
                appBar?.subtitle = state.walletName
            }
        }

        // Simulate list of budgets
        for (i in 1..15) {
            data.add(BudgetItem(
                R.drawable.shopping_icon,
                getString(R.string.shopping),
                i * 25.0,
                25.0 - i
            ))
        }

        val adapter = BudgetItemAdapter(data)
        budgetItemContainer.layoutManager = LinearLayoutManager(activity)
        budgetItemContainer.adapter = adapter

        return layout
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Budgets.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Budgets().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}