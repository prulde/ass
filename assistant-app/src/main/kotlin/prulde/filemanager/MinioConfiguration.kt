package prulde.filemanager

import io.minio.MinioClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MinioConfiguration(
    @Value("\${file-manager.s3.url}") private val url: String,
    @Value("\${file-manager.s3.credentials.user}") private val user: String,
    @Value("\${file-manager.s3.credentials.password}") private val password: String,
) {

    @Bean
    fun minioClient(): MinioClient =
        MinioClient.builder()
            .endpoint(url)
            .credentials(user, password)
            .build()
}