package com.example.civilink

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

class SplashScreenActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var fadeInAnimation: Animation
    var storage: FirebaseStorage?=null
    var database: FirebaseDatabase?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spalsh_screen)

        auth = Firebase.auth
        database = FirebaseDatabase.getInstance()
        storage= FirebaseStorage.getInstance()

        // Load the fade-in animation from XML
        fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.splash_anim)

        val splashScreenView = findViewById<ImageView>(R.id.splashImage)
        splashScreenView.startAnimation(fadeInAnimation)
        Handler().postDelayed({
            val currentUser = auth.currentUser
            if (currentUser != null && currentUser.isEmailVerified) {
                    val uid = auth.currentUser!!.uid
                    database!!.reference
                        .child("users")
                        .child(uid)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    if (dataSnapshot.child("uid").exists() && dataSnapshot.child("name").exists()) {
                                        startActivity(Intent(this@SplashScreenActivity, WorkSpace::class.java))
                                        this@SplashScreenActivity.finish()
                                    } else {
                                        startActivity(Intent(this@SplashScreenActivity, MainActivity::class.java))
                                        this@SplashScreenActivity.finish()
                                    }
                                }
                                else{
                                    startActivity(Intent(this@SplashScreenActivity, MainActivity::class.java))
                                    this@SplashScreenActivity.finish()
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                startActivity(Intent(this@SplashScreenActivity, MainActivity::class.java))
                                this@SplashScreenActivity.finish()
                                Toast.makeText(this@SplashScreenActivity,"Check your Internet connection",
                                    Toast.LENGTH_LONG).show()
                            }
                        })
            } else {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }, 2000) // 3 seconds delay
    }
}
