package com.example.habitapp

import java.time.LocalDate

data class Habit(
    var id: String? = null,
    var name: String? = null,
    var startDate: LocalDate? = null,
    var streak: Int? = 0,
    var score: Int? = 0,
    val description: String? = null,
    var completion: Boolean? = false
)
