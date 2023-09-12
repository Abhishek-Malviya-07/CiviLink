package com.example.civilink.SLF
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.example.civilink.databinding.FragmentForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordFragment : Fragment() {
    private var binding: FragmentForgotPasswordBinding? = null
    private lateinit var auth: FirebaseAuth

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        val view = binding!!.root
        auth = FirebaseAuth.getInstance()

        val animationView: LottieAnimationView = binding!!.ani
        animationView.playAnimation()

        binding!!.reset.setOnClickListener {
            val proBar = binding!!.progressBar
            proBar.visibility = View.VISIBLE
            val email = binding!!.EmailAddress.text.toString()

                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        proBar.visibility = View.GONE
                        if (task.isSuccessful) {
                            // Password reset email sent successfully
                            Toast.makeText(
                                requireContext(),
                                "Password reset email sent.",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            // Password reset email failed to send
                            Toast.makeText(
                                requireContext(),
                                "Failed to send password reset email,Check Internet connection",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
        }
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
