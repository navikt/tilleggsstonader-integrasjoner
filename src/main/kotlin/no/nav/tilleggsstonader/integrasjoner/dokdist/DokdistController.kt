package no.nav.tilleggsstonader.integrasjoner.dokdist

import jakarta.validation.Valid
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.tilleggsstonader.integrasjoner.dokdist.domene.DistribuerJournalpostResponseTo
import no.nav.tilleggsstonader.integrasjoner.dokdist.domene.DokdistConflictException
import no.nav.tilleggsstonader.kontrakter.dokdist.DistribuerJournalpostRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.HttpClientErrorException

@RestController
@ProtectedWithClaims(issuer = "azuread")
@RequestMapping("/api/dist")
@Validated
class DokdistController(private val dokdistService: DokdistService) {

    @ExceptionHandler(HttpClientErrorException::class)
    fun handleHttpClientException(e: HttpClientErrorException): ProblemDetail {
        secureLogger.warn("Feil ved distribusjon: ${e.message}")
        return ProblemDetail.forStatusAndDetail(e.statusCode, e.responseBodyAsString)
    }

    @ExceptionHandler(DokdistConflictException::class)
    fun handleDokdistConflictException(ex: DokdistConflictException): ResponseEntity<DistribuerJournalpostResponseTo> {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.response)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    fun distribuerJournalpost(
        @RequestBody @Valid
        request: DistribuerJournalpostRequest,
    ): String {
        return dokdistService.distribuerDokumentForJournalpost(request).bestillingsId
    }

    companion object {

        private val secureLogger = LoggerFactory.getLogger("secureLogger")
    }
}
