package com.example.

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.civilink.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class Login_Activity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var fireBaseAuth: FirebaseAuth
    private lateinit var progressBar: ProgressBar

    // Regular expression to check if the password meets the required restrictions
    private val PASSWORD_PATTERN: Regex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[$@$!%*?&])[A-Za-z\\d$@$!%*?&]{8,}")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fireBaseAuth = FirebaseAuth.getInstance()
        progressBar = binding.progressBar

        // Click listener for the "Sign Up" text view to open the sign up activity
        binding.text1.setOnClickListener {
            finish()
        }

        // Click listener for the login button to sign in the user with their email and password
        binding.button.setOnClickListener {
            val email = binding.editTextTextEmailAddress2.text.toString()
            val pass = binding.editTextNumberPassword.text.toString()
            if (email.isNotEmpty() && pass.isNotEmpty()) {
                // Check if the password meets the required restrictions
                if (PASSWORD_PATTERN.matches(pass)) {
                    progressBar.visibility = View.VISIBLE
                    fireBaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener {
                        progressBar.visibility = View.GONE
                        if (it.isSuccessful) {
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }else{
                            Toast.makeText(this,"Login unsuccessful",Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Password must have at least 8 characters, contain at least one uppercase letter, one lowercase letter, one number, and one special character.", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, "Please fill both the required fields.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = fireBaseAuth.currentUser
        if (currentUser != null) {
            if (currentUser.isEmailVerified) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Please verify your email to continue", Toast.LENGTH_LONG).show()
                fireBaseAuth.signOut()
            }
        }
    }

    fun forgot(view: View) {
        val intent = Intent(this,Forgot::class.java)
        startActivity(intent)
    }
}
