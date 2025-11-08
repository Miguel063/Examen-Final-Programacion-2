package com.example.examenfinalprogramacion2.model

data class Student(
    val name: String = "",
    val carnet: String = "",
    val carrera: String = "",
    val photoUrl: String? = null,
    val role: String = "student"
)

