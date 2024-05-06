package prulde.controller

import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ExceptionHandlerController {
    private val logger = KotlinLogging.logger() {}

    @ExceptionHandler(value = [NoSuchElementException::class])
    fun handleNoSuchElement(ex: NoSuchElementException): ResponseEntity<String> {
        val error = "ERROR: $ex"
        logger.error { error }
        return ResponseEntity(error, HttpStatus.NOT_FOUND)
    }
}