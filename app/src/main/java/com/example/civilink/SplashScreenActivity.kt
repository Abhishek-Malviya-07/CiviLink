package com.example.civilink

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.civilink.loginsignupforgot.Login_Activity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SplashScreenActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var fadeInAnimation: Animation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spalsh_screen)

        auth = Firebase.auth

        // Load the fade-in animation from XML
        fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.splash_anim)

        val splashScreenView = findViewById<ImageView>(R.id.splashImage)
        splashScreenView.startAnimation(fadeInAnimation)

        // Delayed navigation to the next activity after 3 seconds (3000 milliseconds)
        Handler().postDelayed({
            // Check if the user is already signed in and their email is verified
            val currentUser = auth.currentUser
            if (currentUser != null && currentUser.isEmailVerified) {
                // User is signed in and email is verified, navigate to MainActivity
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                // User is not signed in or email is not verified, navigate to LoginActivity
                startActivity(Intent(this, Login_Activity::class.java))
            }

            // Close the splash screen activity to prevent going back to it
            finish()
        }, 3000) // 3 seconds delay
    }
}
