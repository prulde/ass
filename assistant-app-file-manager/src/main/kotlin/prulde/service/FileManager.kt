package prulde.service

import org.springframework.web.multipart.MultipartFile

interface FileManager {
    fun uploadFile(file: MultipartFile, directory: String): String
    fun downloadFile(filePath: String): ByteArray
}