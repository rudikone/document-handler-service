package ru.rudikov.documenthandlerservice.adapter.primary.rest.advice

import io.swagger.v3.oas.annotations.Hidden
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import ru.rudikov.documenthandlerservice.application.domain.exception.StorageException
import ru.rudikov.documenthandlerservice.application.domain.exception.StorageFileNotFoundException

@RestControllerAdvice

class ControllerAdvice {

    @Hidden
    @ExceptionHandler(value = [StorageFileNotFoundException::class])
    @ResponseStatus(value = NOT_FOUND)
    fun notFoundException(ex: StorageFileNotFoundException, request: WebRequest?): ErrorMessage =
        buildErrorMessage(ex = ex, status = NOT_FOUND)

    @Hidden
    @ExceptionHandler(value = [StorageException::class])
    @ResponseStatus(value = INTERNAL_SERVER_ERROR)
    fun storageException(ex: StorageException, request: WebRequest?): ErrorMessage =
        buildErrorMessage(ex = ex, status = INTERNAL_SERVER_ERROR)

    @Hidden
    @ExceptionHandler(value = [Throwable::class])
    @ResponseStatus(value = INTERNAL_SERVER_ERROR)
    fun throwable(ex: Throwable, request: WebRequest?): ErrorMessage =
        buildErrorMessage(ex = ex, status = INTERNAL_SERVER_ERROR)

    private fun buildErrorMessage(ex: Throwable, status: HttpStatus): ErrorMessage {
        logger.error { ex }

        return ErrorMessage(
            status = status.value(),
            message = ex.message ?: "Message is not present",
            description = status.reasonPhrase,
        )
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}