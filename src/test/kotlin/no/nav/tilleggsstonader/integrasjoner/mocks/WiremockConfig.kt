package no.nav.tilleggsstonader.integrasjoner.mocks

import com.github.tomakehurst.wiremock.common.ConsoleNotifier
import org.springframework.cloud.contract.wiremock.WireMockConfigurationCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class WiremockConfig {
    @Bean
    fun optionsCustomizer(): WireMockConfigurationCustomizer =
        WireMockConfigurationCustomizer { config ->
            config.notifier(ConsoleNotifier(false))
        }
}
