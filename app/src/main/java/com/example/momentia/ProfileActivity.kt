package com.example.momentia

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var ivProfilePhoto: ImageView
    private lateinit var etName: EditText
    private lateinit var etBio: EditText
    private lateinit var etCountry: EditText
    private lateinit var llCountriesContainer: LinearLayout
    private lateinit var tvSave: TextView

    private val countriesList    = mutableListOf<String>()
    private var selectedImageUri: Uri? = null

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                selectedImageUri = result.data?.data
                selectedImageUri?.let { Glide.with(this).load(it).circleCrop().into(ivProfilePhoto) }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        db   = FirebaseFirestore.getInstance()
        if (auth.currentUser == null) { finish(); return }
        initCloudinary()

        ivProfilePhoto       = findViewById(R.id.ivProfilePhoto)
        etName               = findViewById(R.id.etName)
        etBio                = findViewById(R.id.etBio)
        etCountry            = findViewById(R.id.etCountry)
        llCountriesContainer = findViewById(R.id.llCountriesContainer)
        tvSave               = findViewById(R.id.tvSave)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<TextView>(R.id.tvChangePhoto).setOnClickListener { openGallery() }
        findViewById<ImageButton>(R.id.btnAddCountry).setOnClickListener {
            val country = etCountry.text.toString().trim()
            when {
                country.isEmpty()               -> etCountry.error = "Escribe un país o ciudad"
                countriesList.contains(country) -> Toast.makeText(this, "Ya está en tu lista", Toast.LENGTH_SHORT).show()
                else -> { countriesList.add(country); addCountryItem(country); etCountry.text.clear() }
            }
        }
        tvSave.setOnClickListener { saveProfile() }
        loadUserData()
    }

    private fun loadUserData() {
        db.collection("users").document(auth.currentUser!!.uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    etName.setText(doc.getString("name") ?: "")
                    etBio.setText(doc.getString("bio") ?: "")
                    val photoUrl = doc.getString("photoUrl") ?: ""
                    if (photoUrl.isNotEmpty()) Glide.with(this).load(photoUrl).circleCrop().placeholder(R.mipmap.ic_launcher).into(ivProfilePhoto)
                    @Suppress("UNCHECKED_CAST")
                    val countries = doc.get("countries") as? List<String> ?: emptyList()
                    countriesList.clear(); countriesList.addAll(countries)
                    llCountriesContainer.removeAllViews()
                    countries.forEach { addCountryItem(it) }
                }
            }
            .addOnFailureListener { Toast.makeText(this, "Error al cargar perfil", Toast.LENGTH_SHORT).show() }
    }


    private fun addCountryItem(country: String) {
        val itemView = LayoutInflater.from(this).inflate(R.layout.item_country, llCountriesContainer, false)
        itemView.findViewById<TextView>(R.id.tvCountryName).text = country
        itemView.findViewById<ImageButton>(R.id.btnDeleteCountry).setOnClickListener {
            countriesList.remove(country); llCountriesContainer.removeView(itemView)
        }
        llCountriesContainer.addView(itemView)
    }

    private fun saveProfile() {
        val name = etName.text.toString().trim()
        val bio  = etBio.text.toString().trim()
        if (name.isEmpty()) { etName.error = "El nombre no puede estar vacío"; return }
        tvSave.isEnabled = false; tvSave.text = "Guardando..."
        if (selectedImageUri != null) uploadPhoto(selectedImageUri!!) { saveToFirestore(name, bio, it) }
        else saveToFirestore(name, bio, null)
    }

    private fun uploadPhoto(uri: Uri, onSuccess: (String) -> Unit) {
        MediaManager.get().upload(uri)
            .option("folder", "momentia/profiles").option("public_id", "profile_${auth.currentUser!!.uid}")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {}
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                override fun onSuccess(requestId: String, resultData: Map<*, *>) { onSuccess(resultData["secure_url"] as? String ?: "") }
                override fun onError(requestId: String, error: ErrorInfo) {
                    runOnUiThread { Toast.makeText(this@ProfileActivity, "Error: ${error.description}", Toast.LENGTH_LONG).show(); tvSave.isEnabled = true; tvSave.text = "Guardar" }
                }
                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            }).dispatch()
    }

    private fun saveToFirestore(name: String, bio: String, photoUrl: String?) {
        val updates = hashMapOf<String, Any>("name" to name, "bio" to bio, "countries" to countriesList)
        if (photoUrl != null) updates["photoUrl"] = photoUrl
        db.collection("users").document(auth.currentUser!!.uid).update(updates)
            .addOnSuccessListener { Toast.makeText(this, "Perfil guardado", Toast.LENGTH_SHORT).show(); tvSave.isEnabled = true; tvSave.text = "Guardar"; selectedImageUri = null }
            .addOnFailureListener { e -> Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show(); tvSave.isEnabled = true; tvSave.text = "Guardar" }
    }

    private fun openGallery() { pickImageLauncher.launch(
        Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        ).apply { type = "image/*" }) }

    private fun initCloudinary() {
        try { MediaManager.init(this, mapOf("cloud_name" to getString(R.string.cloudinary_cloud_name), "api_key" to getString(R.string.cloudinary_api_key), "api_secret" to getString(R.string.cloudinary_api_secret))) }
        catch (e: IllegalStateException) { }
    }


}