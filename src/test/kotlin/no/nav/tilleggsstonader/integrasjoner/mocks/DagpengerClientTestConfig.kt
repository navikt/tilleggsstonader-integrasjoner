package no.nav.tilleggsstonader.integrasjoner.mocks

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.tilleggsstonader.integrasjoner.dagpenger.DagpengerClient
import no.nav.tilleggsstonader.integrasjoner.dagpenger.DagpengerPerioderResponse
import no.nav.tilleggsstonader.integrasjoner.dagpenger.DagpengerYtelseType
import no.nav.tilleggsstonader.integrasjoner.dagpenger.Periode
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import java.time.LocalDate

@Configuration
@Profile("mock-dagpenger")
class DagpengerClientTestConfig {
    @Bean
    @Primary
    fun dagpengerClient(): DagpengerClient {
        val client = mockk<DagpengerClient>()
        resetMock(client)
        return client
    }

    companion object {
        private val dagpengerPeriode =
            DagpengerPerioderResponse(
                personIdent = "12345678910",
                perioder =
                    listOf(
                        Periode(
                            fraOgMedDato = LocalDate.now(),
                            tilOgMedDato = LocalDate.now().plusDays(1),
                            ytelseType = DagpengerYtelseType.DAGPENGER_ARBEIDSSOKER_ORDINAER,
                        ),
                    ),
            )

        fun resetMock(client: DagpengerClient) {
            clearMocks(client)
            every { client.hentPerioder(any(), any(), any()) } returns dagpengerPeriode
        }
    }
}
