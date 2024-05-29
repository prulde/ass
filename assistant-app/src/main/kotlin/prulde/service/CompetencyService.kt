package prulde.service

import org.jooq.impl.DSL.*
import org.jooq.*
import org.jooq.DSLContext
import org.jooq.kotlin.mapping

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import prulde.keys.SKILL__SKILL_COMPETENCY_ID_FKEY
import prulde.model.CompetencyModel
import prulde.model.request.*
import prulde.tables.Competency.Companion.COMPETENCY
import prulde.tables.Skill.Companion.SKILL
import prulde.tables.TestQuestion.Companion.TEST_QUESTION
import prulde.tables.AnswerOption.Companion.ANSWER_OPTION
import prulde.tables.records.AnswerOptionRecord
import prulde.tables.records.CompetencyRecord
import prulde.tables.records.SkillRecord
import prulde.tables.records.TestQuestionRecord
import prulde.util.BadInputException

@Service
class CompetencyService(
    private val dsl: DSLContext,
) {

    @Transactional(readOnly = true)
    fun listCompetencyNames(): List<CompetencyModel> =
        dsl.selectDistinct(
            COMPETENCY.ID,
            COMPETENCY.NAME
        )
            .from(COMPETENCY)
            .where(COMPETENCY.PRIORITY.eq(1))
            .map {
                CompetencyModel(
                    id = it.value1(),
                    name = it.value2(),
                )
            }

    @Transactional(readOnly = true)
    fun filterCompetencies(name: String?, page: Int, size: Int): List<CompetencyModel> =
        dsl.select(
            COMPETENCY.ID,
            COMPETENCY.NAME,
            COMPETENCY.LEVEL,
            COMPETENCY.PRIORITY
        )
            .from(COMPETENCY)
            .where(coalesce(name, null).isNull.or(COMPETENCY.NAME.eq(name)))
            .orderBy(COMPETENCY.NAME.asc(), COMPETENCY.PRIORITY.asc())
            .limit(size)
            .offset(size * (page - 1))
            .map {
                CompetencyModel(
                    id = it.value1(),
                    name = it.value2(),
                    level = it.value3(),
                    priority = it.value4(),
                )
            }

    @Transactional(readOnly = true)
    fun getCompetencyById(id: Long): CompetencyModel =
        dsl.select(
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
                            ANSWER_OPTION.IS_CORRECT
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
            .where(COMPETENCY.ID.eq(id))
            .fetchOne(Records.mapping(::CompetencyModel))
            ?: throw NoSuchElementException("Competency with ${id} not found!")


    @Transactional
    fun patchCompetency(id: Long, request: CompetencyPatchRequest): CompetencyModel? {
        dsl.fetchOne(COMPETENCY, COMPETENCY.ID.eq(id))
            ?: return null

        dsl.update(COMPETENCY)
            .set(COMPETENCY.NAME, request.name)
            .set(COMPETENCY.PRIORITY, request.priority)
            .set(COMPETENCY.LEVEL, request.level)
            .set(COMPETENCY.TEST_TIME_MINUTES, request.testTimeMinutes)
            .set(COMPETENCY.PASS_THRESHOLD, request.passThreshold)
            .where(COMPETENCY.ID.eq(id))
            .execute()

        return getCompetencyById(id)
    }

    @Transactional
    fun patchTestQuestion(id: Long, questionId: Long, request: TestQuestionPatchRequest): CompetencyModel? {
        val question = dsl.fetchOne(TEST_QUESTION, TEST_QUESTION.ID.eq(questionId))
            ?: return null

        question.questionDescription = request.description
        question.store()

        val answerOptions =
            dsl.fetch(ANSWER_OPTION, ANSWER_OPTION.ID.`in`(request.answerOptions?.map { it.id })).toList()

        try {
            // bad:(
            request.answerOptions?.forEachIndexed { index, ao ->
                answerOptions[index].let {
                    it.id = ao.id
                    it.tqId = questionId
                    it.option = ao.option
                    it.isCorrect = ao.isCorrect
                }
            }
        } catch (e: Exception) {
            throw BadInputException(e.message)
        }


        dsl.batchUpdate(answerOptions)
            .execute()

        return getCompetencyById(id)
    }

    @Transactional
    fun updateCompetencySkills(id: Long, request: List<SkillUpdateRequest>): CompetencyModel? {
        val competencyRecord = dsl.fetchOne(COMPETENCY, COMPETENCY.ID.eq(id))
            ?: return null

        competencyRecord.fetchChildren(SKILL__SKILL_COMPETENCY_ID_FKEY)
            .forEach {
                it.delete()
            }

        val skills: MutableList<SkillRecord> = mutableListOf()
        request.forEach { skill ->
            skills.add(
                SkillRecord(
                    competencyId = id,
                    name = skill.name,
                    fileName = skill.markdowns?.joinToString(separator = ",")
                )
            )
        }

        dsl.batchInsert(skills)
            .execute()

        return getCompetencyById(id)
    }

    @Transactional
    fun postTestQuestions(id: Long, request: List<TestQuestionPostRequest>): CompetencyModel? {
        dsl.fetchOne(COMPETENCY, COMPETENCY.ID.eq(id))
            ?: return null


        val result = dsl.insertInto(TEST_QUESTION)
            .columns(TEST_QUESTION.COMPETENCY_ID, TEST_QUESTION.QUESTION_DESCRIPTION)
            .apply { request.forEach { values(id, it.description) } }
            .returning(TEST_QUESTION.ID)
            .fetch()

        val answerOptions: MutableList<AnswerOptionRecord> = mutableListOf()
        try {
            result.forEachIndexed { index, testQuestionRecord ->
                request[index].answerOptions?.forEach { option ->
                    answerOptions.add(
                        AnswerOptionRecord(
                            tqId = testQuestionRecord.id,
                            option = option.option,
                            isCorrect = option.isCorrect,
                        )
                    )
                }
            }
        } catch (e: Exception) {
            throw BadInputException(e.message)
        }


        dsl.batchInsert(answerOptions)
            .execute()

        return getCompetencyById(id)
    }

    @Transactional
    fun deleteCompetencySkill(id: Long, skillId: Long) {
        // 1=ok, 0=bad
        val result = dsl.deleteFrom(SKILL)
            .where(SKILL.ID.eq(skillId))
            .execute()
    }

    @Transactional
    fun deleteCompetency(id: Long) {
        // 1=ok, 0=bad
        val result = dsl.delete(COMPETENCY)
            .where(COMPETENCY.ID.eq(id))
            .execute()
    }

    @Transactional
    fun deleteTestQuestion(id: Long, questionId: Long) {
        // 1=ok, 0=bad
        val result = dsl.deleteFrom(TEST_QUESTION)
            .where(TEST_QUESTION.ID.eq(questionId))
            .execute()
    }

    @Transactional
    fun postCompetency(request: CompetencyPostRequest): CompetencyModel {
        val competencyRecord = dsl.newRecord(COMPETENCY)
        competencyRecord.let {
            it.name = request.name
            it.priority = request.priority
            it.level = request.level
            it.testTimeMinutes = request.testTimeMinutes
            it.passThreshold = request.passThreshold
        }

        competencyRecord.store()

        val skills: MutableList<SkillRecord> = mutableListOf()
        request.skills?.forEach { skill ->
            skills.add(
                SkillRecord(
                    competencyId = competencyRecord.id,
                    name = skill.name,
                    fileName = skill.markdowns?.joinToString(separator = ",")
                )
            )
        }

        dsl.batchInsert(skills)
            .execute()


        val result = dsl.insertInto(TEST_QUESTION)
            .columns(TEST_QUESTION.COMPETENCY_ID, TEST_QUESTION.QUESTION_DESCRIPTION)
            .apply { request.questions?.forEach { values(competencyRecord.id, it.description) } }
            .returning(TEST_QUESTION.ID)
            .fetch()

        val answerOptions: MutableList<AnswerOptionRecord> = mutableListOf()
        result.forEachIndexed { index, testQuestionRecord ->
            request.questions?.get(index)?.answerOptions?.forEach { option ->
                answerOptions.add(
                    AnswerOptionRecord(
                        tqId = testQuestionRecord.id,
                        option = option.option,
                        isCorrect = option.isCorrect,
                    )
                )
            }
        }

        dsl.batchInsert(answerOptions)
            .execute()

        return getCompetencyById(competencyRecord.id!!)
    }
}