package ru.rudikov.documenthandlerservice.adapter.primary.rest.advice

import java.time.Instant

data class ErrorMessage(
    val status: Int,
    val date: Instant = Instant.now(),
    val message: String,
    val description: String,
)