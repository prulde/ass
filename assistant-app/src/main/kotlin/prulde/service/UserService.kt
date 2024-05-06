package prulde.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import prulde.entity.AnswerOptionEntity
import prulde.entity.TestAttemptEntity
import prulde.entity.UserCompetencyEntity
import prulde.entity.UserEntity
import prulde.model.UserModel
import prulde.model.request.UserUpdateRequest
import prulde.repository.CompetencyRepository
import prulde.repository.UserRepository

@Service
class UserService(
    private val userRepository: UserRepository,
    private val competencyRepository: CompetencyRepository,
) {

    @Transactional(readOnly = true)
    fun filterUsers(fullName: String?): List<UserModel> =
        userRepository.getUsersByFilter(fullName)?.map {
            UserModel(
                id = it.id,
                fullName = it.fullName,
                email = it.email,
            )
        } ?: mutableListOf()

    @Transactional(readOnly = true)
    fun getUserById(id: Long): UserModel {
        val userEntity = userRepository.getReferenceById(id)
        if (userEntity.isActive == false)
            throw NoSuchElementException("user with ${id} is inactive")
        return userEntityToUserModel(userEntity)
    }

    @Transactional
    fun updateUser(id: Long, request: UserUpdateRequest): UserModel {
        val userEntity = request.let {
            UserEntity(
                id = id,
                name = it.surname,
                surname = it.surname,
                patronymic = it.patronymic,
                fullName = "${it.name} ${it.surname} ${it.patronymic}",
                email = it.email,
                userCompetencies = it.userCompetencies?.map {
                    UserCompetencyEntity(
                        competency = it.id?.let { competencyRepository.getReferenceById(id) },
                        completed = it.completed,
                        testAttempts = it.testAttempts?.map {
                            TestAttemptEntity(
                                solutionDuration = it.solutionDuration,
                                userAnswers = it.answers?.map {
                                    AnswerOptionEntity(
                                        option = it.option,
                                        isCorrect = it.isCorrect,
                                    )
                                } ?: mutableListOf()
                            )
                        } ?: mutableListOf()
                    )
                } ?: mutableListOf()
            )
        }
        // null pointer??? cuz entity is null????????? OR fullname
        userRepository.save(userEntity)
        return userEntityToUserModel(userEntity)
    }

    @Transactional
    fun makeUserInactive(id: Long): Unit =
        userRepository.makeUserInactive(id)

    private fun userEntityToUserModel(entity: UserEntity): UserModel =
        entity.let {
            UserModel(
                id = it.id,
                name = it.name,
                surname = it.surname,
                patronymic = it.patronymic,
                fullName = it.fullName,
                email = it.email,
                userCompetencies = it.userCompetencies?.map {
                    UserModel.UserCompetency(
                        id = it.competency?.id,
                        name = it.competency?.name,
                        level = it.competency?.level,
                        priority = it.competency?.priority,
                        testTimeMinutes = it.competency?.testTimeMinutes,
                        passThreshold = it.competency?.passThreshold,
                        completed = it.completed,
                        testAttempts = it.testAttempts?.map {
                            UserModel.UserCompetency.TestAttempt(
                                solutionDuration = it.solutionDuration,
                                uploadedAt = it.uploadedAt!!,
                                answers = it.userAnswers?.map {
                                    UserModel.UserCompetency.TestAttempt.Answer(
                                        option = it.option,
                                        isCorrect = it.isCorrect,
                                    )
                                } ?: mutableListOf()
                            )
                        } ?: mutableListOf()
                    )
                } ?: mutableListOf()
            )
        }
}