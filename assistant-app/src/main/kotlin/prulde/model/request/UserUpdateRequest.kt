package prulde.model.request

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
data class UserUpdateRequest(
    val name: String? = null,
    val surname: String? = null,
    val patronymic: String? = null,
    val email: String? = null,
    val userCompetencies: List<UserCompetency>? = null,
) {
    @Schema(name = "UserUpdateRequest\$UserCompetency")
    data class UserCompetency(
        val id: Long? = null,
        val completed: Boolean? = null,
        val testAttempts: List<TestAttempt>? = null,
    ) {
        @Schema(name = "UserUpdateRequest\$UserCompetency\$TestAttempt")
        data class TestAttempt(
            val solutionDuration: Int,
            val answers: List<Answer>,
        ) {
            @Schema(name = "UserUpdateRequest\$UserCompetency\$TestAttempt\$Answer")
            data class Answer(
                val option: String,
                val isCorrect: Boolean,
            )
        }
    }
}