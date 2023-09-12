package com.example.civilink
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment

// In your MainActivity
class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private lateinit var btnSignUp :Button
    private lateinit var btnLogin :Button
    private lateinit var divider :View
    private lateinit var forgotText : TextView

    //
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnSignUp = findViewById(R.id.btnSignUp)
        btnLogin = findViewById(R.id.btnLogin)
        divider = findViewById(R.id.divider)
        forgotText = findViewById(R.id.forgotText)

        // Find the NavHostFragment using the fragment container view's ID
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragmentContainerView) as NavHostFragment

        // Get the NavController from the NavHostFragment
        navController = navHostFragment.navController

        // Add a destination changed listener to update button backgrounds
        navController.addOnDestinationChangedListener { _, destination, _ ->
            updateButtonBackground(destination.id)
        }
    }
    fun navigateToSignUp(view: View) {
        btnSignUp.visibility = View.VISIBLE
        btnLogin.visibility = View.VISIBLE
        forgotText.visibility=View.GONE
        navController = findNavController(R.id.fragmentContainerView)
        val currentDestinationId = navController.currentDestination?.id
        if (currentDestinationId == R.id.loginFragment) {
            if (!navController.popBackStack()) {
                navController.navigate(R.id.action_loginFragment_to_signUpFragment)
            }
        } else if (currentDestinationId != R.id.signUpFragment) {
            navController.navigate(R.id.action_loginFragment_to_signUpFragment)
        }
    }

    fun navigateToLogin(view: View) {
        btnSignUp.visibility = View.VISIBLE
        btnLogin.visibility = View.VISIBLE
        forgotText.visibility=View.GONE
        navController = findNavController(R.id.fragmentContainerView)
        if (navController.currentDestination?.id != R.id.loginFragment) {
            navController.navigate(R.id.action_signUpFragment_to_loginFragment)
        }
    }

    private fun updateButtonBackground(destinationId: Int) {
        when (destinationId) {
            R.id.signUpFragment -> {
                btnSignUp.setBackgroundResource(R.drawable.otp_box)
                btnLogin.setBackgroundResource(R.color.clr_bg)
            }
            R.id.loginFragment -> {
                btnSignUp.setBackgroundResource(R.color.clr_bg)
                btnLogin.setBackgroundResource(R.drawable.otp_box)
            }
            else -> {
                // Handle other fragments if needed
            }
        }
    }
    fun forgot(view: View) {
        btnSignUp.visibility = View.GONE
        btnLogin.visibility = View.GONE
        divider.visibility = View.GONE
        forgotText.visibility=View.VISIBLE
        navController = findNavController(R.id.fragmentContainerView)
        navController.navigate(R.id.action_loginFragment_to_forgotPasswordFragment)
    }
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Check if the current destination is the "Forgot Password" fragment
        if (navController.currentDestination?.id == R.id.forgotPasswordFragment) {
            btnSignUp.visibility = View.VISIBLE
            btnLogin.visibility = View.VISIBLE
            divider.visibility=View.VISIBLE
            forgotText.visibility=View.GONE
            navController.popBackStack()
        } else {
            super.onBackPressed()
        }
    }
}
