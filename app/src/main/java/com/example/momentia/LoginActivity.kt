package com.example.momentia

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var googleSignInClient: GoogleSignInClient

    private val googleLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Error Google: ${e.message}", Toast.LENGTH_LONG).show()
                enableButtons()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        if (auth.currentUser != null) {
            goToMain(); return
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnGoogle = findViewById<Button>(R.id.btnGoogle)
        val tvGoToRegister = findViewById<TextView>(R.id.tvGoToRegister)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            if (email.isEmpty()) {
                etEmail.error = "Ingresa tu correo"; return@setOnClickListener
            }
            if (password.isEmpty()) {
                etPassword.error = "Ingresa tu contraseña"; return@setOnClickListener
            }
            disableButtons(btnLogin, btnGoogle)
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    Toast.makeText(this, "¡Bienvenido!", Toast.LENGTH_SHORT).show(); goToMain()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        "Error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show(); enableButtons(btnLogin, btnGoogle)
                }
        }

        btnGoogle.setOnClickListener {
            disableButtons(btnLogin, btnGoogle)
            googleSignInClient.signOut()
                .addOnCompleteListener { googleLauncher.launch(googleSignInClient.signInIntent) }
        }

        tvGoToRegister.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    RegisterActivity::class.java
                )
            )
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener { authResult ->
                val user = authResult.user!!
                val isNewUser = authResult.additionalUserInfo?.isNewUser ?: false
                if (isNewUser) {
                    val userData = hashMapOf(
                        "uid" to user.uid, "name" to (user.displayName ?: ""),
                        "email" to (user.email ?: ""), "bio" to "",
                        "photoUrl" to (user.photoUrl?.toString() ?: ""),
                        "countries" to listOf<String>(),
                        "createdAt" to System.currentTimeMillis()
                    )
                    db.collection("users").document(user.uid).set(userData)
                        .addOnSuccessListener {
                            Toast.makeText(
                                this,
                                "¡Cuenta creada con Google!",
                                Toast.LENGTH_SHORT
                            ).show(); goToMain()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                this,
                                "Error: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show(); enableButtons()
                        }
                } else {
                    Toast.makeText(this, "¡Bienvenido de nuevo!", Toast.LENGTH_SHORT)
                        .show(); goToMain()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show(); enableButtons()
            }
    }

    private fun disableButtons(
        btnLogin: Button = findViewById(R.id.btnLogin),
        btnGoogle: Button = findViewById(R.id.btnGoogle)
    ) {
        btnLogin.isEnabled = false; btnGoogle.isEnabled = false
    }

    private fun enableButtons(
        btnLogin: Button = findViewById(R.id.btnLogin),
        btnGoogle: Button = findViewById(R.id.btnGoogle)
    ) {
        btnLogin.isEnabled = true; btnGoogle.isEnabled = true
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java)); finish()
    }
}