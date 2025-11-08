package com.example.examenfinalprogramacion2.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.examenfinalprogramacion2.firebase.FirestoreRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@Composable
fun AdminScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()
    var prestamos by remember { mutableStateOf(listOf<Map<String, Any>>()) }
    var message by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        db.collection("prestamo").addSnapshotListener { snapshot, e ->
            if (e == null && snapshot != null) {
                prestamos = snapshot.documents.mapNotNull { it.data?.plus("id" to it.id) }
            }
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Panel del Administrador", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))

        Button(onClick = { navController.navigate("prestamos") }) {
            Text("Ir a Préstamos")
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = { navController.navigate("register_student") }) {
            Text("Registrar Estudiante")
        }

        Spacer(Modifier.height(16.dp))
        if (prestamos.isEmpty()) {
            Text("No hay solicitudes de préstamo.")
        } else {
            LazyColumn {
                items(prestamos) { p ->
                    Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                        Column(Modifier.padding(12.dp)) {
                            Text("Estudiante: ${p["studentId"]}")
                            Text("Equipo: ${p["equipoNombre"]}")
                            Text("Cantidad: ${p["cantidad"]}")
                            Text("Estado: ${p["status"]}")
                            Spacer(Modifier.height(8.dp))
                            Row {
                                Button(onClick = {
                                    scope.launch {
                                        message = "Aprobando..."
                                        val res = FirestoreRepository.aprobarPrestamo(p["id"].toString(), "admin_demo")
                                        message = if (res.isSuccess) "Aprobado" else "Error: ${res.exceptionOrNull()?.message}"
                                    }
                                }) { Text("Aprobar") }

                                Spacer(Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        scope.launch {
                                            message = "Rechazando..."
                                            val res = FirestoreRepository.rechazarPrestamo(p["id"].toString(), "admin_demo")
                                            message = if (res.isSuccess) "Rechazado" else "Error: ${res.exceptionOrNull()?.message}"
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                ) { Text("Rechazar") }

                                Spacer(Modifier.width(8.dp))
                                Button(onClick = {
                                    scope.launch {
                                        message = "Marcando devuelto..."
                                        val res = FirestoreRepository.marcarDevuelto(p["id"].toString(), "admin_demo")
                                        message = if (res.isSuccess) "Devuelto" else "Error: ${res.exceptionOrNull()?.message}"
                                    }
                                }) { Text("Devolver") }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Text(message)
    }
}