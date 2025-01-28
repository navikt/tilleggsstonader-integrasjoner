package no.nav.tilleggsstonader.integrasjoner.fullmakt

import com.github.tomakehurst.wiremock.client.WireMock.badRequest
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.serverError
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import no.nav.tilleggsstonader.integrasjoner.IntegrationTest
import no.nav.tilleggsstonader.integrasjoner.util.FileUtil
import no.nav.tilleggsstonader.integrasjoner.util.ProblemDetailUtil.catchProblemDetailException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.TestPropertySource
import org.springframework.web.client.exchange

@TestPropertySource(properties = ["clients.repr-api.uri=http://localhost:28086"])
@AutoConfigureWireMock(port = 28086)
class FullmaktControllerTest : IntegrationTest() {
    private val fullmaktsgiverIdent = "12345678910"
    private val fullmektigIdent = "30515505985"

    @BeforeEach
    fun setUp() {
        headers.setBearerAuth(onBehalfOfToken())
        headers.set("Content-Type", "application/json")
    }

    @Test
    fun `data mappes når respons er OK`() {
        stubResponse(HttpStatus.OK)
        val response = kallFullmektige()
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).contains(fullmektigIdent)
    }

    @Test
    fun `skal svare med problem detail i tilfelle klientfeil`() {
        stubResponse(HttpStatus.BAD_REQUEST)
        val response =
            catchProblemDetailException {
                kallFullmektige()
            }
        assertThat(response.httpStatus).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(response.detail.detail).contains("Kunne ikke hente ut fullmakter fra REPR")
    }

    @Test
    fun `skal svare med problem detail i tilfelle serverfeil`() {
        stubResponse(HttpStatus.INTERNAL_SERVER_ERROR)
        val response =
            catchProblemDetailException {
                kallFullmektige()
            }
        assertThat(response.httpStatus).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        assertThat(response.detail.detail).contains("Kunne ikke hente ut fullmakter fra REPR")
    }

    private fun kallFullmektige(): ResponseEntity<String> {
        val identRequestJson =
            """
            {
                "ident": "$fullmaktsgiverIdent"
            }
            """.trimIndent()

        return restTemplate.exchange<String>(
            localhost("/api/fullmakt/fullmektige"),
            HttpMethod.POST,
            HttpEntity(identRequestJson, headers),
        )
    }
}

private fun stubResponse(responseType: HttpStatus) {
    val response =
        when (responseType) {
            HttpStatus.OK -> okJson(FullmaktResponseStubs.ok)
            HttpStatus.INTERNAL_SERVER_ERROR -> serverError().withBody(FullmaktResponseStubs.internalServerError)
            HttpStatus.BAD_REQUEST -> badRequest().withBody(FullmaktResponseStubs.badRequest)
            else -> throw NotImplementedError("Har ikke laget testrespons for $responseType")
        }

    stubFor(
        post("/api/internbruker/fullmakt/fullmaktsgiver")
            .willReturn(
                response.withHeader("Content-Type", "application/json"),
            ),
    )
}

private object FullmaktResponseStubs {
    val ok: String = FileUtil.readFile("fullmakt/ok.json")
    val internalServerError: String = FileUtil.readFile("fullmakt/internal-server-error.json")
    val badRequest: String = FileUtil.readFile("fullmakt/bad-request.json")
}
