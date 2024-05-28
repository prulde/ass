package prulde.model.request

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CompetencyPatchRequest(
    val name: String? = null,
    val priority: Int? = null,
    val level: String? = null,
    val testTimeMinutes: Int? = null,
    val passThreshold: Int? = null,
)