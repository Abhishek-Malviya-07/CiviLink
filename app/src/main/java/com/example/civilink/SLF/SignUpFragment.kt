package com.example.civilink.SLF

import android.app.Dialog
import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieAnimationView
import com.example.civilink.ProfileActivity
import com.example.civilink.R
import com.example.civilink.WorkSpace
import com.example.civilink.data.User
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
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Password must have at least 8 characters, contain at least one uppercase, one lowercase, one number, and one special character.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }else {
                if(email.isEmpty()){
                    binding.editTextTextEmailAddress2.error="*required field"
                }else{
                    binding.editTextTextEmailAddress2.error=null
                }
                if(pass.isEmpty()){
                    binding.editTextNumberPassword.error="*required field"
                }
                else{
                    binding.editTextNumberPassword.error=null
                }
                if(email.isEmpty() && pass.isEmpty()){
                    binding.editTextTextEmailAddress2.error="*required field"
                    binding.editTextNumberPassword.error="*required field"
                }
                else{
                    binding.editTextTextEmailAddress2.error=null
                    binding.editTextNumberPassword.error=null
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
                val account = result.signInAccount
                firebaseAuthWithGoogle(account!!)
                val intent = Intent(requireContext(), ProfileActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Something went Wrong",
                    Toast.LENGTH_LONG
                ).show()
                // Google Sign-In failed, handle the error
            }
            dialog!!.dismiss()
        }
    }
    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        fireBaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {

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
                    Toast.makeText(
                        requireContext(),
                        "A verification email has been sent to your email address. Please verify your email to continue.",
                        Toast.LENGTH_LONG
                    ).show()
                    clearFields()
                    findNavController().navigate(R.id.action_signUpFragment_to_loginFragment)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Please fill email and password correctly",
                        Toast.LENGTH_SHORT
                    ).show()
                    clearFields()
                }
            }
        } else {
            Toast.makeText(
                requireContext(),
                "Please fill email and password correctly or user already exists",
                Toast.LENGTH_SHORT
            ).show()
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
                    Log.d(TAG, "Email verification sent.")
                } else {
                    Log.e(TAG, "Failed to send email verification.", task.exception)
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
                                Toast.makeText(requireContext(),"Check your Internet connection",Toast.LENGTH_LONG).show()
                            }
                        })
                }

            } else {
                Toast.makeText(requireContext(), "Please verify your email to continue", Toast.LENGTH_LONG).show()
                fireBaseAuth.signOut()
            }
        }
        else{

        }
    }
}
