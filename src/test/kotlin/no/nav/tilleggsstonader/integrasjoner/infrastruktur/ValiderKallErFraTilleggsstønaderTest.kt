package no.nav.tilleggsstonader.integrasjoner.infrastruktur

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.tilleggsstonader.integrasjoner.IntegrationTest
import no.nav.tilleggsstonader.libs.sikkerhet.EksternBrukerUtils
import no.nav.tilleggsstonader.libs.test.httpclient.ProblemDetailUtil.catchProblemDetailException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.exchange

class ValiderKallErFraTilleggsstønaderTest : IntegrationTest() {
    private val applikasjon = "dev-gcp:namespace:annen-applikasjon"

    @Test
    fun `kall mot liveness går ok`() {
        val response =
            restTemplate.exchange<String>(
                localhost("/internal/health/liveness"),
                HttpMethod.GET,
                HttpEntity(null, headers),
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Nested
    inner class IkkeEksternController {
        @Test
        fun `tokenx - skal kunne kalle på endepunkt med tilleggsstønader-app`() {
            headers.setBearerAuth(tokenX(applikasjon = "dev-gcp:tilleggsstonader:tilleggsstonader-sak"))

            val response =
                restTemplate.exchange<String>(
                    localhost("/api/test/token"),
                    HttpMethod.GET,
                    HttpEntity(null, headers),
                )

            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        }

        @Test
        fun `azuread - skal kunne kalle på endepunkt med tilleggsstønader-app`() {
            headers.setBearerAuth(onBehalfOfToken(applikasjon = "dev-gcp:tilleggsstonader:tilleggsstonader-sak"))

            val response =
                restTemplate.exchange<String>(
                    localhost("/api/test/token/azuread"),
                    HttpMethod.GET,
                    HttpEntity(null, headers),
                )

            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        }

        @Test
        fun `tokenx - skal kontrollere at kallet kommer fra en tilleggsstønader-app`() {
            headers.setBearerAuth(tokenX(applikasjon = applikasjon))

            val exception =
                catchProblemDetailException {
                    restTemplate.exchange<String>(
                        localhost("/api/test/token"),
                        HttpMethod.GET,
                        HttpEntity(null, headers),
                    )
                }

            assertThat(exception.httpStatus).isEqualTo(HttpStatus.FORBIDDEN)
        }

        @Test
        fun `azuread - skal kontrollere at kallet kommer fra en tilleggsstønader-app`() {
            headers.setBearerAuth(onBehalfOfToken(applikasjon = applikasjon))

            val exception =
                catchProblemDetailException {
                    restTemplate.exchange<String>(
                        localhost("/api/test/token/azuread"),
                        HttpMethod.GET,
                        HttpEntity(null, headers),
                    )
                }

            assertThat(exception.httpStatus).isEqualTo(HttpStatus.FORBIDDEN)
        }
    }

    @Nested
    inner class EksternController {
        @Test
        fun `tokenx - skal ikke kontrollere at kallet kommer fra en tilleggsstønader-app`() {
            headers.setBearerAuth(tokenX(applikasjon = applikasjon))

            val response =
                restTemplate.exchange<String>(
                    localhost("/api/test/token/ekstern"),
                    HttpMethod.GET,
                    HttpEntity(null, headers),
                )

            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        }

        @Test
        fun `azuread - skal ikke kontrollere at kallet kommer fra en tilleggsstønader-app`() {
            headers.setBearerAuth(onBehalfOfToken(applikasjon = applikasjon))

            val response =
                restTemplate.exchange<String>(
                    localhost("/api/test/token/ekstern/azuread"),
                    HttpMethod.GET,
                    HttpEntity(null, headers),
                )

            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        }
    }
}

@RestController
@ProtectedWithClaims(issuer = "azuread")
@RequestMapping("/api/test/token")
@Validated
class TokenTestController {
    @GetMapping
    @ProtectedWithClaims(issuer = EksternBrukerUtils.ISSUER_TOKENX, claimMap = ["acr=Level4"])
    fun tokenx(): Boolean = true

    @GetMapping("azuread")
    fun clientCredentials(): Boolean = true

    @GetMapping("ekstern")
    @ProtectedWithClaims(issuer = EksternBrukerUtils.ISSUER_TOKENX, claimMap = ["acr=Level4"])
    fun ekstern(): Boolean = true

    @GetMapping("ekstern/azuread")
    fun ekstern2(): Boolean = true
}
