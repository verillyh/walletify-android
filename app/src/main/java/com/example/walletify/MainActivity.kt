package com.example.walletify

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
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
import com.example.walletify.databinding.ActivityMainBinding
import com.example.walletify.databinding.AddWalletBinding
import com.example.walletify.databinding.ChangeWalletBinding
import com.example.walletify.databinding.EntryExpenseBinding
import com.example.walletify.databinding.EntryIncomeBinding
import com.example.walletify.databinding.FragmentHomeBinding
import com.example.walletify.databinding.FragmentLoginBinding
import com.example.walletify.databinding.FragmentProfileBinding
import com.example.walletify.databinding.FragmentSignupBinding
import com.example.walletify.databinding.NewEntryBinding
import com.example.walletify.databinding.TransferWalletBinding
import com.example.walletify.databinding.WalletOptionsBinding
import com.example.walletify.ui.TransactionsViewModel
import com.example.walletify.ui.UserViewModel
import com.example.walletify.ui.WalletViewModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    lateinit var navHostFragment: NavHostFragment
    lateinit var navController: NavController
    lateinit var appBarConfiguration: AppBarConfiguration
    lateinit var userViewModel: UserViewModel
    lateinit var transactionsViewModel: TransactionsViewModel
    lateinit var walletViewModel: WalletViewModel
    lateinit var popup: Popup
    lateinit var mainBinding: ActivityMainBinding
    lateinit var homeBinding: FragmentHomeBinding
    lateinit var loginBinding: FragmentLoginBinding
    lateinit var signupBinding: FragmentSignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Setup bindings
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        homeBinding = FragmentHomeBinding.inflate(layoutInflater)
        loginBinding = FragmentLoginBinding.inflate(layoutInflater)
        signupBinding = FragmentSignupBinding.inflate(layoutInflater)

        setContentView(mainBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(mainBinding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Instantiate view models
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
        walletViewModel = ViewModelProvider(this)[WalletViewModel::class.java]
        transactionsViewModel = ViewModelProvider(this)[TransactionsViewModel::class.java]

        // Popup class
        popup = Popup(
            context = this,
            rootView = mainBinding.main,
            userViewModel = userViewModel,
            walletViewModel = walletViewModel,
            transactionsViewModel = transactionsViewModel,
            lifecycleScope = lifecycleScope,
        )

        // Setup app navigation
        val bottomNavigationView = mainBinding.bottomNavigation
        val appBar = mainBinding.topAppBar
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.main_fragment) as NavHostFragment
        navController = navHostFragment.navController
        appBarConfiguration =
            AppBarConfiguration(setOf(R.id.home, R.id.profile))

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
        val fab = mainBinding.fab
        fab.setOnClickListener {
            popup.showEntryView() // Show the popup
        }

        Toast.makeText(this, "Logged in as guest", Toast.LENGTH_SHORT)
            .show()
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
