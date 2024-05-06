package prulde.configuration

import org.jooq.DSLContext
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class InitDslSettings(
    private val dslContext: DSLContext,
) {

    @EventListener(ContextRefreshedEvent::class)
    fun init() {
        val settings = dslContext.settings()
        settings.isReturnDefaultOnUpdatableRecord = true
        settings.withMapConstructorParameterNamesInKotlin(true)
    }

}