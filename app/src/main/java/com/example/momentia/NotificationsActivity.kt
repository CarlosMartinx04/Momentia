package com.example.momentia

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class NotificationsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: NotificationAdapter
    private val notifList = mutableListOf<Notification>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        if (auth.currentUser == null) {
            finish(); return
        }

        val rvNotifications = findViewById<RecyclerView>(R.id.rvNotifications)
        val tvEmpty = findViewById<TextView>(R.id.tvEmpty)
        val tvMarkAllRead = findViewById<TextView>(R.id.tvMarkAllRead)

        adapter = NotificationAdapter(notifList)
        rvNotifications.layoutManager = LinearLayoutManager(this)
        rvNotifications.adapter = adapter

        tvMarkAllRead.setOnClickListener { markAllAsRead() }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_notifications
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_feed -> {
                    startActivity(Intent(this, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    })
                    overridePendingTransition(0, 0)
                    ; true

                }

                R.id.nav_notifications -> true
                R.id.nav_profile -> {
                    startActivity(
                        Intent(this, ProfileActivity::class.java)
                    )
                    overridePendingTransition(0, 0)
                    ; true
                }

                else -> false
            }
        }

        db.collection("notifications")
            .whereEqualTo("toUid", auth.currentUser!!.uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                notifList.clear()
                for (doc in documents) {
                    notifList.add(
                        Notification(
                            id = doc.id,
                            fromName = doc.getString("fromName") ?: "",
                            type = doc.getString("type") ?: "",
                            postId = doc.getString("postId") ?: "",
                            postImage = doc.getString("postImage") ?: "",
                            message = doc.getString("message") ?: "",
                            read = doc.getBoolean("read") ?: false,
                            createdAt = doc.getLong("createdAt") ?: 0L
                        )
                    )
                }
                adapter.notifyDataSetChanged()
                tvEmpty.visibility = if (notifList.isEmpty()) View.VISIBLE else View.GONE
                rvNotifications.visibility = if (notifList.isEmpty()) View.GONE else View.VISIBLE
                markAllAsRead()
            }
            .addOnFailureListener {
                Toast.makeText(
                    this,
                    "Error al cargar notificaciones",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun markAllAsRead() {
        val uid = auth.currentUser!!.uid
        db.collection("notifications").whereEqualTo("toUid", uid).whereEqualTo("read", false).get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) return@addOnSuccessListener
                val batch = db.batch()
                documents.forEach { batch.update(it.reference, "read", true) }
                batch.commit().addOnSuccessListener {
                    notifList.forEachIndexed { i, n ->
                        if (!n.read) notifList[i] = n.copy(read = true)
                    }
                    adapter.notifyDataSetChanged()
                }
            }
    }
}

