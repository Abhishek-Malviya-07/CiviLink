package com.example.civilink.SLF

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieAnimationView
import com.example.civilink.ProfileActivity
import com.example.civilink.R
import com.example.civilink.databinding.FragmentSignUpBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SignUpFragment : Fragment() {
    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!
    private lateinit var fireBaseAuth: FirebaseAuth

    private val PASSWORD_PATTERN: Regex =
        Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[$@$!%*?&])[A-Za-z\\d$@$!%*?&]{8,}")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val animationView: LottieAnimationView = binding.ani
        animationView.playAnimation()
        fireBaseAuth = FirebaseAuth.getInstance()

        binding.cirLoginButton.setOnClickListener {
            val email = binding.editTextTextEmailAddress2.text.toString()
            val pass = binding.editTextNumberPassword.text.toString()
            if (PASSWORD_PATTERN.matches(pass)) {
                if (email.isNotEmpty() && pass.isNotEmpty()) {
                    binding.progressBar.visibility = View.VISIBLE
                    // Use lifecycleScope for coroutines
                    viewLifecycleOwner.lifecycleScope.launch {
                        val result = createUserWithEmailAndPassword(email, pass)
                        binding.progressBar.visibility = View.GONE
                        handleAuthResult(result, email, pass)
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Please fill in the email and password",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "Password must have at least 8 characters, contain at least one uppercase letter, one lowercase letter, one number, and one special character.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private suspend fun createUserWithEmailAndPassword(email: String, password: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                fireBaseAuth.createUserWithEmailAndPassword(email, password).await()
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    private fun handleAuthResult(success: Boolean, email: String, password: String) {
        if (success) {
            val currentUser = fireBaseAuth.currentUser
            if (currentUser != null) {
                if (!currentUser.isEmailVerified) {
                    sendEmailVerification(email)
                    Toast.makeText(
                        requireContext(),
                        "A verification email has been sent to your email address. Please verify your email to continue.",
                        Toast.LENGTH_LONG
                    ).show()
                    clearFields()
                    findNavController().navigate(R.id.action_signUpFragment_to_loginFragment)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Please fill email and password correctly",
                        Toast.LENGTH_SHORT
                    ).show()
                    clearFields()
                }
            }
        } else {
            Toast.makeText(
                requireContext(),
                "Please fill email and password correctly or user already exists",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun clearFields() {
        binding.editTextTextEmailAddress2.text!!.clear()
        binding.editTextNumberPassword.text!!.clear()
    }

    private fun sendEmailVerification(email: String) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.sendEmailVerification()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Email verification sent.")
                } else {
                    Log.e(TAG, "Failed to send email verification.", task.exception)
                }
            }
    }
    override fun onStart() {
        super.onStart()
        val currentUser = fireBaseAuth.currentUser
        if (currentUser != null) {
            if (currentUser.isEmailVerified) {
                val intent = Intent(requireContext(), ProfileActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            } else {
                Toast.makeText(requireContext(), "Please verify your email to continue", Toast.LENGTH_LONG).show()
                fireBaseAuth.signOut()
            }
        }
    }
}
