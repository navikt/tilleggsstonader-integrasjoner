package no.nav.tilleggsstonader.integrasjoner.dokdist

import io.mockk.every
import io.mockk.mockk
import no.nav.tilleggsstonader.integrasjoner.dokdist.domene.DistribuerJournalpostResponseTo
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Configuration
@Profile("mock-dokdist")
class DokdistRestClientTestConfig {
    @Bean
    @Primary
    fun dokdistMockRestClient(): DokdistRestClient {
        val klient = mockk<DokdistRestClient>(relaxed = true)
        val response = DistribuerJournalpostResponseTo("fd5a2ccb-a303-42fd-92fa-f5db70e7f324")

        every {
            klient.distribuerJournalpost(any())
        } returns response

        return klient
    }
}
