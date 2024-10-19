package com.example.walletify

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.walletify.data.Transaction
import com.example.walletify.data.TransactionsViewModel
import com.example.walletify.data.UserViewModel
import com.example.walletify.data.WalletViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class MainActivity : AppCompatActivity() {
    lateinit var navHostFragment: NavHostFragment
    lateinit var navController: NavController
    lateinit var appBarConfiguration: AppBarConfiguration
    lateinit var userViewModel: UserViewModel
    lateinit var transactionsViewModel: TransactionsViewModel
    lateinit var walletViewModel: WalletViewModel
    lateinit var popup: Popup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
//         Instantiate view models
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
        walletViewModel = ViewModelProvider(this)[WalletViewModel::class.java]
        // Add guest if no guest profile
        lifecycleScope.launch {
            userViewModel.addGuest()
        }
        transactionsViewModel = ViewModelProvider(this)[TransactionsViewModel::class.java]

        popup = Popup(
            context = this,
            rootView = findViewById(R.id.main),
            userViewModel = userViewModel,
            walletViewModel = walletViewModel,
            transactionsViewModel = transactionsViewModel,
            lifecycleScope = lifecycleScope
        )

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
        lifecycleScope.launch {
            walletViewModel.activeWalletState.collect { state ->
                appBar.subtitle = state.walletName
            }
        }

        // Setup FAB
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            popup.showEntryView() // Show the popup
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

    // Inflate menu to app bar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_app_bar, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            // TODO: Add camera option
            R.id.walletMenu -> {
                popup.showWalletPopup()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
