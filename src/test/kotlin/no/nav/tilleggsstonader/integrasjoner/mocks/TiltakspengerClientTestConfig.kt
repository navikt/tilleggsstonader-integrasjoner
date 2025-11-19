package no.nav.tilleggsstonader.integrasjoner.mocks

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.tilleggsstonader.integrasjoner.tiltakspenger.Periode
import no.nav.tilleggsstonader.integrasjoner.tiltakspenger.TiltakspengerClient
import no.nav.tilleggsstonader.integrasjoner.tiltakspenger.TiltakspengerDetaljerResponse
import no.nav.tilleggsstonader.integrasjoner.tiltakspenger.TiltakspengerPerioderResponseGammel
import no.nav.tilleggsstonader.integrasjoner.tiltakspenger.TiltakspengerPerioderResponseNy
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
        private val tiltakspengerPeriodeNyVersjon =
            TiltakspengerPerioderResponseNy(
                vedtaksperiode =
                    TiltakspengerPerioderResponseNy.PeriodeDto(
                        fraOgMed = LocalDate.now(),
                        tilOgMed = LocalDate.now().plusDays(1),
                    ),
                rettighet = TiltakspengerPerioderResponseNy.RettighetDto.TILTAKSPENGER,
                kilde = TiltakspengerPerioderResponseNy.KildeDto.ARENA,
                innvilgelsesperioder =
                    listOf(
                        TiltakspengerPerioderResponseNy.PeriodeDto(
                            fraOgMed = LocalDate.now(),
                            tilOgMed = LocalDate.now().plusDays(1),
                        ),
                    ),
                omgjortAvRammevedtakId = null,
                omgjorRammevedtakId = null,
                vedtakstidspunkt = OffsetDateTime.now(),
            )

        private val tiltakspengerPeriodeGammel =
            TiltakspengerPerioderResponseGammel(
                periode =
                    Periode(
                        fraOgMed = LocalDate.now(),
                        tilOgMed = LocalDate.now().plusDays(1),
                    ),
            )

        private val tiltakspengerDetaljer =
            TiltakspengerDetaljerResponse(
                fom = LocalDate.now(),
                tom = LocalDate.now().plusDays(1),
                rettighet = TiltakspengerDetaljerResponse.RettighetResponseJson.TILTAKSPENGER,
            )

        fun resetMock(client: TiltakspengerClient) {
            clearMocks(client)
            every { client.hentPerioderGammelVersjon(any(), any(), any()) } returns listOf(tiltakspengerPeriodeGammel)
            every { client.hentPerioderNyVersjon(any(), any(), any()) } returns listOf(tiltakspengerPeriodeNyVersjon)
            every { client.hentDetaljer(any(), any(), any()) } returns listOf(tiltakspengerDetaljer)
        }
    }
}
