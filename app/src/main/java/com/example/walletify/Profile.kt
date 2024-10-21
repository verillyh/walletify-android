package com.example.walletify

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.example.walletify.databinding.FragmentProfileBinding
import com.example.walletify.databinding.ProfileDefaultButtonsBinding
import com.example.walletify.databinding.SaveChangesButtonBinding
import com.example.walletify.ui.UserViewModel
import com.example.walletify.ui.WalletViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Profile.newInstance] factory method to
 * create an instance of this fragment.
 */
class Profile : Fragment() {
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
        // Inflate bindings
        val profileBinding = FragmentProfileBinding.inflate(layoutInflater, container, false)
        val saveChangesButtonBinding = SaveChangesButtonBinding.inflate(layoutInflater, container, false)
        val defaultButtonsBinding = ProfileDefaultButtonsBinding.inflate(layoutInflater, container, false)

        // Set roots
        val layout = profileBinding.root
        val saveChangesButton = saveChangesButtonBinding.root
        val defaultButtons = defaultButtonsBinding.root

        // Profile bindings
        val buttonsContainer = profileBinding.profileButtonsContainer
        val edit = profileBinding.userEdit
        val userName = profileBinding.userName
        val fullNameInputLayout = profileBinding.fullNameInputLayout
        val fullNameEditText = profileBinding.fullNameInputEditText
        val emailInputLayout = profileBinding.emailInputLayout
        val emailEditText = profileBinding.emailInputEditText
        val phoneNumberInputLayout = profileBinding.phoneNumberInputLayout
        val phoneNumberEditText = profileBinding.phoneNumberEditText
        val toHide = profileBinding.onEditHide

        // Signout bindings
        val signout = defaultButtonsBinding.signout
        val deleteAccount = defaultButtonsBinding.deleteAccount

        val appBar = activity?.findViewById<MaterialToolbar>(R.id.topAppBar)
        val grayColor = resources.getColor(R.color.gray, null)
        val whiteColor = resources.getColor(R.color.white, null)
        val navController = activity?.findNavController(R.id.main_fragment)
        val userViewModel: UserViewModel by activityViewModels()
        val walletViewModel: WalletViewModel by activityViewModels()

        lifecycleScope.launch {
            // Change profile state whenever it's updated
            userViewModel.uiState.collect { state ->
                if (!state.loggedIn) {
                    navController?.navigate(R.id.login)
                }

                // Update profile texts
                userName.text = state.fullName
                fullNameEditText.setText(state.fullName)
                emailEditText.setText(state.email)
                phoneNumberEditText.setText(state.phoneNumber)
            }
        }

        // Update subtitle for wallet name
        lifecycleScope.launch {
            walletViewModel.activeWalletState.collect { state ->
                appBar?.subtitle = state.walletName
            }
        }

        buttonsContainer.addView(defaultButtons)

        // Edit logic
        edit.setOnClickListener {
            Log.i("Walletify", "Edit clicked")
            fullNameInputLayout.isEnabled = true
            fullNameEditText.setTextColor(whiteColor)
            emailInputLayout.isEnabled = true
            emailEditText.setTextColor(whiteColor)
            phoneNumberInputLayout.isEnabled = true
            phoneNumberEditText.setTextColor(whiteColor)
            toHide.isVisible = false
            buttonsContainer.removeAllViews()
            buttonsContainer.addView(saveChangesButton)
        }

        // On click save button
        saveChangesButton.setOnClickListener {
            fullNameInputLayout.isEnabled = false
            fullNameEditText.setTextColor(grayColor)
            emailInputLayout.isEnabled = false
            emailEditText.setTextColor(grayColor)
            phoneNumberInputLayout.isEnabled = false
            phoneNumberEditText.setTextColor(grayColor)
            toHide.isVisible = true
            buttonsContainer.removeAllViews()
            buttonsContainer.addView(defaultButtons)

            // Save new details
            lifecycleScope.launch {
                userViewModel.updateDetails(
                    fullNameEditText.text.toString(),
                    phoneNumberEditText.text.toString(),
                    emailEditText.text.toString()
                )
            }
        }

        // Confirmation for signup
        signout.setOnClickListener {
            container?.let {
                MaterialAlertDialogBuilder(it.context)
                    .setTitle(resources.getString(R.string.signout))
                    .setMessage(resources.getString(R.string.signout_confirmation))
                    .setNeutralButton(resources.getString(R.string.cancel)) { dialog, which -> }
                        // If confirmed, then sign out and let make a Toast
                    .setPositiveButton(resources.getString(R.string.signout)) { dialog, which ->
                        lifecycleScope.launch {
                            val success = userViewModel.signout()

                            if (success) {
                                Toast.makeText(activity, "Signed out! Using guess account...", Toast.LENGTH_SHORT)
                                    .show()
                            }
                            else {
                                Toast.makeText(activity, "Something went wrong, please try again", Toast.LENGTH_SHORT)
                                    .show()
                            }

                            navController?.navigate(R.id.home)
                        }
                    }
                    .show()
            }
        }

        deleteAccount.setOnClickListener {
            container?.let {
                MaterialAlertDialogBuilder(it.context)
                    .setTitle(resources.getString(R.string.delete_account))
                    .setMessage(resources.getString(R.string.delete_account_confirmation))
                    .setNeutralButton(resources.getString(R.string.cancel)) { dialog, which -> }
                    .setPositiveButton(resources.getString(R.string.delete_account)) { dialog, which ->
                        lifecycleScope.launch {
                            val success = userViewModel.deleteAccount()

                            if (success) {
                                Toast.makeText(activity, "Account deleted!", Toast.LENGTH_SHORT)
                                    .show()
                                navController?.navigate(R.id.home)
                            }
                            else {
                                Toast.makeText(activity, "Something went wrong, please try again", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }
                    .show()
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
         * @return A new instance of fragment Profile.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Profile().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}