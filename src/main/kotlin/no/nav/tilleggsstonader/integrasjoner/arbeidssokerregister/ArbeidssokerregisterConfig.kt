package no.nav.tilleggsstonader.integrasjoner.arbeidssokerregister

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestTemplate
import java.net.URI

@Configuration
class ArbeidssokerregisterConfig {
    @Bean
    fun arbeidssokerregisterRestClient(
        @Qualifier("azureClientCredential") restTemplate: RestTemplate,
        @Value($$"${clients.arbeidssokerregister.uri}") baseUrl: URI,
    ): RestClient =
        RestClient
            .builder(restTemplate)
            .baseUrl(baseUrl.toString())
            .build()
}
