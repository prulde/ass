package prulde.model.request

data class UserRegisterRequest(
    val name: String,
    val surname: String,
    val patronymic: String,
    val email: String,
)