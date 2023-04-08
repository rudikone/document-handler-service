package ru.rudikov.documenthandlerservice.application.service


import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.stereotype.Service
import org.springframework.util.FileSystemUtils
import org.springframework.web.multipart.MultipartFile
import ru.rudikov.documenthandlerservice.application.domain.exception.StorageException
import ru.rudikov.documenthandlerservice.application.domain.exception.StorageFileNotFoundException
import ru.rudikov.documenthandlerservice.configuration.StorageProperties
import ru.rudikov.documenthandlerservice.port.primary.StoragePort
import java.io.IOException
import java.net.MalformedURLException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.util.stream.Stream

@Service
class FileStorageUseCase(
    private val properties: StorageProperties,
) : StoragePort {

    override fun store(file: MultipartFile) {
        runCatching {
            if (file.isEmpty) {
                throw StorageException("Failed to store empty file.")
            }

            val fileName = file.originalFilename ?: throw StorageException("File not defined or not available")

            val destinationFile: Path = Paths.get(properties.location)
                .resolve(Paths.get(fileName))
                .normalize()
                .toAbsolutePath()

            if (destinationFile.parent != Paths.get(properties.location).toAbsolutePath()) {
                // This is a security check
                throw StorageException(
                    "Cannot store file outside current directory."
                )
            }

            file.inputStream.use { inputStream ->
                Files.copy(inputStream, destinationFile, REPLACE_EXISTING)
            }
        }.onFailure {
            if (it is IOException) {
                throw StorageException("Failed to store file.", it)
            } else {
                throw it
            }
        }.getOrThrow()
    }

    override fun loadAll(): Stream<Path?>? = runCatching {
        Files.walk(Paths.get(properties.location), 1)
            .filter { path -> path != Paths.get(properties.location) }
            .map {
                Paths.get(properties.location).relativize(it)
            }
    }.onFailure {
        if (it is IOException) {
            throw StorageException("Failed to read stored files", it)
        } else {
            throw it
        }
    }.getOrThrow()

    override fun load(filename: String): Path = Paths.get(properties.location).resolve(filename)

    override fun loadAsResource(filename: String): Resource? = runCatching {
        val file: Path = load(filename)
        val resource: Resource = UrlResource(file.toUri())
        if (resource.exists() || resource.isReadable) {
            resource
        } else {
            throw StorageFileNotFoundException("Could not read file: $filename")
        }
    }.onFailure {
        if (it is MalformedURLException) {
            throw StorageFileNotFoundException("Could not read file: $filename", it)
        } else {
            throw it
        }
    }.getOrThrow()

    override fun deleteAll() {
        FileSystemUtils.deleteRecursively(Paths.get(properties.location).toFile())
    }

    override fun init() {
        runCatching {
            Files.createDirectories(Paths.get(properties.location))
        }.onFailure {
            if (it is IOException) {
                throw StorageException("Could not initialize storage", it)
            } else {
                throw it
            }
        }.getOrThrow()
    }
}