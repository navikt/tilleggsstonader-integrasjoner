package no.nav.tilleggsstonader.integrasjoner.fullmakt

import com.github.tomakehurst.wiremock.client.WireMock.badRequest
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.serverError
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import no.nav.tilleggsstonader.integrasjoner.IntegrationTest
import no.nav.tilleggsstonader.integrasjoner.util.ProblemDetailUtil.catchProblemDetailException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.TestPropertySource
import org.springframework.web.client.exchange
import java.nio.charset.StandardCharsets
import java.nio.file.Files

private const val FULLMAKT_URI = "/api/fullmakt/fullmektige"

@TestPropertySource(properties = ["clients.pdl-fullmakt.uri=http://localhost:28086"])
@AutoConfigureWireMock(port = 28086)
class FullmaktControllerTest : IntegrationTest() {

    private val dummyIdent = "12345678910"
    private val identRequestJson = """
        {
            "ident": "$dummyIdent"
        }
    """.trimIndent()

    @BeforeEach
    fun setUp() {
        headers.setBearerAuth(onBehalfOfToken())
        headers.set("Content-Type", "application/json")
    }

    @Test
    fun `data mappes når respons er OK`() {
        stubFor(
            post("/api/internbruker/fullmaktsgiver")
                .willReturn(
                    okJson(fullmaktResponses.ok)
                        .withHeader("Content-Type", "application/json")
                ),
        )
        val response: ResponseEntity<String> = restTemplate.exchange(
            localhost(FULLMAKT_URI),
            HttpMethod.POST,
            HttpEntity(identRequestJson, headers),
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).contains(dummyIdent)
    }

    @Test
    fun `skal svare med problem detail i tilfelle klientfeil`() {
        stubBadRequest()
        val response = catchProblemDetailException {
            postTilFullmektige()
        }
        assertThat(response.httpStatus).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(response.detail.detail).isEqualTo(fullmaktResponses.badRequest)
    }

    @Test
    fun `skal svare med problem detail i tilfelle serverfeil`() {
        stubInternalServerError()
        val response = catchProblemDetailException {
            postTilFullmektige()
        }
        assertThat(response.httpStatus).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        assertThat(response.detail.detail).isEqualTo(fullmaktResponses.internalServerError)
    }

    private fun postTilFullmektige() {
        restTemplate.exchange<String>(
            localhost(FULLMAKT_URI),
            HttpMethod.POST,
            HttpEntity(identRequestJson, headers),
        )
    }
}

fun stubInternalServerError() {
    stubFor(
        post("/api/internbruker/fullmaktsgiver")
            .willReturn(
                serverError()
                    .withHeader("Content-Type", "application/json")
                    .withBody(fullmaktResponses.internalServerError)
            ),
    )
}

fun stubBadRequest() {
    stubFor(
        post("/api/internbruker/fullmaktsgiver")
            .willReturn(
                badRequest()
                    .withHeader("Content-Type", "application/json")
                    .withBody(fullmaktResponses.badRequest)
            ),
    )
}

private object fullmaktResponses {
    val ok: String = readFile("fullmakt/ok.json")
    val internalServerError: String = readFile("fullmakt/internal-server-error.json")
    val badRequest: String = readFile("fullmakt/bad-request.json")
}

private fun readFile(filePath: String) =
    Files.readString(ClassPathResource(filePath).file.toPath(), StandardCharsets.UTF_8)