package prulde.model

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CompetencyModel(
    val id: Long? = null,
    val name: String? = null,
    val priority: Int? = null,
    val level: String? = null,
    val testTimeMinutes: Int? = null,
    val passThreshold: Int? = null,
    val skills: List<SkillModel>? = null,
    val questions: List<TestQuestionModel>? = null,
) {
    @Schema(name = "CompetencyCreateRequest\$Skill")
    data class SkillModel(
        val id: Long? = null,
        val name: String? = null,
        val markdowns: List<FileModel>? = null,
    ) {
        @Schema(name = "CompetencyCreateRequest\$Skill\$Model")
        data class FileModel(
            val id: Long? = null,
            val fileName: String? = null,
        )
    }

    @Schema(name = "CompetencyCreateRequest\$TestQuestionModel")
    data class TestQuestionModel(
        val id: Long? = null,
        val description: String? = null,
        val answerOptions: List<AnswerOptionModel>?,
    ) {
        @Schema(name = "CompetencyCreateRequest\$TestQuestionModel\$AnswerOptionModel")
        data class AnswerOptionModel(
            val id: Long? = null,
            val option: String? = null,
            val isCorrect: Boolean? = null,
        )
    }
}