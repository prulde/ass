package prulde.model.request

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class UserPatchRequest(
    val name: String? = null,
    val surname: String? = null,
    val patronymic: String? = null,
    val email: String? = null,
)