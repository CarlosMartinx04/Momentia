package com.example.momentia

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.momentia.presentation.initial.InitialScreen
import com.example.momentia.presentation.login.LoginScreen
import com.example.momentia.presentation.signup.SignUpScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun NavigationWrapper(navHostController: NavHostController, auth: FirebaseAuth) {

    NavHost(navController = navHostController, startDestination = "initial"){
        composable("initial"){
            InitialScreen(
                navigateToSignUp = {navHostController.navigate("signUp")},
                navigateToLogin = {navHostController.navigate("logIn")}

            )
        }

        composable("logIn"){
            //El auth hace que puedan registrarse o iniciar sesion
            LoginScreen(auth)
        }

        composable("signUp"){
            SignUpScreen(auth)
        }
    }

}