package ru.rudikov.documenthandlerservice.port.primary

import org.springframework.core.io.Resource
import org.springframework.web.multipart.MultipartFile

interface FilePort {

    fun save(file: MultipartFile): String

    fun download(filename: String): Resource

    fun deleteAll()
}
