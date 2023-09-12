package com.example.civilink.SLF

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieAnimationView
import com.example.civilink.ProfileActivity
import com.example.civilink.WorkSpace
import com.example.civilink.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var fireBaseAuth: FirebaseAuth

    // Regular expression to check if the password meets the required restrictions
    private val PASSWORD_PATTERN: Regex =
        Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[$@$!%*?&])[A-Za-z\\d$@$!%*?&]{8,}")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val animationView: LottieAnimationView = binding.ani
        animationView.playAnimation()
        fireBaseAuth = FirebaseAuth.getInstance()

        binding.button.setOnClickListener {
            val email = binding.editTextTextEmailAddress2.text.toString()
            val pass = binding.editTextNumberPassword.text.toString()
            if (email.isNotEmpty() && pass.isNotEmpty()) {
                if (PASSWORD_PATTERN.matches(pass)) {
                    binding.progressBar.visibility = View.VISIBLE
                    // Use lifecycleScope for coroutines
                    viewLifecycleOwner.lifecycleScope.launch {
                        val result = signInWithEmailAndPassword(email, pass)
                        binding.progressBar.visibility = View.GONE
                        handleAuthResult(result)
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Password must have at least 8 characters, contain at least one uppercase letter, one lowercase letter, one number, and one special character.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please fill both the required fields.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private suspend fun signInWithEmailAndPassword(email: String, password: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                fireBaseAuth.signInWithEmailAndPassword(email, password).await()
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    private fun handleAuthResult(success: Boolean) {
        if (success) {
            val currentUser = fireBaseAuth.currentUser
            if (currentUser != null) {
                if (currentUser.isEmailVerified) {
                    val intent = Intent(requireContext(), ProfileActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Please verify your email to continue",
                        Toast.LENGTH_LONG
                    ).show()
                    fireBaseAuth.signOut()
                }
            }
        } else {
            Toast.makeText(
                requireContext(),
                "Login unsuccessful",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = fireBaseAuth.currentUser
        if (currentUser != null) {
            if (currentUser.isEmailVerified) {
                // Check if the user's profile data exists in the database
                val uid = currentUser.uid
                val databaseReference = FirebaseDatabase.getInstance().reference
                    .child("users")
                    .child(uid)

                databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // User's profile data exists, navigate to the main activity
                            val intent = Intent(requireActivity(), WorkSpace::class.java)
                            startActivity(intent)
                            requireActivity().finish()
                        } else {
                            // User's profile data doesn't exist, redirect to create user profile
                            val intent = Intent(requireActivity(), ProfileActivity::class.java)
                            startActivity(intent)
                            requireActivity().finish()
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Toast.makeText(
                            requireContext(),
                            "Check your Internet connection",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                })
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please verify your email to continue",
                    Toast.LENGTH_LONG
                ).show()
                fireBaseAuth.signOut()
            }
        }
    }
}
