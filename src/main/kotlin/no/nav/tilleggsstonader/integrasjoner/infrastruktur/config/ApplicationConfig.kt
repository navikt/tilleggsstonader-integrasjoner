package no.nav.tilleggsstonader.integrasjoner.infrastruktur.config

import no.nav.security.token.support.client.spring.oauth2.EnableOAuth2Client
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import no.nav.tilleggsstonader.libs.http.config.RestTemplateConfiguration
import no.nav.tilleggsstonader.libs.log.filter.LogFilterConfiguration
import org.springframework.boot.SpringBootConfiguration
import org.springframework.context.annotation.Import

@SpringBootConfiguration
@EnableJwtTokenValidation(ignore = ["org.springframework", "org.springdoc"])
@EnableOAuth2Client(cacheEnabled = true)
@Import(
    RestTemplateConfiguration::class,
    LogFilterConfiguration::class,
)
class ApplicationConfig
