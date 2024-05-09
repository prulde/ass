package prulde.model.request

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema

@JsonInclude(JsonInclude.Include.NON_NULL)
data class TestQuestionPostRequest(
    val description: String? = null,
    val answerOptions: List<AnswerOption>? = null,
) {
    @Schema(name = "TestQuestionPostRequest\$AnswerOption")
    data class AnswerOption(
        val option: String? = null,
        val isCorrect: Boolean? = null,
    )
}