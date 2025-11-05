package no.nav.tilleggsstonader.integrasjoner.aktiviteter

import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlMatching
import no.nav.tilleggsstonader.integrasjoner.IntegrationTest
import no.nav.tilleggsstonader.integrasjoner.arena.ArenaAktivitetUtil.aktivitetArenaResponse
import no.nav.tilleggsstonader.integrasjoner.arena.PeriodeArena
import no.nav.tilleggsstonader.kontrakter.aktivitet.AktivitetArenaDto
import no.nav.tilleggsstonader.kontrakter.aktivitet.TypeAktivitet
import no.nav.tilleggsstonader.kontrakter.felles.ObjectMapperProvider.objectMapper
import no.nav.tilleggsstonader.kontrakter.felles.Stønadstype
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.test.context.TestPropertySource
import org.springframework.web.client.exchange
import java.time.LocalDate

@TestPropertySource(properties = ["clients.arena.uri=http://localhost:28085"])
@AutoConfigureWireMock(port = 28085)
class EksternAktivitetControllerTest : IntegrationTest() {
    @BeforeEach
    fun setUp() {
        headers.setBearerAuth(tokenX(applikasjon = "dev-gcp:skjemadigitalisering:skjemautfylling"))
    }

    @Test
    fun `skal hente aktiviteter til skjemabyggeren`() {
        stubAktiviteter(
            objectMapper.writeValueAsString(
                listOf(
                    aktivitetArenaResponse(),
                    aktivitetArenaResponse(erStoenadsberettigetAktivitet = false, aktivitetstype = TypeAktivitet.AAPLOK.name),
                ),
            ),
        )

        val entity = HttpEntity(null, headers)
        val url = localhost("/api/ekstern/aktivitet?stønadstype=${Stønadstype.BOUTGIFTER}")
        val response = restTemplate.exchange<List<AktivitetSøknadDto>>(url, HttpMethod.GET, entity)

        assertThat(response.body!!).containsExactly(
            AktivitetSøknadDto(id = "1", "aktivitetnavn: 01. januar 2023 - 31. januar 2023", AktivitetSøknadType.TILTAK),
        )
    }

    fun stubAktiviteter(responseJson: String) {
        val response = okJson(responseJson)
        stubFor(get(urlMatching("/api/v1/tilleggsstoenad/aktiviteter.*")).willReturn(response))
    }

    @Test
    fun `skal hente aktiviteter uten fom og tom dato`() {
        val aktivitetsperiode =
            aktivitetArenaResponse(
                periode = PeriodeArena(fom = null, tom = null),
            )

        stubAktiviteter(
            objectMapper.writeValueAsString(listOf(aktivitetsperiode)),
        )

        val entity = HttpEntity(null, headers)
        val url = localhost("/api/ekstern/aktivitet?stønadstype=${Stønadstype.BOUTGIFTER}")
        val response = restTemplate.exchange<List<AktivitetSøknadDto>>(url, HttpMethod.GET, entity)

        assertThat(response.body!!).containsExactly(
            AktivitetSøknadDto(
                id = "1",
                tekst = "aktivitetnavn",
                type = AktivitetSøknadType.TILTAK,
            ),
        )
    }

    @Test
    fun `skal hente aktiviteter med fom dato men uten tom dato`() {
        val aktivitetsperiode =
            aktivitetArenaResponse(
                periode = PeriodeArena(fom = LocalDate.of(2025, 1, 1), tom = null),
            )

        stubAktiviteter(
            objectMapper.writeValueAsString(listOf(aktivitetsperiode)),
        )

        val entity = HttpEntity(null, headers)
        val url = localhost("/api/ekstern/aktivitet?stønadstype=${Stønadstype.BOUTGIFTER}")
        val response = restTemplate.exchange<List<AktivitetSøknadDto>>(url, HttpMethod.GET, entity)

        assertThat(response.body!!).containsExactly(
            AktivitetSøknadDto(
                id = "1",
                tekst = "aktivitetnavn: 01. januar 2025 - ukjent sluttdato",
                type = AktivitetSøknadType.TILTAK,
            ),
        )
    }

    @Test
    fun `skal hente aktiviteter uten fom dato men med tom dato`() {
        val aktivitetsperiode =
            aktivitetArenaResponse(
                periode = PeriodeArena(fom = null, tom = LocalDate.of(2025, 1, 31)),
            )

        stubAktiviteter(
            objectMapper.writeValueAsString(listOf(aktivitetsperiode)),
        )

        val entity = HttpEntity(null, headers)
        val url = localhost("/api/ekstern/aktivitet?stønadstype=${Stønadstype.BOUTGIFTER}")
        val response = restTemplate.exchange<List<AktivitetSøknadDto>>(url, HttpMethod.GET, entity)

        assertThat(response.body!!).containsExactly(
            AktivitetSøknadDto(
                id = "1",
                tekst = "aktivitetnavn",
                type = AktivitetSøknadType.TILTAK,
            ),
        )
    }
}
