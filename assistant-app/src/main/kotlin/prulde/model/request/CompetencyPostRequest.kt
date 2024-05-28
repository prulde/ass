package prulde.model.request

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CompetencyPostRequest(
    val name: String? = null,
    val priority: Int? = null,
    val level: String? = null,
    val testTimeMinutes: Int? = null,
    val passThreshold: Int? = null,
    val skills: List<Skill>? = null,
    val questions: List<TestQuestion>? = null,
) {
    @Schema(name = "CompetencyPostRequest\$Skill")
    data class Skill(
        val name: String? = null,
        val markdowns: List<String>? = null,
    )

    @Schema(name = "CompetencyPostRequest\$TestQuestion")
    data class TestQuestion(
        val description: String? = null,
        val answerOptions: List<AnswerOption>? = null,
    ) {
        @Schema(name = "CompetencyPostRequest\$TestQuestion\$AnswerOption")
        data class AnswerOption(
            val option: String? = null,
            val isCorrect: Boolean? = null,
        )
    }
}