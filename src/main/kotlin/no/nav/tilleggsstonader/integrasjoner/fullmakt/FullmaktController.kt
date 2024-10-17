package no.nav.tilleggsstonader.integrasjoner.fullmakt

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.tilleggsstonader.kontrakter.felles.IdentRequest
import no.nav.tilleggsstonader.kontrakter.fullmakt.FullmektigDto
import org.slf4j.LoggerFactory
import org.springframework.http.ProblemDetail
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestClientResponseException

@RestController
@ProtectedWithClaims(issuer = "azuread")
@RequestMapping("/api/fullmakt")
@Validated
class FullmaktController(
    val fullmaktClient: FullmaktClient,
) {
    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    @PostMapping("fullmektige")
    fun hentFullmektige(@RequestBody fullmaktsgiver: IdentRequest): List<FullmektigDto> {
        return fullmaktClient.hentFullmektige(fullmaktsgiver)
    }

    @ExceptionHandler(RestClientResponseException::class)
    fun handleRestClientResponseException(e: RestClientResponseException): ProblemDetail {
        secureLogger.warn("Feil ved kall mot pfl-fullmakt: ${e.message}")
        return ProblemDetail.forStatusAndDetail(e.statusCode, e.responseBodyAsString)
    }
}