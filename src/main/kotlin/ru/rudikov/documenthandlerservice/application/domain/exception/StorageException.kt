package ru.rudikov.documenthandlerservice.application.domain.exception

open class StorageException : RuntimeException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}