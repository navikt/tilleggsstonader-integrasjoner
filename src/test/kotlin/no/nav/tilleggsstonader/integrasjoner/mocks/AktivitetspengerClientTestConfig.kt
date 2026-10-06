package no.nav.tilleggsstonader.integrasjoner.mocks

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.tilleggsstonader.integrasjoner.aktivitetspenger.AktivitetspengerClient
import no.nav.tilleggsstonader.integrasjoner.aktivitetspenger.AktivitetspengerPeriode
import no.nav.tilleggsstonader.integrasjoner.aktivitetspenger.AktivitetspengerPerioderResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import java.time.LocalDate

@Configuration
@Profile("mock-aktivitetspenger")
class AktivitetspengerClientTestConfig {
    @Bean
    @Primary
    fun aktivitetspengerClient(): AktivitetspengerClient {
        val client = mockk<AktivitetspengerClient>()
        resetMock(client)
        return client
    }

    companion object {
        private val perioderResponse =
            AktivitetspengerPerioderResponse(
                perioder =
                    listOf(
                        AktivitetspengerPeriode(
                            fom = LocalDate.now(),
                            tom = LocalDate.now().plusDays(1),
                        ),
                    ),
            )

        fun resetMock(client: AktivitetspengerClient) {
            clearMocks(client)
            every { client.hentPerioder(any(), any(), any()) } returns perioderResponse
        }
    }
}
