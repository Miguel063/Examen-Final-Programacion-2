package com.example.examenfinalprogramacion2.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.examenfinalprogramacion2.firebase.FirestoreRepository
import com.example.examenfinalprogramacion2.model.Student
import kotlinx.coroutines.launch

@Composable
fun RegisterStudent(navController: NavController) {
    val scope = rememberCoroutineScope()
    var carnet by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var carrera by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var message by remember { mutableStateOf("") }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> photoUri = uri }

    Column(
        Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Registro de Estudiante", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(value = carnet, onValueChange = { carnet = it }, label = { Text("Carnet") })
        OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") })
        OutlinedTextField(value = carrera, onValueChange = { carrera = it }, label = { Text("Carrera") })
        Spacer(Modifier.height(8.dp))

        Button(onClick = { imagePicker.launch("image/*") }) {
            Text("Seleccionar Foto")
        }
        Spacer(Modifier.height(8.dp))

        photoUri?.let {
            Image(
                painter = rememberAsyncImagePainter(it),
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(Modifier.height(16.dp))
        Button(onClick = {
            scope.launch {
                val student = Student(carnet, nombre, carrera, null)
                val result = FirestoreRepository.registrarStudent(student, photoUri)
                message = if (result.isSuccess) {
                    "Registrado correctamente"
                } else {
                    "Error: ${result.exceptionOrNull()?.message}"
                }
            }
        }) {
            Text("Registrar")
        }

        Spacer(Modifier.height(8.dp))
        Button(onClick = { navController.navigate("prestamos") }) {
            Text("Ir a Préstamos")
        }

        Spacer(Modifier.height(8.dp))
        Text(message)
    }
}

