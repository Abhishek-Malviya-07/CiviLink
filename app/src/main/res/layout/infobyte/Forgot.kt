package com.example.infobyte

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.infobyte.databinding.ActivityForgotBinding
import com.google.firebase.auth.FirebaseAuth

class Forgot : AppCompatActivity() {
    private var binding : ActivityForgotBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =  ActivityForgotBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        val auth = FirebaseAuth.getInstance()

// Get the user's email address from the input field or wherever you have it
        val email = binding!!.EmailAddress
        binding!!.reset.setOnClickListener {
            val proBar = binding!!.progressBar
            proBar.visibility = View.VISIBLE
            val email = binding!!.EmailAddress.text.toString()

            try {
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        proBar.visibility = View.GONE
                        if (task.isSuccessful) {
                            // Password reset email sent successfully
                            super.onBackPressed()
                            Toast.makeText(this, "Password reset email sent.", Toast.LENGTH_SHORT).show()
                        } else {
                            // Password reset email failed to send
                            Toast.makeText(this, "Failed to send password reset email.", Toast.LENGTH_SHORT).show()
                        }
                    }
            } catch (e: Exception) {
                proBar.visibility = View.GONE
                Log.e("ForgotActivity", "Error sending password reset email: ${e.message}")
                Toast.makeText(this, "An error occurred. Please try again later.", Toast.LENGTH_SHORT).show()
            }
        }

    }
}