package prulde.model.request

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema

@JsonInclude(JsonInclude.Include.NON_NULL)
data class TestQuestionPatchRequest(
    val description: String? = null,
    val answerOptions: List<AnswerOption>? = null,
) {
    @Schema(name = "TestQuestionPatchRequest\$AnswerOption")
    data class AnswerOption(
        val id: Long? = null,
        val option: String? = null,
        val isCorrect: Boolean? = null,
    )
}