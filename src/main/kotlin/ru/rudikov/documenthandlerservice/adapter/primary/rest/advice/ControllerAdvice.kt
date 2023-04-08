package ru.rudikov.documenthandlerservice.adapter.primary.rest.advice

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import ru.rudikov.documenthandlerservice.application.domain.exception.StorageFileNotFoundException

@RestControllerAdvice

class ControllerAdvice {

    @ExceptionHandler(value = [StorageFileNotFoundException::class])
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    fun notFoundException(ex: StorageFileNotFoundException, request: WebRequest?): ErrorMessage = ErrorMessage(
        status = HttpStatus.NOT_FOUND.value(),
        message = ex.message ?: "Message is not present",
        description = HttpStatus.NOT_FOUND.reasonPhrase,
    )
}