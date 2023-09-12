@file:Suppress("DEPRECATION")

package com.example.civilink

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.civilink.data.User
import com.example.civilink.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

@Suppress("DEPRECATION")

class ProfileActivity : AppCompatActivity() {

    var binding : ActivityProfileBinding? = null
    var auth : FirebaseAuth? = null
    var database: FirebaseDatabase?=null
    var uid : String? = null
    var storage:FirebaseStorage?=null
    var selectedImage : Uri? = null
    private var profileImage :ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        supportActionBar?.hide()

        val dialog = ProgressDialog(this)
        dialog.setMessage("Loading...")
        dialog.setCancelable(false)

        database = FirebaseDatabase.getInstance()
        storage= FirebaseStorage.getInstance()
        auth=FirebaseAuth.getInstance()

        profileImage =binding!!.profileImage

        profileImage!!.setOnClickListener {

            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, 17)
        }


        binding!!.set.setOnClickListener {
            val name = binding!!.name.text.toString()
            if(name.isEmpty()) {
                binding!!.name.error = "please enter your name"
            }
            if (selectedImage!= null)
            {
                val reference = storage!!.reference.child("Profile").child(auth!!.uid!!)
                reference.putFile(selectedImage!!).addOnCompleteListener{Task ->
                    if(Task.isSuccessful)
                    {
                        reference.downloadUrl.addOnSuccessListener { uri->
                            val imageUri = uri.toString()
                            uid = auth!!.uid
                            val userName = binding!!.name.text.toString()
                            val email = auth!!.currentUser!!.email
                            val user = User(uid, userName, imageUri, email)

                            database!!.reference
                                .child("users")
                                .child(uid!!)
                                .setValue(user).addOnCompleteListener {
                                    dialog.dismiss()
                                    startActivity(Intent(this, MainActivity::class.java))
                                    finish()
                                }
                        }
                    }
                }
            }
            else {
                Toast.makeText(this,"select image",Toast.LENGTH_SHORT).show()
            }

            if(selectedImage!=null&&name.isNotEmpty())
            {
                dialog.show()
            }

        }

    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode==17)
        {
            if(data != null)
            {
                selectedImage=data.data

                profileImage!!.setImageURI(selectedImage)
            }
        }
    }
    override fun onStart() {
        super.onStart()

        // Check if the user is logged in
        if (auth!!.currentUser != null) {
            val uid = auth!!.currentUser!!.uid

            // Check if the user's profile data exists in the database
            database!!.reference
                .child("users")
                .child(uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // User's profile data exists, check if it contains all necessary fields
                            if (dataSnapshot.child("uid").exists() && dataSnapshot.child("name").exists()) {
                                // User's profile data is complete, navigate to main activity
                                startActivity(Intent(this@ProfileActivity, WorkSpace::class.java))
                                finish()
                            } else {

                            }
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Toast.makeText(this@ProfileActivity,"Check your Internet connection",Toast.LENGTH_LONG).show()
                    }
                })
        }
    }
}
