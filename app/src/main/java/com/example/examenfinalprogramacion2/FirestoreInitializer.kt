package com.example.examenfinalprogramacion2.firebase

import com.example.examenfinalprogramacion2.model.Equipo
import com.google.firebase.firestore.FirebaseFirestore

object FirestoreInitializer {
    fun ensureInitialData() {
        val db = FirebaseFirestore.getInstance()
        val equipoRef = db.collection("equipo")

        val equiposIniciales = listOf(
            Equipo("EQ001", "Laptop Dell", "Laptop Core i7 con 16GB RAM", 5, 5),
            Equipo("EQ002", "Proyector Epson", "Proyector HD 1080p", 3, 3),
            Equipo("EQ003", "Cámara Canon", "Cámara réflex Canon EOS", 4, 4)
        )

        equiposIniciales.forEach { equipo ->
            equipoRef.document(equipo.id).set(equipo)
        }
    }
}


