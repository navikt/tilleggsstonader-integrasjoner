package no.nav.tilleggsstonader.integrasjoner.infrastruktur.config

import no.nav.security.token.support.client.spring.oauth2.EnableOAuth2Client
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import no.nav.tilleggsstonader.integrasjoner.infrastruktur.ValiderKallErFraTilleggsstønaderFilter
import no.nav.tilleggsstonader.libs.http.config.RestTemplateConfiguration
import no.nav.tilleggsstonader.libs.log.filter.LogFilterConfiguration
import org.slf4j.LoggerFactory
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import

@SpringBootConfiguration
@EnableJwtTokenValidation(ignore = ["org.springframework", "org.springdoc"])
@EnableOAuth2Client(cacheEnabled = true)
@Import(
    RestTemplateConfiguration::class,
    LogFilterConfiguration::class,
)
class ApplicationConfig {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Bean
    fun validerKallErFraTilleggsstønaderFilter(): FilterRegistrationBean<ValiderKallErFraTilleggsstønaderFilter> {
        logger.info("Registering ${ValiderKallErFraTilleggsstønaderFilter::class.simpleName} filter")
        val filterRegistration = FilterRegistrationBean<ValiderKallErFraTilleggsstønaderFilter>()
        filterRegistration.filter = ValiderKallErFraTilleggsstønaderFilter()
        filterRegistration.order = 3
        return filterRegistration
    }
}
