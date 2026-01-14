package com.example.momentia

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.momentia.presentation.home.HomeScreen
import com.example.momentia.presentation.initial.InitialScreen
import com.example.momentia.presentation.login.LoginScreen
import com.example.momentia.presentation.model.CreaterScreen
import com.example.momentia.presentation.signup.SignUpScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun NavigationWrapper(
    navHostController: NavHostController,
    auth: FirebaseAuth,
    db: FirebaseFirestore
) {

    NavHost(navController = navHostController, startDestination = "initial"){
        composable("initial"){
            InitialScreen(
                navigateToSignUp = {navHostController.navigate("signUp")},
                navigateToLogin = {navHostController.navigate("logIn")}
            )
        }

        composable("logIn"){
            //El auth hace que puedan registrarse o iniciar sesion
            //el db hace que accedamos a firebase
            LoginScreen(auth,
                navigateBack = {navHostController.popBackStack()},
                navigateToHome = {navHostController.navigate("home")})
        }

        composable("signUp"){
            SignUpScreen(auth,
                navigateBack = {navHostController.popBackStack()},
                navigateToCreater = {navHostController.navigate("creater") }
            )
        }

        composable("home"){
            HomeScreen(db, auth)
        }

        composable("creater"){
            CreaterScreen(auth, db,
                navigateToHome = {navHostController.navigate("home")}
            )
        }
    }

}