package prulde.service

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
        val path = "${directory}/${file.originalFilename}"
        val obj = PutObjectArgs.builder()
            .bucket(appBucket)
            .`object`(path)
            .stream(file.inputStream, file.size, -1)
            .build()
        minioClient.putObject(obj)

        return path
    }

    override fun downloadFile(filePath: String): ByteArray {
        val obj = minioClient.getObject(
            GetObjectArgs.builder()
                .bucket(appBucket)
                .`object`(filePath)
                .build()
        )
        return obj.readAllBytes()
    }
}