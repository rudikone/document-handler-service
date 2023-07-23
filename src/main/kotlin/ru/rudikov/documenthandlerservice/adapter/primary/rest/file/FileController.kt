package ru.rudikov.documenthandlerservice.adapter.primary.rest.file

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.data.mongodb.gridfs.GridFsResource
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders.CONTENT_DISPOSITION
import org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE
import org.springframework.http.MediaType.parseMediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import ru.rudikov.documenthandlerservice.adapter.primary.rest.advice.ErrorMessage
import ru.rudikov.documenthandlerservice.port.primary.StoragePort
import java.net.URLEncoder
import java.nio.charset.StandardCharsets.UTF_8

@RestController
@RequestMapping("/file")
class FileController(
    private val gridFSUseCase: StoragePort,
) {

    @Operation(summary = "Сохранить файл")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "Created",
                content = [Content(schema = Schema(hidden = true))]
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal Server Error",
                content = [Content(schema = Schema(implementation = ErrorMessage::class))]
            ),
        ]
    )
    @PostMapping(consumes = [MULTIPART_FORM_DATA_VALUE])
    fun saveFile(@RequestParam("file") file: MultipartFile): ResponseEntity<Unit> {
        val fileId = gridFSUseCase.save(file = file)
        val location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(fileId).toUri()

        return ResponseEntity.created(location).build()
    }

    @Operation(summary = "Скачать файл")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "OK",
                content = [Content(mediaType = MULTIPART_FORM_DATA_VALUE)]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Not Found",
                content = [Content(schema = Schema(implementation = ErrorMessage::class))]
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal Server Error",
                content = [Content(schema = Schema(implementation = ErrorMessage::class))]
            ),
        ]
    )
    @GetMapping("/{id:.+}")
    @ResponseBody
    fun download(@PathVariable id: String): ResponseEntity<Resource> {
        val file = gridFSUseCase.download(id) as GridFsResource

        val encodedFilename = URLEncoder.encode(file.filename, UTF_8.toString())
        val contentDisposition = ContentDisposition.builder("attachment").filename(encodedFilename).build()
        val response = ByteArrayResource(file.inputStream.readBytes())

        return ResponseEntity.ok()
            .contentType(parseMediaType(file.contentType))
            .header(CONTENT_DISPOSITION, contentDisposition.toString())
            .body(response)
    }
}
