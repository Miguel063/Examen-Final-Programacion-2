package com.example.examenfinalprogramacion2.firebase

import android.net.Uri
import android.content.Context
import com.example.examenfinalprogramacion2.model.Student
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

object FirestoreRepository {

    suspend fun registrarStudent(context: Context, student: Student, photoUri: Uri?): Result<Unit> {
        return try {
            val db = FirebaseFirestore.getInstance()
            val storage = FirebaseStorage.getInstance()
            var photoUrl: String? = student.photoUrl

            if (photoUri != null) {
                val inputStream = context.contentResolver.openInputStream(photoUri)
                    ?: throw Exception("No se puede abrir la imagen desde el URI")

                val bytes = inputStream.readBytes()
                inputStream.close()

                val ref = storage.reference.child("students/${student.carnet}.jpg")
                ref.putBytes(bytes).await()
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
                "status" to "pending",
                "requestedAt" to Timestamp.now()
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
            val prestamoRef = db.collection("prestamo").document(prestamoId)

            db.runTransaction { tx ->
                val prestamoSnap = tx.get(prestamoRef)
                if (!prestamoSnap.exists()) throw Exception("Préstamo no encontrado")

                val status = prestamoSnap.getString("status") ?: "pending"
                if (status != "pending") throw Exception("Solo préstamos pendientes pueden aprobarse")

                val equipoId = prestamoSnap.getString("equipoId") ?: throw Exception("equipoId faltante")
                val cantidad = (prestamoSnap.getLong("cantidad") ?: 0L)

                val equipoRef = db.collection("equipo").document(equipoId)
                val equipoSnap = tx.get(equipoRef)
                if (!equipoSnap.exists()) throw Exception("Equipo no existe")

                val disponible = equipoSnap.getLong("cantidadDisponible") ?: 0L
                if (disponible < cantidad) throw Exception("No hay unidades suficientes disponibles")

                tx.update(equipoRef, "cantidadDisponible", disponible - cantidad)
                tx.update(prestamoRef, mapOf(
                    "status" to "approved",
                    "approvedBy" to adminId,
                    "approvedAt" to FieldValue.serverTimestamp()
                ))
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun rechazarPrestamo(prestamoId: String, adminId: String): Result<Unit> {
        return try {
            val db = FirebaseFirestore.getInstance()
            val prestamoRef = db.collection("prestamo").document(prestamoId)
            prestamoRef.update(
                mapOf(
                    "status" to "rejected",
                    "rejectedBy" to adminId,
                    "rejectedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun marcarDevuelto(prestamoId: String, adminId: String? = null): Result<Unit> {
        return try {
            val db = FirebaseFirestore.getInstance()
            val prestamoRef = db.collection("prestamo").document(prestamoId)

            db.runTransaction { tx ->
                val prestamoSnap = tx.get(prestamoRef)
                if (!prestamoSnap.exists()) throw Exception("Préstamo no encontrado")

                val status = prestamoSnap.getString("status") ?: "pending"
                if (status != "approved") throw Exception("Solo préstamos aprobados pueden marcarse como devueltos")

                val equipoId = prestamoSnap.getString("equipoId") ?: throw Exception("equipoId faltante")
                val cantidad = (prestamoSnap.getLong("cantidad") ?: 0L)

                val equipoRef = db.collection("equipo").document(equipoId)
                val equipoSnap = tx.get(equipoRef)
                if (!equipoSnap.exists()) throw Exception("Equipo no existe")

                val disponible = equipoSnap.getLong("cantidadDisponible") ?: 0L
                tx.update(equipoRef, "cantidadDisponible", disponible + cantidad)

                val updates = mutableMapOf<String, Any>(
                    "status" to "returned",
                    "returnedAt" to FieldValue.serverTimestamp()
                )
                adminId?.let { updates["returnedBy"] = it }
                tx.update(prestamoRef, updates)
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
