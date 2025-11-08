package com.example.examenfinalprogramacion2.firebase

import android.net.Uri
import com.example.examenfinalprogramacion2.model.Student
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

object FirestoreRepository {

    suspend fun registrarStudent(student: Student, photoUri: Uri?): Result<Unit> {
        return try {
            val db = FirebaseFirestore.getInstance()
            val storage = FirebaseStorage.getInstance()
            var photoUrl: String? = student.photoUrl

            if (photoUri != null) {
                val ref = storage.reference.child("students/${student.carnet}.jpg")
                ref.putFile(photoUri).await()
                photoUrl = ref.downloadUrl.await().toString()
            }

            val updatedStudent = student.copy(photoUrl = photoUrl)
            db.collection("students").document(student.carnet).set(updatedStudent).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun solicitarPrestamo(carnet: String, equipoId: String, cantidad: Long): Result<Unit> {
        return try {
            val db = FirebaseFirestore.getInstance()
            val equipoDoc = db.collection("equipo").document(equipoId).get().await()
            if (!equipoDoc.exists()) throw Exception("Equipo no encontrado")

            val equipoNombre = equipoDoc.getString("nombre") ?: ""
            val prestamo = mapOf(
                "studentId" to carnet,
                "equipoId" to equipoId,
                "equipoNombre" to equipoNombre,
                "cantidad" to cantidad,
                "status" to "pending"
            )

            db.collection("prestamo").add(prestamo).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun aprobarPrestamo(prestamoId: String, adminId: String): Result<Unit> {
        return try {
            val db = FirebaseFirestore.getInstance()
            db.collection("prestamo").document(prestamoId)
                .update("status", "approved", "approvedBy", adminId).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun rechazarPrestamo(prestamoId: String, adminId: String): Result<Unit> {
        return try {
            val db = FirebaseFirestore.getInstance()
            db.collection("prestamo").document(prestamoId)
                .update("status", "rejected", "approvedBy", adminId).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun marcarDevuelto(prestamoId: String): Result<Unit> {
        return try {
            val db = FirebaseFirestore.getInstance()
            db.collection("prestamo").document(prestamoId)
                .update("status", "returned").await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
