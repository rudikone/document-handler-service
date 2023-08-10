package ru.rudikov.documenthandlerservice.application.service

import com.mongodb.BasicDBObject
import mu.KotlinLogging
import org.springframework.core.io.Resource
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.gridfs.GridFsOperations
import org.springframework.data.mongodb.gridfs.GridFsTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import ru.rudikov.documenthandlerservice.application.domain.exception.StorageException
import ru.rudikov.documenthandlerservice.application.domain.exception.StorageFileNotFoundException
import ru.rudikov.documenthandlerservice.port.primary.FilePort

@Service
class GridFSUseCase(
    private val template: GridFsTemplate,
    private val operations: GridFsOperations,
) : FilePort {

    override fun save(file: MultipartFile): String =
        runCatching {
            if (file.isEmpty) {
                throw StorageException("Failed to store empty file.")
            }

            val fileName = file.originalFilename ?: throw StorageException("File not defined or not available")
            val metadata = BasicDBObject().apply { this["fileSize"] = file.size }

            val fileId = template.store(
                file.inputStream,
                fileName,
                file.contentType,
                metadata,
            )

            fileId.toString()
        }.onSuccess {
            logger.info { "File ${file.originalFilename} was saved" }
        }.getOrThrow()

    @Suppress("EqualsNullCall")
    override fun download(filename: String): Resource = runCatching {
        val gridFSFile = template.findOne(Query(Criteria.where("_id").`is`(filename)))

        if (gridFSFile.metadata == null) {
            throw StorageFileNotFoundException("Could not read file: $filename")
        }

        operations.getResource(gridFSFile)
    }.onFailure {
        logger.error { it }
        throw StorageFileNotFoundException("Could not read file: $filename", it)
    }.getOrThrow()

    @Transactional
    override fun deleteAll() {
        runCatching {
            template.delete(Query())
            operations.delete(Query())
        }.onSuccess {
            logger.info { "All files deleted" }
        }.getOrThrow()
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
