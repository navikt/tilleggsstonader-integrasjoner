package no.nav.tilleggsstonader.integrasjoner.aktiviteter

import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlMatching
import no.nav.tilleggsstonader.integrasjoner.IntegrationTest
import no.nav.tilleggsstonader.integrasjoner.arena.ArenaAktivitetUtil.aktivitetArenaResponse
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
            AktivitetSøknadDto(id = "1", "aktivitetnavn: 01. januar 2023 - 31. januar 2023"),
        )
    }

    fun stubAktiviteter(responseJson: String) {
        val response = okJson(responseJson)
        stubFor(get(urlMatching("/api/v1/tilleggsstoenad/aktiviteter.*")).willReturn(response))
    }
}
