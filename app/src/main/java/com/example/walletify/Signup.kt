package com.example.walletify

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.example.walletify.data.User
import com.example.walletify.data.UserViewModel
import com.example.walletify.data.WalletViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Signup.newInstance] factory method to
 * create an instance of this fragment.
 */
class Signup : Fragment() {
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
        val layout = inflater.inflate(R.layout.fragment_signup, container, false)
        val userViewModel: UserViewModel by activityViewModels()
        val walletViewModel: WalletViewModel by activityViewModels()
        val signup_button = layout.findViewById<Button>(R.id.signup_button)

        activity?.findViewById<MaterialToolbar>(R.id.topAppBar)?.subtitle = ""

        signup_button.setOnClickListener {
            // TODO: User validation
            val userEmail =
                layout.findViewById<TextInputEditText>(R.id.email_input_edit_text).text.toString()
            val userPhoneNumber =
                layout.findViewById<TextInputEditText>(R.id.phone_number_edit_text).text.toString()
            val userFullName =
                layout.findViewById<TextInputEditText>(R.id.full_name_input_edit_text).text.toString()
            val userPassword =
                layout.findViewById<TextInputEditText>(R.id.password_input_edit_text).text.toString()

            lifecycleScope.launch {
                // Add new user
                val success = userViewModel.addUser(
                    User(
                        fullName = userFullName,
                        email = userEmail,
                        phoneNumber = userPhoneNumber,
                        password = userPassword
                    ),
                    walletViewModel.repository
                )

                // If successfully added, show success toast
                if (success) {
                    Toast.makeText(activity, "Successfully signed up", Toast.LENGTH_SHORT)
                        .show()
                    activity?.findNavController(R.id.main_fragment)?.navigate(R.id.login)
                }
                // Else show fail toast
                else {
                    Toast.makeText(activity, "Something went wrong, please try again", Toast.LENGTH_SHORT)
                        .show()
                }
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
         * @return A new instance of fragment Signup.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Signup().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}