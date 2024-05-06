package prulde.filemanager

import io.minio.GetObjectArgs
import io.minio.MinioClient
import io.minio.PutObjectArgs
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class MinioService(
    private val minioClient: MinioClient,
) : FileManager {
    private val appBucket: String = "assistant-app"
    private val logger = KotlinLogging.logger() {}

    override fun uploadFile(file: MultipartFile, directory: String): String {
        val obj = PutObjectArgs.builder()
            .bucket(appBucket)
            .`object`("${directory}/" + file.originalFilename)
            .stream(file.inputStream, file.size, -1)
            .build()
        minioClient.putObject(obj)

        return "${directory}/${file.originalFilename}"
    }

    override fun downloadFile(fileName: String): ByteArray {
        val obj = minioClient.getObject(
            GetObjectArgs.builder()
                .bucket(appBucket)
                .`object`(fileName)
                .build()
        )
        return obj.readAllBytes()
    }
}