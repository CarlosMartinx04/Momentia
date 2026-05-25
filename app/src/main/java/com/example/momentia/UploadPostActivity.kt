package com.example.momentia

import android.Manifest
import android.accessibilityservice.GestureDescription
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.GpsStatus
import android.media.Image
//import android.location.LocationRequest
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class UploadPostActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var ivPreview: ImageView

    private lateinit var etDescription: EditText
    private lateinit var etLocation: EditText
    private lateinit var btnPublish: Button
    private lateinit var tvGpsStatus: TextView

    private var selectedImageUri: Uri? = null
    private var currentLat = 0.0
    private var currentLng = 0.0

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                selectedImageUri = result.data?.data
                selectedImageUri?.let { Glide.with(this).load(it).centerCrop().into(ivPreview) }
            }
        }

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { perms ->
            if (perms[Manifest.permission.ACCESS_FINE_LOCATION] == true || perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true)
                getLocation()
            else tvGpsStatus.text = "Permiso de ubicación denegado"
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_post)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (auth.currentUser == null) {
            finish(); return
        }
        initCloudinary()

        ivPreview = findViewById(R.id.ivPreview)
        etDescription = findViewById(R.id.etDescription)
        etLocation = findViewById(R.id.etLocation)
        btnPublish = findViewById(R.id.btnPublish)
        val gpsLayout = findViewById<LinearLayout>(R.id.tvGpsStatus)
        tvGpsStatus = gpsLayout.getChildAt(1) as TextView

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<TextView>(R.id.tvSelectPhoto).setOnClickListener { openGallery() }
        ivPreview.setOnClickListener { openGallery() }
        btnPublish.setOnClickListener { publishPost() }
        requestLocationPermissionAndGet()
    }

    private fun requestLocationPermissionAndGet() {
        val fine = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (fine || coarse) getLocation()
        else {
            tvGpsStatus.text = "Obteniendo ubicación..."; locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun getLocation() {
        tvGpsStatus.text = "Buscando ubicación GPS..."
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) {
                    currentLat = loc.latitude; currentLng =
                        loc.longitude; reverseGeocode(currentLat, currentLng)
                } else requestFreshLocation()
            }.addOnFailureListener { tvGpsStatus.text = "No se pudo obtener ubicacion" }
        } catch (e: SecurityException) {
            tvGpsStatus.text = "Permiso denegado"
        }
    }

    private fun requestFreshLocation() {
        val request =
            LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L).setMaxUpdates(1).build()
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation ?: return
                currentLat = loc.latitude; currentLng = loc.longitude
                reverseGeocode(currentLat, currentLng)
                fusedLocationClient.removeLocationUpdates(this)
            }
        }
        try {
            fusedLocationClient.requestLocationUpdates(request, callback, Looper.getMainLooper())
        } catch (e: SecurityException) {
            tvGpsStatus.text = "Permiso denegado"
        }
    }

    private fun reverseGeocode(lat: Double, lng: Double) {
        try {
            @Suppress("DEPRECATION")
            val addresses = Geocoder(this, Locale.getDefault()).getFromLocation(lat, lng, 1)
            if (!addresses.isNullOrEmpty()) {
                val a = addresses[0]
                val city = a.locality ?: a.subAdminArea ?: ""
                val country = a.countryName ?: ""
                etLocation.setText(
                    when {
                        city.isNotEmpty() && country.isNotEmpty() -> "$city, $country"; country.isNotEmpty() -> country; else -> "Lat: $lat, Lng: $lng"
                    }
                )
                tvGpsStatus.text = "Ubicación detectada"
            }
        } catch (e: Exception) {
            tvGpsStatus.text = "Coordenadas: $lat, $lng"
        }
    }

    private fun publishPost() {
        val description = etDescription.text.toString().trim()
        val location = etLocation.text.toString().trim()
        if (selectedImageUri == null) {
            Toast.makeText(this, "Selecciona una foto primero", Toast.LENGTH_SHORT).show(); return
        }
        if (description.isEmpty()) {
            etDescription.error = "Escribe una descripción"; return
        }
        btnPublish.isEnabled = false; btnPublish.text = "Subiendo..."
        uploadImage(selectedImageUri!!) { imageUrl -> savePost(imageUrl, description, location) }
    }

    private fun uploadImage(uri: Uri, onSuccess: (String) -> Unit) {
        val postId = db.collection("posts").document().id
        MediaManager.get().upload(uri)
            .option("folder", "travelgram/posts").option("public_id", "post_$postId")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {}
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    onSuccess(resultData["secure_url"] as? String ?: "")
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    runOnUiThread {
                        Toast.makeText(
                            this@UploadPostActivity,
                            "Error: ${error.description}",
                            Toast.LENGTH_LONG
                        ).show(); btnPublish.isEnabled = true; btnPublish.text = "Publicar"
                    }
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            }).dispatch()
    }


    private fun savePost(imageUrl: String, description: String, location: String) {
        val cu = auth.currentUser!!
        db.collection("users").document(cu.uid).get().addOnSuccessListener { userDoc ->
            db.collection("posts").add(
                hashMapOf(
                    "uid" to cu.uid, "userName" to (userDoc.getString("name") ?: ""),
                    "userPhoto" to (userDoc.getString("photoUrl") ?: ""), "imageUrl" to imageUrl,
                    "description" to description, "location" to location,
                    "lat" to currentLat, "lng" to currentLng,
                    "likes" to listOf<String>(), "commentsCount" to 0,
                    "createdAt" to System.currentTimeMillis()
                )
            ).addOnSuccessListener {
                Toast.makeText(this, "¡Publicado! ✓", Toast.LENGTH_SHORT).show(); finish()
            }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        "Error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show(); btnPublish.isEnabled = true; btnPublish.text = "Publicar"
                }
        }
    }

    private fun openGallery() {
        pickImageLauncher.launch(
            Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            ).apply { type = "image/*" })
    }

    private fun initCloudinary() {
        try {
            MediaManager.init(
                this,
                mapOf(
                    "cloud_name" to getString(R.string.cloudinary_cloud_name),
                    "api_key" to getString(R.string.cloudinary_api_key),
                    "api_secret" to getString(R.string.cloudinary_api_secret)
                )
            )
        } catch (e: IllegalStateException) {
        }
    }

}