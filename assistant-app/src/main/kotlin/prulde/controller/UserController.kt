package prulde.controller

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*
import prulde.model.CompetencyModel
import prulde.model.UserModel
import prulde.model.request.SubmitTestRequest
import prulde.model.request.UserPatchRequest
import prulde.model.request.UserRegisterRequest
import prulde.service.UserService

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService,
) {
    @Tag(name = "client")
    @PostMapping()
    fun register(@RequestBody request: UserRegisterRequest): UserModel =
        userService.register(request)

    @Tag(name = "client")
    @PostMapping("/{id}/competencies")
    fun addCompetency(
        @PathVariable(name = "id") id: Long,
        @RequestParam(name = "competencyId") competencyId: Long,
    ): UserModel? =
        userService.addUserCompetency(id, competencyId)

    @Tag(name = "client")
    @PostMapping("/{id}/competencies/{competencyId}/test")
    fun submitTest(
        @PathVariable(name = "id") id: Long,
        @PathVariable(name = "competencyId") competencyId: Long,
        @RequestBody request: SubmitTestRequest,
    ) =
        userService.submitTest(id, competencyId, request)

    @Tag(name = "client")
    @PatchMapping("/{id}")
    fun updatePrimaryUserInfo(
        @PathVariable(name = "id") id: Long,
        @RequestBody request: UserPatchRequest
    ): UserModel? =
        userService.updatePrimaryUserInfo(id, request)

    @Tag(name = "admin")
    @GetMapping()
    fun filter(
        @RequestParam(required = false) fullName: String? = null,
        @RequestParam(defaultValue = "1", required = false) page: Int,
        @RequestParam(defaultValue = "10", required = false) size: Int,
    ): List<UserModel> =
        userService.filterUsers(fullName, page, size)

    @Tag(name = "common")
    @GetMapping("/{email}")
    fun getUserByEmail(@PathVariable(name = "email") email: String): UserModel? =
        userService.getUserByEmail(email)

    @Tag(name = "client")
    @GetMapping("/{id}/competencies/{competencyId}/next-level")
    fun getUserCompetencyNextLevel(
        @PathVariable(name = "id") id: Long,
        @PathVariable(name = "competencyId") competencyId: Long
    ): CompetencyModel? =
        userService.getUserCompetencyNextLevel(id, competencyId)


    @Tag(name = "client")
    @DeleteMapping("/{id}")
    fun makeUserInactive(@PathVariable(name = "id") id: Long): Unit =
        userService.makeUserInactive(id)
}