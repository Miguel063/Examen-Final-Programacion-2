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
fun PrestamosScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()
    var equipos by remember { mutableStateOf(listOf<Map<String, Any>>()) }
    var carnet by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        db.collection("equipo").addSnapshotListener { snapshot, e ->
            if (e == null && snapshot != null) {
                equipos = snapshot.documents.mapNotNull { it.data?.plus("id" to it.id) }
            }
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Solicitar Préstamo", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(value = carnet, onValueChange = { carnet = it }, label = { Text("Carnet del estudiante") })
        Spacer(Modifier.height(12.dp))

        LazyColumn {
            items(equipos) { equipo ->
                Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Equipo: ${equipo["nombre"]}")
                        Text("Disponible: ${equipo["cantidadDisponible"]}")
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = {
                            scope.launch {
                                message = "Solicitando préstamo..."
                                val res = FirestoreRepository.solicitarPrestamo(
                                    carnet,
                                    equipo["id"].toString(),
                                    1
                                )
                                message = if (res.isSuccess) "Solicitud enviada" else "Error: ${res.exceptionOrNull()?.message}"
                            }
                        }) {
                            Text("Solicitar")
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Text(message)
        Spacer(Modifier.height(8.dp))
        Button(onClick = { navController.navigate("register_student") }) {
            Text("Volver al Registro")
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = { navController.navigate("admin") }) {
            Text("Ir al Panel Admin")
        }
    }
}