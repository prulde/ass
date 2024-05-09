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
    @Schema(name = "CompetencyUpdateRequest\$Skill")
    data class Skill(
        val id: Long? = null,
        val name: String? = null,
        val markdowns: List<String>? = null,
    )

    @Schema(name = "CompetencyUpdateRequest\$TestQuestion")
    data class TestQuestion(
        val id: Long? = null,
        val description: String? = null,
        val answerOptions: List<AnswerOption>? = null,
    ) {
        @Schema(name = "CompetencyUpdateRequest\$TestQuestion\$AnswerOption")
        data class AnswerOption(
            val id: Long? = null,
            val option: String? = null,
            val isCorrect: Boolean? = null,
        )
    }
}