package com.example.momentia

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PostDetailActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private var googleMap: GoogleMap? = null
    private var postLat = 0.0
    private var postLng = 0.0
    private var postLocation = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_post_detail)

        val imageUrl = intent.getStringExtra("imageUrl") ?: ""
        val userName = intent.getStringExtra("userName") ?: ""
        val userPhoto = intent.getStringExtra("userPhoto") ?: ""
        val description = intent.getStringExtra("description") ?: ""
        val createdAt = intent.getLongExtra("createdAt", 0L)
        postLocation = intent.getStringExtra("location") ?: ""
        postLat = intent.getDoubleExtra("lat", 0.0)
        postLng = intent.getDoubleExtra("lng", 0.0)

        val ivDetailImage = findViewById<ImageView>(R.id.ivDetailImage)
        val ivDetailUserPhoto = findViewById<ImageView>(R.id.ivDetailUserPhoto)
        val tvDetailUserName = findViewById<TextView>(R.id.tvDetailUserName)
        val tvDetailDate = findViewById<TextView>(R.id.tvDetailDate)
        val tvDetailDesc = findViewById<TextView>(R.id.tvDetailDescription)
        val tvDetailLocation = findViewById<TextView>(R.id.tvDetailLocation)

        val tvMapTitle = findViewById<LinearLayout>(R.id.tvMapTitle)
        mapView = findViewById(R.id.mapView)

        tvDetailUserName.text = userName
        tvDetailDesc.text = description
        tvDetailLocation.text = postLocation.ifEmpty { "Ubicación no disponible" }
        tvDetailDate.text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(
            Date(
                createdAt
            )
        )

        Glide.with(this).load(imageUrl).centerCrop().into(ivDetailImage)
        if (userPhoto.isNotEmpty()) Glide.with(this).load(userPhoto).circleCrop()
            .placeholder(R.mipmap.ic_launcher).into(ivDetailUserPhoto)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        mapView.onCreate(savedInstanceState)

        if (postLat != 0.0 || postLng != 0.0) {
            mapView.visibility = View.VISIBLE
            tvMapTitle.visibility = View.VISIBLE
            mapView.getMapAsync(this)

        }
        else {
            mapView.visibility = View.GONE

            tvMapTitle.visibility = View.GONE
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        val position = LatLng(postLat, postLng)
        googleMap?.addMarker(
            MarkerOptions().position(position)
                .title(postLocation.ifEmpty { "Ubicación de la foto" })
        )
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 13f))
        googleMap?.uiSettings?.apply {
            isScrollGesturesEnabled = false; isZoomGesturesEnabled = false
            isRotateGesturesEnabled = false; isTiltGesturesEnabled = false
            isZoomControlsEnabled = true
        }
    }


    override fun onResume() {
        super.onResume(); mapView.onResume()
    }

    override fun onPause() {
        super.onPause(); mapView.onPause()
    }

    override fun onStop() {
        super.onStop(); mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy(); mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory(); mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState); mapView.onSaveInstanceState(outState)
    }


}