package prulde.service

import org.jooq.impl.DSL.*
import org.jooq.DSLContext
import org.jooq.Records
import org.jooq.kotlin.mapping
import prulde.tables.Users.Companion.USERS
import prulde.tables.Skill.Companion.SKILL
import prulde.tables.TestQuestion.Companion.TEST_QUESTION
import prulde.tables.AnswerOption.Companion.ANSWER_OPTION
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import prulde.model.CompetencyModel
import prulde.model.UserModel
import prulde.model.request.SubmitTestRequest
import prulde.model.request.UserPatchRequest
import prulde.model.request.UserRegisterRequest
import prulde.tables.records.AnswerOptionRecord
import prulde.tables.records.UserAnswersRecord
import prulde.tables.references.COMPETENCY
import prulde.tables.references.TEST_ATTEMPT
import prulde.tables.references.USER_ANSWERS
import prulde.tables.references.USER_COMPETENCY
import java.time.LocalDateTime

@Service
class UserService(
    private val dsl: DSLContext,
) {

    @Transactional(readOnly = true)
    fun filterUsers(fullName: String?, page: Int, size: Int): List<UserModel> =
        dsl.select(
            USERS.ID,
            USERS.EMAIL,
            USERS.FULL_NAME,
        )
            .from(USERS)
            .where(
                coalesce(fullName, null).isNull.or(USERS.FULL_NAME.like("%${fullName}%")).and(USERS.IS_ACTIVE.eq(true))
            )
            .orderBy(USERS.FULL_NAME.asc())
            .limit(size)
            .offset(size * (page - 1))
            .map {
                UserModel(
                    id = it.value1(),
                    email = it.value2(),
                    fullName = it.value3()
                )
            }

    @Transactional(readOnly = true)
    fun getUserByEmail(email: String): UserModel? =
        dsl.select(
            USERS.ID,
            USERS.NAME,
            USERS.SURNAME,
            USERS.PATRONYMIC,
            USERS.FULL_NAME,
            USERS.EMAIL,
            multiset(
                select(
                    USER_COMPETENCY.competency().ID,
                    USER_COMPETENCY.competency().NAME,
                    USER_COMPETENCY.competency().PRIORITY,
                    USER_COMPETENCY.competency().LEVEL,
                    USER_COMPETENCY.competency().TEST_TIME_MINUTES,
                    USER_COMPETENCY.competency().PASS_THRESHOLD,
                    USER_COMPETENCY.COMPLETED,
                    multiset(
                        select(
                            TEST_ATTEMPT.SOLUTION_DURATION,
                            TEST_ATTEMPT.UPLOADED_AT,
                            multiset(
                                select(
                                    USER_ANSWERS.answerOption().OPTION,
                                    USER_ANSWERS.answerOption().IS_CORRECT,
                                )
                                    .from(USER_ANSWERS)
                                    .where(USER_ANSWERS.TEST_ATTEMPT_ID.eq(TEST_ATTEMPT.ID))
                            ).mapping(UserModel.UserCompetency.TestAttempt::Answer)
                        )
                            .from(TEST_ATTEMPT)
                            .where(TEST_ATTEMPT.USER_COMPETENCY_ID.eq(USER_COMPETENCY.ID))
                    ).mapping(UserModel.UserCompetency::TestAttempt)
                )
                    .from(USER_COMPETENCY)
                    .where(USER_COMPETENCY.users().EMAIL.eq(email))
            ).mapping(UserModel::UserCompetency)
        )
            .from(USERS)
            .where(USERS.EMAIL.eq(email).and(USERS.IS_ACTIVE.eq(true)))
            .fetchOne(Records.mapping(::UserModel))
            ?: throw NoSuchElementException("User with ${email} not found!")

    @Transactional(readOnly = true)
    fun getUserById(id: Long): UserModel? =
        dsl.select(
            USERS.ID,
            USERS.NAME,
            USERS.SURNAME,
            USERS.PATRONYMIC,
            USERS.FULL_NAME,
            USERS.EMAIL,
            multiset(
                select(
                    USER_COMPETENCY.competency().ID,
                    USER_COMPETENCY.competency().NAME,
                    USER_COMPETENCY.competency().PRIORITY,
                    USER_COMPETENCY.competency().LEVEL,
                    USER_COMPETENCY.competency().TEST_TIME_MINUTES,
                    USER_COMPETENCY.competency().PASS_THRESHOLD,
                    USER_COMPETENCY.COMPLETED,
                    multiset(
                        select(
                            TEST_ATTEMPT.SOLUTION_DURATION,
                            TEST_ATTEMPT.UPLOADED_AT,
                            multiset(
                                select(
                                    USER_ANSWERS.answerOption().OPTION,
                                    USER_ANSWERS.answerOption().IS_CORRECT,
                                )
                                    .from(USER_ANSWERS)
                                    .where(USER_ANSWERS.TEST_ATTEMPT_ID.eq(TEST_ATTEMPT.ID))
                            ).mapping(UserModel.UserCompetency.TestAttempt::Answer)
                        )
                            .from(TEST_ATTEMPT)
                            .where(TEST_ATTEMPT.USER_COMPETENCY_ID.eq(USER_COMPETENCY.ID))
                    ).mapping(UserModel.UserCompetency::TestAttempt)
                )
                    .from(USER_COMPETENCY)
                    .where(USER_COMPETENCY.USER_ID.eq(id))
            ).mapping(UserModel::UserCompetency)
        )
            .from(USERS)
            .where(USERS.ID.eq(id).and(USERS.IS_ACTIVE.eq(true)))
            .fetchOne(Records.mapping(::UserModel))
            ?: throw NoSuchElementException("User with ${id} not found!")

    @Transactional
    fun register(request: UserRegisterRequest): UserModel {
        val newUserRecord = dsl.newRecord(USERS)
        newUserRecord.isActive = true
        newUserRecord.name = request.name
        newUserRecord.surname = request.surname
        newUserRecord.patronymic = request.patronymic
        newUserRecord.fullName = "${request.name} ${request.surname} ${request.patronymic}"
        newUserRecord.store()
        return UserModel(
            name = newUserRecord.name,
            surname = newUserRecord.surname,
            patronymic = newUserRecord.patronymic,
            email = newUserRecord.email,
        )
    }

    @Transactional(readOnly = true)
    fun getUserCompetencyNextLevel(id: Long, competencyId: Long): CompetencyModel? {
        val previousCompetencyLevel = dsl.fetchOne(COMPETENCY, COMPETENCY.ID.eq(competencyId)) ?: return null

        return dsl.select(
            COMPETENCY.ID,
            COMPETENCY.NAME,
            COMPETENCY.PRIORITY,
            COMPETENCY.LEVEL,
            COMPETENCY.TEST_TIME_MINUTES,
            COMPETENCY.PASS_THRESHOLD,
            multiset(
                select(
                    SKILL.ID,
                    SKILL.NAME,
                    SKILL.FILE_NAME
                )
                    .from(SKILL)
                    .where(SKILL.COMPETENCY_ID.eq(COMPETENCY.ID))
            ).mapping(CompetencyModel::SkillModel),
            multiset(
                select(
                    TEST_QUESTION.ID,
                    TEST_QUESTION.QUESTION_DESCRIPTION,
                    multiset(
                        select(
                            ANSWER_OPTION.ID,
                            ANSWER_OPTION.OPTION,
                        )
                            .from(ANSWER_OPTION)
                            .where(ANSWER_OPTION.TQ_ID.eq(TEST_QUESTION.ID))
                    ).mapping(CompetencyModel.TestQuestionModel::AnswerOptionModel)
                )
                    .from(TEST_QUESTION)
                    .where(TEST_QUESTION.COMPETENCY_ID.eq(COMPETENCY.ID))
            ).mapping(CompetencyModel::TestQuestionModel)
        )
            .from(COMPETENCY)
            .where(
                COMPETENCY.NAME.eq(previousCompetencyLevel.name)
                    .and(COMPETENCY.PRIORITY.eq(previousCompetencyLevel.priority?.plus(1)))
            )
            .fetchOne(Records.mapping(::CompetencyModel))
            ?: throw NoSuchElementException("Competency with ${id} not found!")
    }

    @Transactional
    fun addUserCompetency(id: Long, competencyId: Long): UserModel? {
        dsl.fetchOne(USERS, USERS.ID.eq(id)) ?: return null

        val userCompetencyRecord = dsl.newRecord(USER_COMPETENCY)
        userCompetencyRecord.competencyId = competencyId
        userCompetencyRecord.userId = id

        dsl.batchInsert(userCompetencyRecord)
            .execute()

        return getUserById(id)
    }

    @Transactional
    fun submitTest(id: Long, competencyId: Long, request: SubmitTestRequest) {
        val userCompetencyRecord = dsl.fetchOne(
            USER_COMPETENCY, USER_COMPETENCY.COMPETENCY_ID.eq(competencyId).and(
                USER_COMPETENCY.USER_ID.eq(id)
            )
        )
            ?: return

        val competencyRecord = dsl.fetchOne(COMPETENCY, COMPETENCY.ID.eq(userCompetencyRecord.competencyId))
            ?: return

        val testAttemptRecord = dsl.newRecord(TEST_ATTEMPT)
        testAttemptRecord.userCompetencyId = userCompetencyRecord.id
        testAttemptRecord.solutionDuration = request.solutionDuration
        testAttemptRecord.uploadedAt = LocalDateTime.now()
        testAttemptRecord.store()

        val answerRecords: MutableList<UserAnswersRecord> = mutableListOf()
        val aoIds: MutableList<Long?> = mutableListOf()

        request.answers?.forEach {
            val answerRecord = dsl.newRecord(USER_ANSWERS)
            answerRecord.testAttemptId = testAttemptRecord.id
            answerRecord.aoId = it.answerOptionId
            answerRecords.add(answerRecord)

            aoIds.add(it.answerOptionId)
        }

        dsl.batchInsert(answerRecords)
            .execute()

        if (request.solutionDuration > competencyRecord.testTimeMinutes!!)
            return

        val answerOptionRecords: MutableList<AnswerOptionRecord> =
            dsl.fetch(ANSWER_OPTION, ANSWER_OPTION.ID.`in`(aoIds))

        var passedCount: Int = 0
        answerOptionRecords.forEach {
            if (it.isCorrect == true)
                passedCount += 1
        }

        if (passedCount >= competencyRecord.passThreshold!!) {
            userCompetencyRecord.completed = true
            userCompetencyRecord.store()
        }
    }

    @Transactional
    fun updatePrimaryUserInfo(id: Long, request: UserPatchRequest): UserModel? {
        dsl.fetchOne(USERS, USERS.ID.eq(id)) ?: return null

        dsl.update(USERS)
            .set(USERS.NAME, request.name)
            .set(USERS.SURNAME, request.surname)
            .set(USERS.PATRONYMIC, request.patronymic)
            .set(USERS.FULL_NAME, "${request.name} ${request.surname} ${request.patronymic}")
            .set(USERS.EMAIL, request.email)
            .where(USERS.ID.eq(id).and(USERS.IS_ACTIVE.eq(true)))
            .execute()

        return getUserById(id)
    }

    @Transactional
    fun makeUserInactive(id: Long): Unit {
        val userRecord = dsl.fetchOne(USERS, USERS.ID.eq(id)) ?: return
        userRecord.isActive = false
        userRecord.store()
    }

}