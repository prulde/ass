package prulde.controller

import org.springframework.web.bind.annotation.*
import prulde.model.UserModel
import prulde.model.request.NextCompetencyRequest
import prulde.model.request.UserUpdateRequest
import prulde.service.UserService

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService,
) {

    @PatchMapping("/{id}")
    fun update(
        @PathVariable(name = "id") id: Long,
        @RequestBody request: UserUpdateRequest,
    ): UserModel =
        userService.updateUser(id, request)

    @GetMapping()
    fun filter(
        @RequestParam(required = false) fullName: String? = null,
        @RequestParam(defaultValue = "0", required = false) page: Int,
        @RequestParam(defaultValue = "10", required = false) size: Int,
    ): List<UserModel> =
        userService.filterUsers(fullName)

    @GetMapping("/{id}")
    fun getUserById(@PathVariable(name = "id") id: Long): UserModel =
        userService.getUserById(id)

    @DeleteMapping("/{id}")
    fun makeUserInactive(@PathVariable(name = "id") id: Long): Unit =
        userService.makeUserInactive(id)

    @GetMapping("/{id}/next-level")
    fun getNextCompetencyLevel(@RequestBody request: NextCompetencyRequest) {

    }
}