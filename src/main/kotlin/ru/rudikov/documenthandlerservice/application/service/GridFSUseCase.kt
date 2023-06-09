package ru.rudikov.documenthandlerservice.application.service

import com.mongodb.BasicDBObject
import mu.KotlinLogging
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.gridfs.GridFsOperations
import org.springframework.data.mongodb.gridfs.GridFsTemplate
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import ru.rudikov.documenthandlerservice.application.domain.exception.StorageException
import ru.rudikov.documenthandlerservice.application.domain.exception.StorageFileNotFoundException
import ru.rudikov.documenthandlerservice.port.primary.StoragePort

@Service
class GridFSUseCase(
    private val template: GridFsTemplate,
    private val operations: GridFsOperations,
) : StoragePort {

    override fun save(file: MultipartFile, type: String?): String =
        runCatching {
            if (file.isEmpty) {
                throw StorageException("Failed to store empty file.")
            }

            val fileName = file.originalFilename ?: throw StorageException("File not defined or not available")

            val metadata = BasicDBObject().apply {
                this["fileSize"] = file.size
            }

            val fileId = template.store(
                file.inputStream,
                fileName,
                file.contentType,
                metadata
            )

            fileId.toString()
        }.onSuccess {
            logger.info { "File ${file.originalFilename} was saved" }
        }.getOrThrow()

    @Suppress("EqualsNullCall")
    override fun download(filename: String): Resource = runCatching {
        val gridFSFile = template.findOne(Query(Criteria.where("_id").`is`(filename)))

        if (gridFSFile.equals(null) || gridFSFile.metadata == null) {
            throw StorageFileNotFoundException("Could not read file: $filename")
        }

        ByteArrayResource(operations.getResource(gridFSFile).inputStream.readBytes())
    }.onFailure {
        logger.error { it }
        throw StorageFileNotFoundException("Could not read file: $filename", it)
    }.getOrThrow()

    override fun deleteAll() {
        TODO("Not yet implemented")
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
