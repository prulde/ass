package prulde.service

import org.jooq.impl.DSL.*
import org.jooq.*
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.jooq.kotlin.mapping

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import prulde.model.CompetencyModel
import prulde.model.request.CompetencyUpdateRequest
import prulde.tables.Competency.Companion.COMPETENCY
import prulde.tables.Skill.Companion.SKILL
import prulde.tables.Markdown.Companion.MARKDOWN
import prulde.tables.TestQuestion.Companion.TEST_QUESTION
import prulde.tables.AnswerOption.Companion.ANSWER_OPTION
import prulde.tables.records.MarkdownRecord
import prulde.tables.records.SkillRecord

@Service
class CompetencyService(
    private val dsl: DSLContext,
) {

    @Transactional(readOnly = true)
    fun listCompetencyNames(): List<CompetencyModel> =
        dsl.selectDistinct(
            COMPETENCY.NAME
        )
            .from(COMPETENCY)
            .map {
                CompetencyModel(
                    name = it.value1(),
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
                    multiset(
                        select(
                            MARKDOWN.ID,
                            MARKDOWN.FILE_ID,
                        )
                            .from(MARKDOWN)
                            .where(MARKDOWN.SKILL_ID.eq(SKILL.ID))
                    ).mapping(CompetencyModel.SkillModel::FileModel)
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
    fun putCompetency(request: CompetencyUpdateRequest): Unit {
        val competencyRecord = dsl.fetchOne(COMPETENCY, COMPETENCY.ID.eq(request.id))
            ?: dsl.newRecord(COMPETENCY)

        //val skills = competencyRecord.fetchChildren(SKILL__SKILL_COMPETENCY_ID_FKEY).toMutableList()

        //competencyRecord
        dsl.update(COMPETENCY)
            .set(COMPETENCY.NAME, request.name)
            .set(COMPETENCY.PRIORITY, request.priority)
            .set(COMPETENCY.LEVEL, request.level)
            .set(COMPETENCY.TEST_TIME_MINUTES, request.testTimeMinutes)
            .set(COMPETENCY.PASS_THRESHOLD, request.passThreshold)
            .where(COMPETENCY.ID.eq(request.id))
            .execute()

        val skills: MutableList<SkillRecord> = mutableListOf()
        val markdowns: MutableList<MarkdownRecord> = mutableListOf()
        request.skills?.forEach() { skill ->
            skills.add(
                SkillRecord(
                    id = skill.id,
                    competencyId = request.id,
                    name = skill.name
                )
            )
            skill.markdowns?.forEach { markdown ->
                markdowns.add(
                    MarkdownRecord(
                        id = markdown.id,
                        skillId = skill.id,
                        fileId = markdown.fileName,
                    )
                )
            }
        }

        dsl.insertInto(SKILL)
            .columns(SKILL.COMPETENCY_ID, SKILL.NAME)
            .apply { skills.forEach { values(request.id, it.name) } }
            .onConflict(SKILL.ID)
            .doUpdate()
            .set(SKILL.NAME, DSL.field("EXCLUDED.name", SKILL.NAME.dataType))
            .execute()

        // delete rows, insert new instead???
        // check sql for update bt
        val sl = dsl.insertInto(SKILL)
            .columns(SKILL.COMPETENCY_ID, SKILL.NAME)
            .apply { skills.forEach { values(request.id, it.name) } }
            .onConflict(SKILL.ID)
            .doUpdate()
            .set(SKILL.NAME, DSL.field("EXCLUDED.name", SKILL.NAME.dataType))
            .getSQL()


//
//        val competencyEntity: CompetencyEntity = request.let {
//            CompetencyEntity(
//                id = it.id,
//                name = it.name,
//                level = it.level,
//                priority = it.priority,
//                testTimeMinutes = it.testTimeMinutes,
//                passThreshold = it.passThreshold,
//                competencySkills = it.skills?.map {
//                    SkillEntity(
//                        name = it.name,
//                        skillMarkdowns = it.markdowns?.map {
//                            MarkdownEntity(
//                                fileId = it.name,
//                            )
//                        }
//                    )
//                } ?: mutableListOf(),
//                testQuestions = it.questions?.map {
//                    TestQuestionEntity(
//                        questionDescription = it.description,
//                        questionOptions = it.answerOptions?.map {
//                            AnswerOptionEntity(
//                                option = it.option,
//                                isCorrect = it.isCorrect,
//                            )
//                        } ?: mutableListOf()
//                    )
//                } ?: mutableListOf()
//            )
//        }
//        competencyRepository.save(competencyEntity)
//        return competencyEntityToModel(competencyEntity)
    }

    @Transactional
    fun deleteCompetency(id: Long): Unit {
        // 1=ok, 0=bad
        val result = dsl.delete(COMPETENCY)
            .where(COMPETENCY.ID.eq(id))
            .execute()
    }

//    private fun competencyEntityToModel(competency: CompetencyEntity) =
//        competency.let {
//            CompetencyModel(
//                id = it.id,
//                name = it.name,
//                level = it.level,
//                priority = it.priority,
//                testTimeMinutes = it.testTimeMinutes,
//                passThreshold = it.passThreshold,
//                skills = it.competencySkills?.map {
//                    CompetencyModel.SkillModel(
//                        name = it.name,
//                        markdowns = it.skillMarkdowns?.map {
//                            CompetencyModel.SkillModel.FileModel(
//                                fileName = it.fileId
//                            )
//                        }
//                    )
//                },
//                questions = it.testQuestions?.map {
//                    CompetencyModel.TestQuestionModel(
//                        description = it.questionDescription,
//                        answerOptions = it.questionOptions?.map {
//                            CompetencyModel.TestQuestionModel.AnswerOptionModel(
//                                option = it.option,
//                                isCorrect = it.isCorrect
//                            )
//                        }
//                    )
//                }
//            )
//        }
}