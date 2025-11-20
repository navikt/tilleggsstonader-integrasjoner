package no.nav.tilleggsstonader.integrasjoner.mocks

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.tilleggsstonader.integrasjoner.tiltakspenger.TiltakspengerClient
import no.nav.tilleggsstonader.integrasjoner.tiltakspenger.TiltakspengerDetaljerResponse
import no.nav.tilleggsstonader.integrasjoner.tiltakspenger.TiltakspengerPerioderResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import java.time.LocalDate
import java.time.OffsetDateTime

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
        private val tiltakspengerPeriodeResponse =
            TiltakspengerPerioderResponse(
                vedtaksperiode =
                    TiltakspengerPerioderResponse.PeriodeDto(
                        fraOgMed = LocalDate.now(),
                        tilOgMed = LocalDate.now().plusDays(1),
                    ),
                rettighet = TiltakspengerPerioderResponse.RettighetDto.TILTAKSPENGER,
                kilde = TiltakspengerPerioderResponse.KildeDto.ARENA,
                innvilgelsesperioder =
                    listOf(
                        TiltakspengerPerioderResponse.PeriodeDto(
                            fraOgMed = LocalDate.now(),
                            tilOgMed = LocalDate.now().plusDays(1),
                        ),
                    ),
                omgjortAvRammevedtakId = null,
                omgjorRammevedtakId = null,
                vedtakstidspunkt = OffsetDateTime.now(),
            )

        private val tiltakspengerDetaljerResponse =
            TiltakspengerDetaljerResponse(
                fom = LocalDate.now(),
                tom = LocalDate.now().plusDays(1),
                rettighet = TiltakspengerDetaljerResponse.RettighetResponseJson.TILTAKSPENGER,
            )

        fun resetMock(client: TiltakspengerClient) {
            clearMocks(client)
            every { client.hentPerioder(any(), any(), any()) } returns listOf(tiltakspengerPeriodeResponse)
            every { client.hentDetaljer(any(), any(), any()) } returns listOf(tiltakspengerDetaljerResponse)
        }
    }
}
