package com.example.examenfinalprogramacion2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.examenfinalprogramacion2.firebase.FirestoreInitializer
import com.example.examenfinalprogramacion2.ui.AdminScreen
import com.example.examenfinalprogramacion2.ui.PrestamosScreen
import com.example.examenfinalprogramacion2.ui.RegisterStudent
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        FirestoreInitializer.ensureInitialData()

        setContent {
            val navController: NavHostController = rememberNavController()

            NavHost(navController = navController, startDestination = "register_student") {
                composable("register_student") { RegisterStudent(navController) }
                composable("prestamos") { PrestamosScreen(navController) }
                composable("admin") { AdminScreen(navController) }
            }
        }
    }
}

