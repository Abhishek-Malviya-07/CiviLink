package com.example.civilink


import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.airbnb.lottie.LottieAnimationView
import com.example.civilink.databinding.ActivityForgotBinding
import com.google.firebase.auth.FirebaseAuth

class Forgot : AppCompatActivity() {
    private var binding : ActivityForgotBinding? = null
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =  ActivityForgotBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        val auth = FirebaseAuth.getInstance()
        // Hide the system navigation bar
        val animationView : LottieAnimationView = findViewById(R.id.ani)
        animationView.playAnimation()

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