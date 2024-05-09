package prulde.model.request

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class SkillUpdateRequest(
    val name: String? = null,
    val markdowns: List<String>? = null,
)