package prulde.model

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
data class UserModel(
    val id: Long? = null,
    val name: String? = null,
    val surname: String? = null,
    val patronymic: String? = null,
    val fullName: String? = null,
    val email: String? = null,
    val userCompetencies: List<UserCompetency>? = null,
) {
    @Schema(name = "UserModel\$UserCompetency")
    data class UserCompetency(
        val id: Long? = null,
        val name: String? = null,
        val priority: Int? = null,
        val level: String? = null,
        val testTimeMinutes: Int? = null,
        val passThreshold: Int? = null,
        val completed: Boolean? = null,
        val testAttempts: List<TestAttempt>? = null,
    ) {
        @Schema(name = "UserModel\$UserCompetency\$TestAttempt")
        data class TestAttempt(
            val solutionDuration: Int? = null,
            val uploadedAt: LocalDateTime? = null,
            val answers: List<Answer>? = null,
        ) {
            @Schema(name = "UserModel\$UserCompetency\$TestAttempt\$Answer")
            data class Answer(
                val option: String? = null,
                val isCorrect: Boolean? = null,
            )
        }
    }
}