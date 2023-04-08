package ru.rudikov.documenthandlerservice.adapter.primary.rest.file

import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import ru.rudikov.documenthandlerservice.application.domain.exception.StorageFileNotFoundException
import ru.rudikov.documenthandlerservice.port.primary.StoragePort

@Controller
class FileController(
    private val storagePort: StoragePort,
) {

    @GetMapping("/")
    fun listUploadedFiles(model: Model): String? {
        model.addAttribute("files", storagePort.loadAll()?.map { path ->
            MvcUriComponentsBuilder.fromMethodName(
                FileController::class.java,
                "serveFile", path?.fileName.toString()
            ).build().toUri().toString()
        }?.toList())
        return "uploadForm"
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    fun serveFile(@PathVariable filename: String): ResponseEntity<Resource?>? {
        val file: Resource? = storagePort.loadAsResource(filename)
        return ResponseEntity.ok().header(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + file?.filename + "\""
        ).body(file)
    }

    @PostMapping("/")
    fun handleFileUpload(
        @RequestParam("file") file: MultipartFile,
        redirectAttributes: RedirectAttributes,
    ): String? {
        storagePort.store(file)
        redirectAttributes.addFlashAttribute(
            "message",
            "You successfully uploaded " + file.originalFilename + "!"
        )
        return "redirect:/"
    }
}