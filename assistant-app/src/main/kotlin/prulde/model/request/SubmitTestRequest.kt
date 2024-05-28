package prulde.model.request

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema

@JsonInclude(JsonInclude.Include.NON_NULL)
data class SubmitTestRequest(
    val solutionDuration: Int,
    val answers: List<Answer>? = null,
) {
    @Schema(name = "SubmitTestRequest\$Answer")
    data class Answer(
        val answerOptionId: Long? = null,
    )
}