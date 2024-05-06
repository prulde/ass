package prulde.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import prulde.service.CompetencyService
import prulde.model.request.CompetencyUpdateRequest
import prulde.model.CompetencyModel

@RestController
@RequestMapping("/api/competencies")
class CompetencyController(
    private val competencyService: CompetencyService
) {

    @PutMapping("/{id}/skills")
    fun addOrUpdateCompetencySkills() {
        
    }

    @PutMapping()
    fun update(
        @RequestBody request: CompetencyUpdateRequest
    ): Unit {
        val response = competencyService.putCompetency(request)
        //return ResponseEntity(response, if (request.id == null) HttpStatus.CREATED else HttpStatus.OK)
    }

    @GetMapping()
    fun filter(
        @RequestParam(required = false) name: String? = null,
        @RequestParam(defaultValue = "0", required = false) page: Int,
        @RequestParam(defaultValue = "10", required = false) size: Int,
    ): List<CompetencyModel> =
        competencyService.filterCompetencies(name, page, size)

    @GetMapping("/{id}")
    fun getCompetencyById(@PathVariable id: Long): CompetencyModel =
        competencyService.getCompetencyById(id)

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): Unit =
        competencyService.deleteCompetency(id)

    @GetMapping("/list-names")
    fun listNames(): List<CompetencyModel> =
        competencyService.listCompetencyNames()
}