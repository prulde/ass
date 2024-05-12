package prulde.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtClaimNames
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.web.server.SecurityWebFilterChain
import reactor.core.publisher.Mono


@Configuration
@EnableWebFluxSecurity
class SecurityConfiguration {
    private val swagger = listOf(
        "/v3/api-docs/swagger-config",
        "/swagger-ui/**",
        "/main-app/api-docs",
        "/file-manager/api-docs",
        "/webjars/swagger-ui/**"
    )

    private val admin = listOf(
        "/api/competencies/**",
    )

    @Bean
    fun springSecurityFilterChain(
        http: ServerHttpSecurity
    ): SecurityWebFilterChain {
        http.authorizeExchange { e ->
            e
                .pathMatchers(*swagger.toTypedArray()).permitAll()
                .pathMatchers(*admin.toTypedArray()).hasRole("ADMIN")
                .anyExchange().authenticated()
        }
            .cors { c -> c.disable() }
            .csrf { c -> c.disable() }
            .oauth2ResourceServer { s ->
                s.jwt { it.jwtAuthenticationConverter(keycloakJwtAuthenticationConverter()) }
            }
        return http.build()
    }


    private fun keycloakJwtAuthenticationConverter(): Converter<Jwt, Mono<AbstractAuthenticationToken>> {
        return Converter<Jwt, Mono<AbstractAuthenticationToken>> { jwt ->
            try {
                val claims = jwt.claims

                val roles = extractRolesFromClaims(claims)
                val authorities: List<GrantedAuthority> = roles.map { SimpleGrantedAuthority("ROLE_$it") }
                val principalName = jwt.getClaimAsString(JwtClaimNames.SUB)

                val token = JwtAuthenticationToken(jwt, authorities, principalName)

                Mono.just(token)
            } catch (e: Exception) {
                Mono.error(e)
            }
        }
    }

    private fun extractRolesFromClaims(claims: Map<String, Any>): List<String> {
        val roles = mutableListOf<String>()
        if (claims.containsKey("realm_access")) {
            val realmAccess = claims.get("realm_access") as Map<String, Any>
            val realmRoles = realmAccess.get("roles") as List<String>
            roles.addAll(realmRoles)
        }
        return roles
    }
}