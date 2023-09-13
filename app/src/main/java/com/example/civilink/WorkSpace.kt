package com.example.civilink

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import android.Manifest
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

    class WorkSpace : AppCompatActivity(), OnMapReadyCallback {

        private lateinit var mapView: MapView
        private lateinit var auth: FirebaseAuth
        private lateinit var fireBaseAuth: FirebaseAuth
        private val LOCATION_PERMISSION_REQUEST_CODE = 123

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_work_space)

            // Initialize the MapView
            fireBaseAuth = FirebaseAuth.getInstance()
            mapView = findViewById(R.id.mapView)
            mapView.onCreate(savedInstanceState)
            mapView.getMapAsync(this)

            // Check if permissions are granted, and if not, request them
            requestLocationPermissions()
        }
        private fun requestLocationPermissions() {
            val fineLocationPermission = Manifest.permission.ACCESS_FINE_LOCATION
            val coarseLocationPermission = Manifest.permission.ACCESS_COARSE_LOCATION

            val fineLocationPermissionGranted = ContextCompat.checkSelfPermission(this, fineLocationPermission) == PackageManager.PERMISSION_GRANTED
            val coarseLocationPermissionGranted = ContextCompat.checkSelfPermission(this, coarseLocationPermission) == PackageManager.PERMISSION_GRANTED

            if (!fineLocationPermissionGranted || !coarseLocationPermissionGranted) {
                // Request permissions
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(fineLocationPermission, coarseLocationPermission),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }
        }
            override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)

                if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
                    if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                        // Location permissions granted, you can now use the map
                    } else {
                        // Location permissions not granted, handle accordingly (e.g., show a message or disable map features)
                    }
                }
            }

        override fun onMapReady(googleMap: GoogleMap) {
            // Customize the map and add markers, polylines, etc. here
            val location = LatLng(0.0, 0.0) // Replace with your desired location
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 12.0f))
        }

        // Handle the lifecycle of the MapView
        override fun onResume() {
            super.onResume()
            mapView.onResume()
        }

        override fun onPause() {
            super.onPause()
            mapView.onPause()
        }

        override fun onDestroy() {
            super.onDestroy()
            mapView.onDestroy()
        }

        override fun onSaveInstanceState(outState: Bundle) {
            super.onSaveInstanceState(outState)
            mapView.onSaveInstanceState(outState)
        }

        override fun onLowMemory() {
            super.onLowMemory()
            mapView.onLowMemory()
        }

    override fun onStart() {
        super.onStart()
        val currentUser = fireBaseAuth.currentUser
        if (currentUser != null) {
            if (currentUser.isEmailVerified) {
                // Check if the user's profile data exists in the database
                val uid = currentUser.uid
                val databaseReference = FirebaseDatabase.getInstance().reference
                    .child("users")
                    .child(uid)

                databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (!dataSnapshot.exists()) {
                            // User's profile data doesn't exist, redirect to create user profile
                            val intent = Intent(this@WorkSpace, ProfileActivity::class.java)
                            startActivity(intent)
                            this@WorkSpace.finish()
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Toast.makeText(
                            this@WorkSpace,
                            "Check your Internet connection",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                })
            } else {
                Toast.makeText(
                    this@WorkSpace,
                    "Please verify your email to continue",
                    Toast.LENGTH_LONG
                ).show()
                fireBaseAuth.signOut()
            }
        }
    }

}