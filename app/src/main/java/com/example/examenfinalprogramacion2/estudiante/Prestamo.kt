package com.example.examenfinalprogramacion2.model

data class Prestamo(
    val id: String = "",
    val studentId: String = "",
    val equipoId: String = "",
    val equipoNombre: String = "",
    val cantidad: Long = 1,
    val status: String = "pending",
    val approvedBy: String? = null
)

