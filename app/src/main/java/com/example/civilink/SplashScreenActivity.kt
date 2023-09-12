package com.example.civilink

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
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
        Handler().postDelayed({
            val currentUser = auth.currentUser
            if (currentUser != null && currentUser.isEmailVerified) {
                startActivity(Intent(this, WorkSpace::class.java))
            } else {
                startActivity(Intent(this, MainActivity::class.java))
            }
            finish()
        }, 3000) // 3 seconds delay
    }
}
