package com.example.momentia.presentation.home

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.example.momentia.NavigationWrapper
import com.example.momentia.components.MyNavigationBar
import com.example.momentia.ui.theme.MomentiaTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.auth.User
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ListItem
import androidx.compose.runtime.mutableIntStateOf
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.momentia.R
import com.example.momentia.presentation.model.NavItem
import com.example.momentia.presentation.model.Post

@Composable
fun HomeScreen(db: FirebaseFirestore, auth: FirebaseAuth, navController: NavController=rememberNavController()){
    val navController = rememberNavController()

    var selectedIndex by remember { mutableIntStateOf(0) }

    val db = Firebase.firestore
    val currentUser = auth.currentUser?.email.toString()

    val itemList = listOf(
        NavItem("Inicio", R.drawable.home, "home"),
        NavItem("Notificaciones", R.drawable.notifications, "notifications"),
        NavItem("Perfil", R.drawable.profile, "profile")
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            MyNavigationBar(auth, selectedIndex = selectedIndex, itemList) { index ->
                navController.navigate(itemList[index].route) {
                    launchSingleTop = true
                    restoreState = true
                    popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController = navController, startDestination = "profile"){
            composable("profile"){
                Profile(
                    auth,db
                )
            }

            composable("notifications"){
                //El auth hace que puedan registrarse o iniciar sesion
                //el db hace que accedamos a firebase
                Notifications(

                )
            }

            composable("home"){
                Home(
                    auth, db
                )
            }
        }

    }

}

fun obtenerInformacion(
    userId: String,
    onResult: (String?) -> Unit
) {
    val db = FirebaseFirestore.getInstance()

    db.collection("users")
        .document(userId)
        .get()
        .addOnSuccessListener { document ->
            if (document.exists()) {
                val nombre = document.getString("username")
                onResult(nombre)
            } else {
                onResult(null)
            }
        }
        .addOnFailureListener {
            onResult(null)
        }
}



@Composable
fun Home(auth: FirebaseAuth, db: FirebaseFirestore) {

}

@Composable
fun Notifications() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Pantalla Notificaciones")
    }
}

@Composable
fun Profile(auth: FirebaseAuth, db: FirebaseFirestore) {
    var username by remember { mutableStateOf("") }
    val currentUser = auth.currentUser?.email.toString()
    LaunchedEffect(currentUser) {
        obtenerInformacion(currentUser) { document ->
            username = document ?: "Cargando"
        }
    }
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = username,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color.Gray),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Foto", color = Color.White)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "0",
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
            }
        }
    }
}
