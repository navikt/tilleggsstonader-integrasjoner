package no.nav.tilleggsstonader.integrasjoner.ytelse

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.verify
import no.nav.tilleggsstonader.integrasjoner.IntegrationTest
import no.nav.tilleggsstonader.integrasjoner.arbeidssokerregister.ArbeidssokerregisterClient
import no.nav.tilleggsstonader.kontrakter.felles.JsonMapperProvider.jsonMapper
import no.nav.tilleggsstonader.kontrakter.ytelse.ResultatKilde
import no.nav.tilleggsstonader.kontrakter.ytelse.TypeYtelsePeriode
import no.nav.tilleggsstonader.kontrakter.ytelse.YtelsePeriode
import no.nav.tilleggsstonader.kontrakter.ytelse.YtelsePerioderDto
import no.nav.tilleggsstonader.kontrakter.ytelse.YtelsePerioderRequest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowableOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.TestPropertySource
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.exchange
import java.time.LocalDate

@TestPropertySource(properties = ["clients.arbeidssokerregister.uri=http://localhost:28085/arbeidssokerregister"])
class ArbeidssokerperioderTest
    @Autowired
    constructor(
        private val arbeidssokerregisterClient: ArbeidssokerregisterClient,
    ) : IntegrationTest() {
        @BeforeEach
        fun setUp() {
            headers.setBearerAuth(onBehalfOfToken())
        }

        @Test
        fun `arbeidssøkerperioder returneres med lokale datoer uten id eller hendelser`() {
            stubFor(
                post(UPSTREAM_PATH).willReturn(
                    okJson(
                        """
                        [
                          {
                            "periodeId": "ubrukt-id",
                            "identitetsnummer": "$IDENT",
                            "startet": "2026-01-01T23:30:00Z",
                            "avsluttet": "2026-03-29T22:00:00Z",
                            "hendelser": [{"opplysninger": "$MARKOR"}]
                          },
                          {"startet": "2026-06-30T22:30:00Z"}
                        ]
                        """.trimIndent(),
                    ),
                ),
            )

            val dto = hentPerioder()
            assertThat(hentPerioder()).isEqualTo(dto)

            assertThat(dto.perioder).containsExactlyInAnyOrder(
                YtelsePeriode.Arbeidssøker(LocalDate.of(2026, 1, 2), LocalDate.of(2026, 3, 30)),
                YtelsePeriode.Arbeidssøker(LocalDate.of(2026, 7, 1), null),
            )
            verify(
                1,
                postRequestedFor(urlEqualTo(UPSTREAM_PATH))
                    .withHeader("Authorization", equalTo("Bearer Mock-token-response"))
                    .withRequestBody(equalToJson("""{"type":"IDENTITETSNUMMER","identitetsnummer":"$IDENT"}""")),
            )
        }

        @Test
        fun `tomme oppslag gir OK og caches`() {
            stubFor(post(UPSTREAM_PATH).willReturn(okJson("[]")))

            repeat(2) {
                val dto = hentPerioder()
                assertThat(dto.perioder).isEmpty()
                assertThat(dto.kildeResultat.single().resultat).isEqualTo(ResultatKilde.OK)
            }

            verify(1, postRequestedFor(urlEqualTo(UPSTREAM_PATH)))
        }

        @Test
        fun `endret ident eller datointervall gir nytt oppslag`() {
            stubFor(post(UPSTREAM_PATH).willReturn(okJson("[]")))

            hentPerioder()
            hentPerioder(ident = "11111122222")
            hentPerioder(fom = LocalDate.of(2026, 2, 2))
            hentPerioder(tom = LocalDate.of(2026, 3, 1))

            verify(4, postRequestedFor(urlEqualTo(UPSTREAM_PATH)))
        }

        @Test
        fun `feilet oppslag caches ikke`() {
            stubFor(post(UPSTREAM_PATH).willReturn(aResponse().withStatus(500)))
            assertThat(hentPerioder().kildeResultat.single().resultat).isEqualTo(ResultatKilde.FEILET)

            stubFor(post(UPSTREAM_PATH).willReturn(okJson("[]")))
            assertThat(hentPerioder().kildeResultat.single().resultat).isEqualTo(ResultatKilde.OK)

            verify(2, postRequestedFor(urlEqualTo(UPSTREAM_PATH)))
        }

        @Test
        fun `arbeidssøkerregisteret kalles bare når typen er valgt og duplikater hentes en gang`() {
            assertThat(hentPerioder(typer = listOf(TypeYtelsePeriode.AAP)).kildeResultat.single().type)
                .isEqualTo(TypeYtelsePeriode.AAP)
            verify(0, postRequestedFor(urlEqualTo(UPSTREAM_PATH)))

            stubFor(post(UPSTREAM_PATH).willReturn(okJson("[]")))
            val dto = hentPerioder(typer = listOf(TypeYtelsePeriode.ARBEIDSSØKER, TypeYtelsePeriode.ARBEIDSSØKER))

            assertThat(dto.kildeResultat).hasSize(1)
            verify(1, postRequestedFor(urlEqualTo(UPSTREAM_PATH)))
        }

        @ParameterizedTest
        @ValueSource(ints = [400, 401, 403, 404, 500])
        fun `kildefeil gir delresultat og FEILET`(status: Int) {
            stubFor(post(UPSTREAM_PATH).willReturn(aResponse().withStatus(status).withBody(MARKOR)))

            val dto = hentPerioder(typer = listOf(TypeYtelsePeriode.AAP, TypeYtelsePeriode.ARBEIDSSØKER))

            assertThat(dto.perioder).hasSize(1)
            assertThat(dto.perioder.single().type).isEqualTo(TypeYtelsePeriode.AAP)
            assertThat(dto.kildeResultat.map { it.type to it.resultat }).containsExactlyInAnyOrder(
                TypeYtelsePeriode.AAP to ResultatKilde.OK,
                TypeYtelsePeriode.ARBEIDSSØKER to ResultatKilde.FEILET,
            )
            assertThat(jsonMapper.writeValueAsString(dto)).doesNotContain(IDENT, MARKOR)
        }

        @Test
        fun `klienten propagerer opprinnelig RestClient-feil`() {
            stubFor(post(UPSTREAM_PATH).willReturn(aResponse().withStatus(403).withBody(MARKOR)))

            val error =
                catchThrowableOfType(RestClientResponseException::class.java) {
                    arbeidssokerregisterClient.hentPerioder(IDENT)
                }

            assertThat(error.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
            assertThat(error.responseBodyAsString).isEqualTo(MARKOR)
        }

        @ParameterizedTest
        @ValueSource(strings = ["", "null", """[{"startet":"SYNTHETIC"}]""", """{"SYNTHETIC":"""])
        fun `ugyldig upstream respons gir FEILET i stedet for tomt vellykket resultat`(body: String) {
            stubFor(post(UPSTREAM_PATH).willReturn(okJson(body)))

            val dto = hentPerioder()

            assertThat(dto.perioder).isEmpty()
            assertThat(dto.kildeResultat.single().resultat).isEqualTo(ResultatKilde.FEILET)
            assertThat(jsonMapper.writeValueAsString(dto)).doesNotContain(IDENT, MARKOR)
        }

        @Test
        fun `avvist ident gir kildefeil`() {
            stubFor(post(UPSTREAM_PATH).willReturn(aResponse().withStatus(400).withBody(MARKOR)))

            val dto = hentPerioder(ident = "SYNTHETIC")

            assertThat(dto.kildeResultat.single().resultat).isEqualTo(ResultatKilde.FEILET)
            verify(1, postRequestedFor(urlEqualTo(UPSTREAM_PATH)))
            assertThat(jsonMapper.writeValueAsString(dto)).doesNotContain(IDENT, MARKOR)
        }

        private fun hentPerioder(
            ident: String = IDENT,
            typer: List<TypeYtelsePeriode> = listOf(TypeYtelsePeriode.ARBEIDSSØKER),
            fom: LocalDate = LocalDate.of(2026, 2, 1),
            tom: LocalDate = LocalDate.of(2026, 2, 28),
        ): YtelsePerioderDto =
            restTemplate
                .exchange<YtelsePerioderDto>(
                    localhost("/api/ytelse/finn"),
                    HttpMethod.POST,
                    HttpEntity(
                        YtelsePerioderRequest(ident, fom, tom, typer),
                        headers,
                    ),
                ).body ?: error("Forventet ytelsesrespons")

        companion object {
            private const val UPSTREAM_PATH = "/arbeidssokerregister/api/v3/perioder"
            private const val IDENT = "12345678910"
            private const val MARKOR = "SYNTHETIC"
        }
    }
