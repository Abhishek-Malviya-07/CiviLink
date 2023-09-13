package com.example.civilink.SLF

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieAnimationView
import com.example.civilink.ProfileActivity
import com.example.civilink.R
import com.example.civilink.WorkSpace
import com.example.civilink.databinding.FragmentSignUpBinding
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

@Suppress("DEPRECATION")
class SignUpFragment : Fragment() {
    private var _binding: FragmentSignUpBinding? = null
    var storage: FirebaseStorage?=null
    private lateinit var navController: NavController
    var auth : FirebaseAuth? = null
    var database: FirebaseDatabase?=null
    private val binding get() = _binding!!
    private lateinit var fireBaseAuth: FirebaseAuth
    private lateinit var googleApiClient: GoogleApiClient
    private val RC_SIGN_IN = 9001 // Request code for Google Sign-In
    private var dialog :ProgressDialog? = null
    private var googleSignInClient: GoogleSignInClient? = null

    private val PASSWORD_PATTERN: Regex =
        Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[$@$!%*?&])[A-Za-z\\d$@$!%*?&]{8,}")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val animationView: LottieAnimationView = binding.ani
        navController = findNavController()
        animationView.playAnimation()
        database = FirebaseDatabase.getInstance()
        storage= FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        fireBaseAuth = FirebaseAuth.getInstance()


        if (googleSignInClient == null) {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
            googleApiClient = GoogleApiClient.Builder(requireContext())
                .enableAutoManage(requireActivity()) {
                }
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build()
        }

        binding.googleSignIn.setOnClickListener {
            dialog = ProgressDialog(requireContext())
            dialog!!.setMessage("Loading...")
            dialog!!.setCancelable(false)
            dialog!!.show()
            signInWithGoogle()
        }

        binding.cirLoginButton.setOnClickListener {
            val email = binding.editTextTextEmailAddress2.text.toString()
            val pass = binding.editTextNumberPassword.text.toString()
            if (email.isNotEmpty() && pass.isNotEmpty()) {
                if (PASSWORD_PATTERN.matches(pass)) {
                    binding.progressBar.visibility = View.VISIBLE
                    // Use lifecycleScope for coroutines
                    viewLifecycleOwner.lifecycleScope.launch {
                        val result = createUserWithEmailAndPassword(email, pass)
                        binding.progressBar.visibility = View.GONE
                        handleAuthResult(result, email, pass)
                    }
                }else {
                    showCustomSeekBarNotification(
                        R.raw.errorlottie, // Change to your desired icon
                        "Password must have at least 8 characters, contain at least one uppercase, one lowercase, one number, and one special character.",
                        )
                }
            }else {
                if(email.isEmpty() && pass.isEmpty()){
                    showCustomSeekBarNotification(
                        R.raw.verify,
                        "*Email is a required field,\n*Password is a required field.",
                    )
                }
                else if(email.isEmpty()){
                    binding.editTextTextEmailAddress2.error="*required field"
                }
                else if(pass.isEmpty()){
                    showCustomSeekBarNotification(
                        R.raw.verify, // Change to your desired icon
                        "*Password is a required field",
                    )
                }
            }
        }
    }
    private fun signInWithGoogle() {
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient)
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val result = data?.let { Auth.GoogleSignInApi.getSignInResultFromIntent(it) }
            if (result!!.isSuccess) {
                navController.navigate(R.id.action_signUpFragment_to_loginFragment)
                dialog!!.dismiss()
                showCustomLottieToast(
                    R.raw.donelottie, // Change to your desired icon
                    "Linking with google...",
                )
                val account = result.signInAccount
                firebaseAuthWithGoogle(account!!)
                requireActivity().finish()
            } else {
                navController.navigate(R.id.action_signUpFragment_to_loginFragment)
                dialog!!.dismiss()
                showCustomSeekBarNotification(
                    R.raw.networkerror, // Change to your desired icon
                    "Google Sign-In failed, Network problem.",
                )
            }
        }
    }
    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        fireBaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    showCustomLottieToast(
                        R.raw.donelottie, // Change to your desired icon
                        "Login successful...",
                    )
                }
          }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private suspend fun createUserWithEmailAndPassword(email: String, password: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                fireBaseAuth.createUserWithEmailAndPassword(email, password).await()
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    private fun handleAuthResult(success: Boolean, email: String, password: String) {
        if (success) {
            val currentUser = fireBaseAuth.currentUser
            if (currentUser != null) {
                if (!currentUser.isEmailVerified) {
                    sendEmailVerification(email)
                    showCustomLottieToast(
                        R.raw.donelottie, // Change to your desired icon
                        "Sending E-mail...",
                    )
                    clearFields()
                    findNavController().navigate(R.id.action_signUpFragment_to_loginFragment)
                } else {
                    showCustomLottieToast(
                        R.raw.verify, // Change to your desired icon
                        "Please fill email and password correctly",
                        )
                    clearFields()
                }
            }
        } else {
            showCustomLottieToast(
                R.raw.errorlottie,
                "Please fill email and password correctly or user already exists",
                )
        }
    }

    private fun clearFields() {
        binding.editTextTextEmailAddress2.text!!.clear()
        binding.editTextNumberPassword.text!!.clear()
    }

    private fun sendEmailVerification(email: String) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.sendEmailVerification()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showCustomLottieToast(R.raw.donelottie, "Email verification sent.")
                } else {
                    showCustomLottieToast(R.raw.errorlottie, "Failed to send email verification.")
                }
            }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = fireBaseAuth.currentUser
        if (currentUser != null) {
            if (currentUser.isEmailVerified) {
                if (auth!!.currentUser != null) {
                    val uid = auth!!.currentUser!!.uid
                    database!!.reference
                        .child("users")
                        .child(uid)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    if (dataSnapshot.child("uid").exists() && dataSnapshot.child("name").exists()) {
                                        startActivity(Intent(requireContext(), WorkSpace::class.java))
                                        requireActivity().finish()
                                    } else {
                                        startActivity(Intent(requireContext(), ProfileActivity::class.java))
                                        requireActivity().finish()
                                    }
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                showCustomSeekBarNotification(
                                    R.raw.networkerror,
                                    "Please Check your Internet connection",
                                )
                            }
                        })
                }

            } else {
                showCustomLottieToast(
                    R.raw.errorlottie,
                    "Please verify your E-mail to continue,check your E-mail",
                )
//                Toast.makeText(requireContext(), "Please verify your email to continue", Toast.LENGTH_LONG).show()
                fireBaseAuth.signOut()
            }
        }
    }
    // Function to show custom SeekBar notification with Lottie animation

    private fun showCustomSeekBarNotification(animationResId: Int, message: String) {
        // Inflate the custom SeekBar layout
        val inflater = LayoutInflater.from(requireContext())
        val customSeekBarView = inflater.inflate(R.layout.custom_seekbar_layout1, null)

        // Customize the layout elements
        val lottieAnimationView = customSeekBarView.findViewById<LottieAnimationView>(R.id.lottieAnimationView)
        val textViewMessage = customSeekBarView.findViewById<TextView>(R.id.textViewMessage)

        // Set Lottie animation resource
        lottieAnimationView.setAnimation(animationResId) // Replace with your animation resource
        lottieAnimationView.playAnimation()

        // Set the message
        textViewMessage.text = message

        // Use a Dialog to display the custom SeekBar notification
        val customSeekBarDialog = Dialog(requireContext())
        customSeekBarDialog.setContentView(customSeekBarView)

        // Optional: Set dialog properties (e.g., background, dimensions, etc.)
        customSeekBarDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        customSeekBarDialog.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        // Show the custom SeekBar notification
        customSeekBarDialog.show()
    }
    private fun showCustomLottieToast(animationResId: Int, message: String) {
        val inflater = layoutInflater
        val layout = inflater.inflate(R.layout.custom_toast_lottie_layout, null)

        // Customize the layout elements
        val lottieAnimationView = layout.findViewById<LottieAnimationView>(R.id.lottieAnimationView)
        val textViewMessage = layout.findViewById<TextView>(R.id.textViewMessage)

        // Set the Lottie animation resource
        lottieAnimationView.setAnimation(animationResId)
        lottieAnimationView.playAnimation()

        textViewMessage.text = message

        val toast = Toast(requireContext())
        toast.duration = Toast.LENGTH_LONG
        toast.view = layout
        toast.show()
    }
}
