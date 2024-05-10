package prulde

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AssistantAppGatewayApplication

fun main(args: Array<String>) {
    runApplication<AssistantAppGatewayApplication>(*args)
}
