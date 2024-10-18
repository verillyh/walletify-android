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
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.example.walletify.data.UserViewModel
import com.example.walletify.data.WalletViewModel
import com.google.android.material.appbar.MaterialToolbar
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
        // Inflate the layout for this fragment
        // TODO: Optimize code
        val layout = inflater.inflate(R.layout.fragment_profile, container, false)
        val edit = layout.findViewById<TextView>(R.id.user_edit)
        val userName = layout.findViewById<TextView>(R.id.user_name)
        val fullNameInputLayout = layout.findViewById<TextInputLayout>(R.id.full_name_input_layout)
        val fullNameEditText = layout.findViewById<TextInputEditText>(R.id.full_name_input_edit_text)
        val emailInputLayout = layout.findViewById<TextInputLayout>(R.id.email_input_layout)
        val emailEditText = layout.findViewById<TextInputEditText>(R.id.email_input_edit_text)
        val phoneNumberInputLayout = layout.findViewById<TextInputLayout>(R.id.phone_number_input_layout)
        val phoneNumberEditText = layout.findViewById<TextInputEditText>(R.id.phone_number_edit_text)
        val toHide = layout.findViewById<LinearLayout>(R.id.on_edit_hide)
        val saveChangesButton = layout.findViewById<Button>(R.id.save_changes)
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

                userName.text = state.fullName
                fullNameEditText.setText(state.fullName)
                emailEditText.setText(state.email)
                phoneNumberEditText.setText(state.phoneNumber)
            }
        }

        // Update subtitle for wallet name
        lifecycleScope.launch {
            walletViewModel.uiState.collect { state ->
                appBar?.subtitle = state.walletName
            }
        }

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
            saveChangesButton.isVisible = true
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
            saveChangesButton.isVisible = false

            // Save new details
            lifecycleScope.launch {
                userViewModel.updateDetails(
                    fullNameEditText.text.toString(),
                    phoneNumberEditText.text.toString(),
                    emailEditText.text.toString()
                )
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