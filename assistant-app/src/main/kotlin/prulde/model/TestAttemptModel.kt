package prulde.model

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
data class TestAttemptModel(
    val solutionDuration: Int? = null,
    val uploadedAt: LocalDateTime? = null,
    val answers: List<Answer>? = null,
) {
    @Schema(name = "TestAttemptModel\$Answer")
    data class Answer(
        val option: String? = null,
        val isCorrect: Boolean? = null,
    )
}