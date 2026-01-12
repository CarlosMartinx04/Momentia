package com.example.momentia.presentation.login

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.momentia.R
import com.example.momentia.ui.theme.Azul
import com.example.momentia.ui.theme.Blanco
import com.example.momentia.ui.theme.GrisClaro
import com.example.momentia.ui.theme.Negro
import com.google.firebase.auth.FirebaseAuth

@Composable
fun LoginScreen(auth: FirebaseAuth) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Blanco)
            .padding(horizontal = 32.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row() {
            Icon(
                painter = painterResource(id = R.drawable.outline_arrow_back_24),
                contentDescription = ""
            )
            Spacer(modifier = Modifier.weight(1f))
        }

        Spacer(Modifier.height(80.dp))

        Text("Email", color = Negro, fontWeight = FontWeight.Bold, fontSize = 40.sp)
        TextField(
            value = email,
            onValueChange = { email = it },
            leadingIcon = { Icon(Icons.Filled.Favorite, contentDescription = "Localized description") },
            trailingIcon = { Icon(imageVector = Icons.Default.Clear, contentDescription = "") },
            modifier = Modifier.border(BorderStroke(width = 4.dp, color = GrisClaro), shape = RoundedCornerShape(20))
                .fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = GrisClaro,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent),
            placeholder = {
                Text("Introduce tu Correo")
            }
        )
        Spacer(Modifier.height(48.dp))
        Text("Contraseña", color = Negro, fontWeight = FontWeight.Bold, fontSize = 40.sp)

        TextField(
            value = password,
            onValueChange = { password = it },
            leadingIcon = { Icon(Icons.Filled.Favorite, contentDescription = "Localized description") },
            trailingIcon = { Icon(imageVector = Icons.Default.Clear, contentDescription = "") },
            modifier = Modifier.border(BorderStroke(width = 4.dp, color = GrisClaro), shape = RoundedCornerShape(20))
                .fillMaxWidth(),colors = TextFieldDefaults.colors(
                focusedContainerColor = GrisClaro,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent),
            placeholder = {
                Text("Introduce tu contraseña")
            }
        )
        Spacer(Modifier.height(48.dp))
        Button(onClick = {
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if(task.isSuccessful){
                    //Navegar
                    Log.i("aris", "Login Ok")
                } else {
                    //Error
                    Log.i("aris", "Login Failed")
                }
            }
        }) {
            Text(text = "Iniciar Sesion")
        }

    }
}

