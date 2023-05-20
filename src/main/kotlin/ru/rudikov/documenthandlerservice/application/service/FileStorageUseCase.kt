package ru.rudikov.documenthandlerservice.application.service

import mu.KotlinLogging
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.stereotype.Service
import org.springframework.util.FileSystemUtils
import org.springframework.web.multipart.MultipartFile
import ru.rudikov.documenthandlerservice.application.domain.exception.StorageException
import ru.rudikov.documenthandlerservice.application.domain.exception.StorageFileNotFoundException
import ru.rudikov.documenthandlerservice.configuration.StorageProperties
import ru.rudikov.documenthandlerservice.port.primary.StoragePort
import java.io.File
import java.io.IOException
import java.net.MalformedURLException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.util.stream.Stream
import kotlin.io.path.absolutePathString

@Service
class FileStorageUseCase(
    private val properties: StorageProperties,
) : StoragePort, FileStorageService {

    override fun save(file: MultipartFile, type: String?): String =
        runCatching {
            if (file.isEmpty) {
                throw StorageException("Failed to store empty file.")
            }

            val fileName = file.originalFilename ?: throw StorageException("File not defined or not available")
            val filePath = type?.let { Paths.get(it + File.separator + fileName) } ?: Paths.get(fileName)
            val destinationFile: Path = properties.storageLocation.resolve(filePath).normalize().toAbsolutePath()

            if (!destinationFile.startsWith(properties.storageLocation.toAbsolutePath())) {
                // This is a security check
                throw StorageException("Cannot store file outside ${properties.storageLocation}.")
            }

            type?.let { Files.createDirectories(destinationFile.parent) }
            file.inputStream.use { inputStream -> Files.copy(inputStream, destinationFile, REPLACE_EXISTING) }

            file.originalFilename!!
        }.onFailure {
            if (it is IOException) {
                logger.error { it }
                throw StorageException("Failed to store file.", it)
            } else {
                throw it
            }
        }.onSuccess {
            logger.info { "File ${file.originalFilename} was saved" }
        }.getOrThrow()

    override fun getAllPaths(): Stream<Path?>? = runCatching {
        Files.walk(properties.storageLocation, 1)
            .filter { path -> path != properties.storageLocation }
            .map { properties.storageLocation.relativize(it) }
    }.onFailure {
        if (it is IOException) {
            logger.error { it }
            throw StorageException("Failed to read stored files", it)
        } else {
            throw it
        }
    }.getOrThrow()

    override fun getPath(filename: String): Path = properties.storageLocation.resolve(filename)

    override fun download(filename: String): Resource = runCatching {
        val file: Path = getPath(filename)
        val resource: Resource = UrlResource(file.toUri())

        if (resource.exists() || resource.isReadable) {
            resource
        } else {
            throw StorageFileNotFoundException("Could not read file: $filename")
        }
    }.onFailure {
        if (it is MalformedURLException) {
            logger.error { it }
            throw StorageFileNotFoundException("Could not read file: $filename", it)
        } else {
            throw it
        }
    }.getOrThrow()

    override fun deleteAll() {
        FileSystemUtils.deleteRecursively(properties.storageLocation.toFile())
    }

    override fun init() {
        runCatching {
            Files.createDirectories(properties.storageLocation)
        }.onFailure {
            if (it is IOException) {
                throw StorageException("Could not initialize storage", it)
            } else {
                throw it
            }
        }.onSuccess {
            logger.info { "Directory ${it.absolutePathString()} was created" }
        }.getOrThrow()
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
