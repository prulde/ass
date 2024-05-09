package prulde.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import prulde.service.CompetencyService
import prulde.model.CompetencyModel
import prulde.model.request.*

@RestController
@RequestMapping("/api/competencies")
class CompetencyController(
    private val competencyService: CompetencyService
) {

    @PutMapping("/{id}/skills")
    fun addOrUpdateCompetencySkills(
        @PathVariable id: Long,
        @RequestBody request: List<SkillUpdateRequest>
    ): CompetencyModel? = competencyService.updateCompetencySkills(id, request)

    @PatchMapping("/{id}")
    fun patchCompetency(
        @PathVariable id: Long,
        @RequestBody request: CompetencyPatchRequest
    ): ResponseEntity<CompetencyModel?> {
        val response = competencyService.patchCompetency(id, request)
        return ResponseEntity(response,
            response?.run { HttpStatus.OK } ?: HttpStatus.BAD_REQUEST)
    }

    @PatchMapping("/{id}/test-questions/{questionId}")
    fun patchTestQuestion(
        @PathVariable id: Long,
        @PathVariable questionId: Long,
        @RequestBody request: TestQuestionPatchRequest,
    ): CompetencyModel? = competencyService.patchTestQuestion(id, questionId, request)

    @PostMapping("/{id}/test-questions")
    fun addTestQuestions(
        @PathVariable id: Long,
        @RequestBody request: List<TestQuestionPostRequest>
    ): CompetencyModel? = competencyService.postTestQuestions(id, request)

    @PostMapping()
    fun postCompetency(@RequestBody request: CompetencyPostRequest): CompetencyModel =
        competencyService.postCompetency(request)

    @GetMapping()
    fun filter(
        @RequestParam(required = false) name: String? = null,
        @RequestParam(defaultValue = "1", required = false) page: Int,
        @RequestParam(defaultValue = "10", required = false) size: Int,
    ): List<CompetencyModel> =
        competencyService.filterCompetencies(name, page, size)

    @GetMapping("/unique-names")
    fun listNames(): List<CompetencyModel> =
        competencyService.listCompetencyNames()

    @GetMapping("/{id}")
    fun getCompetencyById(@PathVariable id: Long): CompetencyModel =
        competencyService.getCompetencyById(id)

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): Unit = competencyService.deleteCompetency(id)

    @DeleteMapping("/{id}/skills/{skillId}")
    fun deleteSkill(
        @PathVariable id: Long,
        @PathVariable skillId: Long,
    ): Unit = competencyService.deleteCompetencySkill(id, skillId)

    @DeleteMapping("/{id}/test-questions/{questionId}")
    fun deleteTestQuestion(
        @PathVariable id: Long,
        @PathVariable questionId: Long,
    ): Unit = competencyService.deleteTestQuestion(id, questionId)
}