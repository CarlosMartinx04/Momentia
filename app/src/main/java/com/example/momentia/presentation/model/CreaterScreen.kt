package com.example.momentia.presentation.model

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.momentia.ui.theme.Azul
import com.example.momentia.ui.theme.Blanco
import com.example.momentia.ui.theme.GrisClaro
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

@Composable
fun CreaterScreen(auth: FirebaseAuth, db: FirebaseFirestore, navigateToHome:() -> Unit={}){
    var nameUser by remember { mutableStateOf("") }
    var biography by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Blanco),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Text(text = "Terminemos de crearte :D")

        Spacer(modifier = Modifier.height(20.dp))
        Text(text = "Nombre de Usuario")
        TextField(
            value = nameUser,
            onValueChange = { nameUser = it },
            leadingIcon = { Icon(Icons.Filled.Favorite, contentDescription = "Localized description") },
            trailingIcon = { Icon(imageVector = Icons.Default.Clear, contentDescription = "") },
            modifier = Modifier
                .border(
                    BorderStroke(width = 4.dp, color = GrisClaro),
                    shape = RoundedCornerShape(20)
                )
                .fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = GrisClaro,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent),
            placeholder = {
                Text("Elige un nombre de usuario")
            }
        )

        Spacer(modifier = Modifier.height(20.dp))
        Text(text = "Terminemos de crearte :D")
        TextField(
            value = biography,
            onValueChange = { biography = it },
            leadingIcon = { Icon(Icons.Filled.Favorite, contentDescription = "Localized description") },
            trailingIcon = { Icon(imageVector = Icons.Default.Clear, contentDescription = "") },
            modifier = Modifier
                .border(
                    BorderStroke(width = 4.dp, color = GrisClaro),
                    shape = RoundedCornerShape(20)
                )
                .fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = GrisClaro,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent),
            placeholder = {
                Text("Describete :)")
            }
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = {
                createUser(db, auth,nameUser, biography)
                navigateToHome()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 32.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Azul)
        ) {
            Text(text = "Registrate", color = Blanco, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.weight(1f))


    }
}

fun createUser(db: FirebaseFirestore,
               auth: FirebaseAuth,
               userName:String,
               biography:String
){
    val user = Users(
        username = userName,
        biography = biography,
        profilePicture = "",
        followers = 0,
        following = 0,
        years = 0,
        posts = emptyList()
    )
    db.collection("users")
        .document(auth.currentUser?.email.toString())
        .set(user)

}