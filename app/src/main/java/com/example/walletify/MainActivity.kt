package com.example.walletify

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.walletify.data.UserViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    lateinit var navHostFragment: NavHostFragment
    lateinit var navController: NavController
    lateinit var appBarConfiguration: AppBarConfiguration
    lateinit var userViewModel: UserViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]

        // Setup app navigation
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val appBar = findViewById<MaterialToolbar>(R.id.topAppBar)
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.main_fragment) as NavHostFragment
        navController = navHostFragment.navController
        appBarConfiguration =
            AppBarConfiguration(setOf(R.id.home, R.id.analysis, R.id.budgets, R.id.profile))

        // Set navigation
        setSupportActionBar(appBar)
        setupActionBarWithNavController(navController, appBarConfiguration)
        bottomNavigationView.setupWithNavController(navController)


        // Setup FAB
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            setNewEntryView()
        }


    }

    // Back button logic on app bar
    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.main_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    // Popup window when clicking the FAB button
    private fun setNewEntryView() {
        // Set variables
        val rootView = findViewById<ViewGroup>(R.id.main)
        val entryLayout = LayoutInflater.from(this).inflate(R.layout.new_entry, rootView, false)
        val popupWindow = PopupWindow(
            entryLayout,
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT,
            true
        )
        val layoutContainer = entryLayout.findViewById<LinearLayout>(R.id.entry_container)
        val expenseLayout = LayoutInflater.from(this).inflate(R.layout.entry_expense, layoutContainer, false)
        val incomeLayout = LayoutInflater.from(this).inflate(R.layout.entry_income, layoutContainer, false)
        val budgetLayout = LayoutInflater.from(this).inflate(R.layout.entry_budget, layoutContainer, false)
        val addCategories = entryLayout.findViewById<MaterialButtonToggleGroup>(R.id.add_categories)

        // Expense layout as default
        switchEntryView(layoutContainer, expenseLayout)

        // Listen for change in category type
        addCategories.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked){
                when (checkedId) {
                    R.id.entry_expense -> switchEntryView(layoutContainer, expenseLayout)
                    R.id.entry_income -> switchEntryView(layoutContainer, incomeLayout)
                    R.id.entry_budget -> switchEntryView(layoutContainer, budgetLayout)
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

    private fun switchEntryView(layoutContainer: ViewGroup, layout: View) {
        layoutContainer.removeAllViews()
        layoutContainer.addView(layout)
    }

    // Inflate menu to app bar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_app_bar, menu)
        return super.onCreateOptionsMenu(menu)
    }
}
