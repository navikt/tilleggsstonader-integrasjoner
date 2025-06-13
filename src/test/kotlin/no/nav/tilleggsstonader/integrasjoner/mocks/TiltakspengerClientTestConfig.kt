package no.nav.tilleggsstonader.integrasjoner.mocks

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.tilleggsstonader.integrasjoner.tiltakspenger.Periode
import no.nav.tilleggsstonader.integrasjoner.tiltakspenger.TiltakspengerClient
import no.nav.tilleggsstonader.integrasjoner.tiltakspenger.TiltakspengerPerioderResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import java.time.LocalDate

@Configuration
@Profile("mock-tiltakspenger")
class TiltakspengerClientTestConfig {
    @Bean
    @Primary
    fun tiltakspengerClient(): TiltakspengerClient {
        val client = mockk<TiltakspengerClient>()
        resetMock(client)
        return client
    }

    companion object {
        private val tiltakspengerPeriode =
            TiltakspengerPerioderResponse(
                periode =
                    Periode(
                        fraOgMed = LocalDate.now(),
                        tilOgMed = LocalDate.now().plusDays(1),
                    ),
            )

        fun resetMock(client: TiltakspengerClient) {
            clearMocks(client)
            every { client.hentPerioder(any(), any(), any()) } returns listOf(tiltakspengerPeriode)
        }
    }
}
