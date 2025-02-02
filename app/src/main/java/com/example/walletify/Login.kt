package com.example.walletify

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.example.walletify.databinding.FragmentLoginBinding
import com.example.walletify.ui.UserViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.launch
import kotlin.math.log


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Login.newInstance] factory method to
 * create an instance of this fragment.
 */
class Login : Fragment() {
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
        val loginBinding = FragmentLoginBinding.inflate(inflater, container, false)
        val layout = loginBinding.root
        val navController = activity?.findNavController(R.id.main_fragment)
        val userViewModel: UserViewModel by activityViewModels()

        // Set wallet name to empty when in login page
        activity?.findViewById<MaterialToolbar>(R.id.topAppBar)?.subtitle = ""

        // Redirect to signup when user wants to
        loginBinding.redirectToSignup.setOnClickListener {
            navController?.navigate(R.id.signup)
        }

        // Login logic
        loginBinding.loginButton.setOnClickListener {
            val email = loginBinding.emailInputEditText.text.toString()
            val password = loginBinding.passwordInputEditText.text.toString()

            // Login via viewmodel. Show Toast of the result
            lifecycleScope.launch {
                val successful = userViewModel.login(email, password)

                if (successful) {
                    Toast.makeText(activity, "Logged in!", Toast.LENGTH_SHORT)
                        .show()
                    navController?.navigate(R.id.profile)
                }
                else {
                    Toast.makeText(activity, "Wrong credentials!", Toast.LENGTH_SHORT)
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
         * @return A new instance of fragment Login.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Login().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}