package prulde

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.servers.Server
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
@OpenAPIDefinition(
    servers = [
        Server(url = "/", description = "main-app")
    ]
)
class AssistantAppApplication

fun main(args: Array<String>) {
    runApplication<AssistantAppApplication>(*args)
}
