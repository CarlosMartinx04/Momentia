package com.example.momentia

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val etName = findViewById<EditText>(R.id.etName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<TextView>(R.id.tvGoToLogin).setOnClickListener { finish() }

        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (name.isEmpty()) {
                etName.error = "Ingresa tu nombre"; return@setOnClickListener
            }
            if (email.isEmpty()) {
                etEmail.error = "Ingresa tu correo"; return@setOnClickListener
            }
            if (password.isEmpty()) {
                etPassword.error = "Ingresa una contraseña"; return@setOnClickListener
            }
            if (password.length < 6) {
                etPassword.error = "Mínimo 6 caracteres"; return@setOnClickListener
            }

            btnRegister.isEnabled = false; btnRegister.text = "Creando cuenta..."

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { authResult ->
                    val userId = authResult.user!!.uid
                    val userData = hashMapOf(
                        "uid" to userId, "name" to name, "email" to email,
                        "bio" to "", "photoUrl" to "",
                        "countries" to listOf<String>(),
                        "createdAt" to System.currentTimeMillis()
                    )
                    db.collection("users").document(userId).set(userData)
                        .addOnSuccessListener {
                            Toast.makeText(this, "¡Cuenta creada!", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, MainActivity::class.java)
                            intent.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                            btnRegister.isEnabled = true; btnRegister.text = "Registrarse"
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    btnRegister.isEnabled = true;
                    btnRegister.text = "Registrarse"
                }
        }
    }
}