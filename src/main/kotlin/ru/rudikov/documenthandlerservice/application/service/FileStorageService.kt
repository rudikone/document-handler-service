package ru.rudikov.documenthandlerservice.application.service

import java.nio.file.Path
import java.util.stream.Stream

interface FileStorageService {

    fun init()

    fun getPath(filename: String): Path?

    fun getAllPaths(): Stream<Path?>?
}
