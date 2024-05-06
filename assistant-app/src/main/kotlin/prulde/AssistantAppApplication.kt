package prulde

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AssistantAppApplication

fun main(args: Array<String>) {
    runApplication<AssistantAppApplication>(*args)
}
