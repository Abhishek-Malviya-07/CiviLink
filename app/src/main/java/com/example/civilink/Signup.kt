package com.example.civilink

import android.R
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.example.civilink.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth


class SignUp_activity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private lateinit var fireBaseAuth: FirebaseAuth

    private val PASSWORD_PATTERN: Regex =
        Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[$@$!%*?&])[A-Za-z\\d$@$!%*?&]{8,}")

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val animationView :LottieAnimationView = binding.ani
        animationView.playAnimation()
        fireBaseAuth = FirebaseAuth.getInstance()
        binding.text2.setOnClickListener {
            val intent = Intent(this, Login_Activity::class.java)
            startActivity(intent)
        }
        binding.cirLoginButton.setOnClickListener {
            val email = binding.editTextTextEmailAddress2.text.toString()
            val pass = binding.editTextNumberPassword.text.toString()
            if (PASSWORD_PATTERN.matches(pass)) {
                if (email.isNotEmpty() && pass.isNotEmpty()) {
                    binding.progressBar.visibility = View.VISIBLE
                    fireBaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener {
                        binding.progressBar.visibility = View.GONE
                        if (it.isSuccessful) {
                            fireBaseAuth.currentUser?.sendEmailVerification()
                                ?.addOnSuccessListener {
                                    Toast.makeText(
                                        this,
                                        "Please verify your Email",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    val intent = Intent(this, Login_Activity::class.java)
                                    startActivity(intent)
                                }
                                ?.addOnFailureListener {
                                    Toast.makeText(this, "Internet error", Toast.LENGTH_LONG).show()
                                }
                        } else {
                            Toast.makeText(
                                this,
                                "Internet error or user already exist",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    }
                }else {
                    Toast.makeText(this, "Please fill the email and password", Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                Toast.makeText(
                    this,
                    "Password must have at least 8 characters, contain at least one uppercase letter, one lowercase letter, one number and one special character.",
                    Toast.LENGTH_LONG
                ).show()
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
}
