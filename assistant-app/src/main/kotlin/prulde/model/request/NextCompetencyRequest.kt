package prulde.model.request

data class NextCompetencyRequest(
    val competencyId: Long,
    val currentLevel: Int,
)