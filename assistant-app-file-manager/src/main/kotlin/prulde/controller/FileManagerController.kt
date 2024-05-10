package prulde.controller

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import prulde.service.FileManager

@RestController
@RequestMapping("/api/files")
@Tag(name = "file-manager")
class FileManagerController(
    private val fileManager: FileManager,
    private val defaultDir: String = "tmp"
) {

    @PostMapping("/{directory}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadFile(
        @RequestPart(value = "file") file: MultipartFile,
        @PathVariable(value = "directory") directory: String,
    ): String = fileManager.uploadFile(file, directory)

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadFile(
        @RequestPart(value = "file") file: MultipartFile,
    ): String = fileManager.uploadFile(file, defaultDir)

    @GetMapping("/{directory}/{name}")
    fun downloadFile(
        @PathVariable(name = "directory") directory: String,
        @PathVariable(name = "name") name: String,
    ): ByteArray =
        fileManager.downloadFile("${directory}/${name}")
}