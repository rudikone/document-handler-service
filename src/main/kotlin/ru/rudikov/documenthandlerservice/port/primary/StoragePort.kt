package ru.rudikov.documenthandlerservice.port.primary

import org.springframework.core.io.Resource
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Path
import java.util.stream.Stream

interface StoragePort {

    fun init()

    fun store(file: MultipartFile)

    fun loadAll(): Stream<Path?>?

    fun load(filename: String): Path?

    fun loadAsResource(filename: String): Resource?

    fun deleteAll()
}