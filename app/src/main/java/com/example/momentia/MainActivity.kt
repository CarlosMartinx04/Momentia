package com.example.momentia

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore


    private lateinit var adapter: PostAdapter
    private val postsList = mutableListOf<Post>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        MobileAds.initialize(this)

        MobileAds.setRequestConfiguration(
            com.google.android.gms.ads.RequestConfiguration.Builder()
                .setTestDeviceIds(listOf("57F1F47C73A6883AD0CE960323F559B7"))
                .build()
        )

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()


        if (auth.currentUser == null) {
            goToLogin(); return
        }

        findViewById<ImageButton>(R.id.btnLogout).setOnClickListener {
            auth.signOut()
            goToLogin()
        }


        val rvFeed = findViewById<RecyclerView>(R.id.rvFeed)
        adapter = PostAdapter(postsList, this)
        rvFeed.layoutManager = LinearLayoutManager(this)
        rvFeed.adapter = adapter

        findViewById<ImageButton>(R.id.fabUpload).setOnClickListener {
            startActivity(Intent(this, UploadPostActivity::class.java))
            overridePendingTransition(0, 0)
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_feed
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_feed -> true
                R.id.nav_notifications -> {
                    startActivity(
                        Intent(this, NotificationsActivity::class.java)
                    )
                    overridePendingTransition(0, 0)
                    ; true

                }

                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java)); true
                }

                else -> false
            }
        }
        loadFeed()
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.destroyAds()
    }

    override fun onResume() {
        super.onResume(); loadFeed()
    }

    private fun loadFeed() {
        db.collection("posts").orderBy("createdAt", Query.Direction.DESCENDING).get()
            .addOnSuccessListener { document ->
                postsList.clear()
                for (doc in document) {
                    @Suppress("UNCHECKED_CAST")
                    postsList.add(
                        Post(
                            id = doc.id,
                            uid = doc.getString("uid") ?: "",
                            userName = doc.getString("userName") ?: "",
                            userPhoto = doc.getString("userPhoto") ?: "",
                            imageUrl = doc.getString("imageUrl") ?: "",
                            description = doc.getString("description") ?: "",
                            location = doc.getString("location") ?: "",
                            lat = doc.getDouble("lat") ?: 0.0,
                            lng = doc.getDouble("lng") ?: 0.0,
                            likes = doc.get("likes") as? List<String> ?: emptyList(),
                            createdAt = doc.getLong("createdAt") ?: 0L
                        )
                    )
                }
                adapter.notifyDataSetChanged()

            }
            .addOnFailureListener {
                Toast.makeText(
                    this,
                    "Error al cargar el feed",
                    Toast.LENGTH_SHORT
                ).show()
            }

    }

    private fun goToLogin() {
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
    }


}