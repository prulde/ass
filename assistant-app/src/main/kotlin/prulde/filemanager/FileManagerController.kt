package prulde.filemanager

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/files")
class FileManagerController(
    private val fileManager: FileManager,
    private val defaultDir: String = "tmp"
) {

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadFile(
        @RequestPart(value = "file") file: MultipartFile,
        @RequestParam(value = "directory", required = false) directory: String? = null
    ): String = fileManager.uploadFile(file, directory ?: defaultDir)

    @GetMapping("/{fileName}")
    fun downloadFile(@PathVariable(name = "fileName") fileName: String): ByteArray =
        fileManager.downloadFile(fileName)
}