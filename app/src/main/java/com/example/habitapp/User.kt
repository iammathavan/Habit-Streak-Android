package com.example.habitapp

import java.time.LocalDate

data class User(
    var id: String? = null,
    var name: String? = null,
    var email: String? = null,
    var lastLogin: LocalDate? = null
)
