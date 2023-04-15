package ru.rudikov.documenthandlerservice.adapter.primary.rest.file

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE
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

@RestController
@RequestMapping("/file")
class FileController(
    private val storagePort: StoragePort,
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
    fun saveFile(
        @RequestParam("file") file: MultipartFile,
        @RequestParam(name = "type", required = false) type: String?
    ): ResponseEntity<Unit> {
        storagePort.save(file = file, type = type)

        return ResponseEntity.created(
            ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{fileName}")
                .buildAndExpand(file.originalFilename)
                .toUri()
        ).build()
    }

    @Operation(summary = "Скачать файл")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Created",
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
    @GetMapping("/{filename:.+}")
    @ResponseBody
    fun getFile(@PathVariable filename: String): ResponseEntity<Resource> {
        val file: Resource = storagePort.loadAsResource(filename)
        return ResponseEntity.ok().header(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + file.filename + "\""
        ).body(file)
    }
}
