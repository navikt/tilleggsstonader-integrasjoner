package no.nav.tilleggsstonader.integrasjoner.mocks

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.tilleggsstonader.integrasjoner.aap.AAPClient
import no.nav.tilleggsstonader.integrasjoner.aap.AAPPeriode
import no.nav.tilleggsstonader.integrasjoner.aap.AAPPerioderResponse
import no.nav.tilleggsstonader.integrasjoner.aap.Periode
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import java.time.LocalDate

@Configuration
@Profile("mock-aap")
class AAPClientTestConfig {

    @Bean
    @Primary
    fun aapClient(): AAPClient {
        val client = mockk<AAPClient>()
        resetMock(client)
        return client
    }

    companion object {
        private val periode = AAPPeriode(
            aktivitetsfaseNavn = "Arbeidsutprøving",
            aktivitetsfaseKode = "AU",
            periode = Periode(LocalDate.now(), LocalDate.now()),
        )
        fun resetMock(client: AAPClient) {
            clearMocks(client)
            every { client.hentPerioder(any(), any(), any()) } returns AAPPerioderResponse(listOf(periode))
        }
    }
}
