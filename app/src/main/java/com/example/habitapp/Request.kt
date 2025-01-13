package com.example.habitapp

import java.time.LocalDate

data class Request(
    var id: String? = null,
    var sender: String? = null,
    var receiver: String? = null,
    var date: LocalDate? = null,
    var sname: String? = null,
    var rname: String? = null
)
