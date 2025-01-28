package no.nav.tilleggsstonader.integrasjoner.journalpost

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.tilleggsstonader.kontrakter.journalpost.Journalpost
import no.nav.tilleggsstonader.kontrakter.journalpost.JournalposterForBrukerRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.HttpStatusCodeException

@RestController
@RequestMapping("/api/journalpost")
@ProtectedWithClaims(issuer = "azuread")
class HentJournalpostController(
    private val journalpostService: JournalpostService,
) {
    @ExceptionHandler(JournalpostRestClientException::class)
    fun handleRestClientException(ex: JournalpostRestClientException): ProblemDetail {
        val errorBaseMessage = "Feil ved henting av journalpost=${ex.journalpostId}"
        val errorExtMessage = byggFeilmelding(ex)
        LOG.warn(errorBaseMessage, ex)
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, errorBaseMessage + errorExtMessage)
    }

    @ExceptionHandler(JournalpostRequestException::class)
    fun handleJournalpostForBrukerException(ex: JournalpostRequestException): ProblemDetail {
        val errorBaseMessage = "Feil ved henting av journalpost for ${ex.safJournalpostRequest}"
        val errorExtMessage = byggFeilmelding(ex)
        secureLogger.warn(errorBaseMessage, ex)
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, errorBaseMessage + errorExtMessage)
    }

    @ExceptionHandler(JournalpostForbiddenException::class)
    fun handleJournalpostForbiddenException(e: JournalpostForbiddenException): ProblemDetail {
        LOG.warn("Bruker eller system ikke tilgang til saf ressurs: ${e.message}")

        return ProblemDetail.forStatusAndDetail(
            HttpStatus.FORBIDDEN,
            e.message ?: "Bruker eller system har ikke tilgang til saf ressurs",
        )
    }

    @ExceptionHandler(JournalpostIkkeFunnetException::class)
    fun handleJournalpostForbiddenException(e: JournalpostIkkeFunnetException): ProblemDetail {
        LOG.warn("Journalpost=${e.id} ikke funnet")

        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Sak mangler for journalpostId=${e.id}")
    }

    private fun byggFeilmelding(ex: RuntimeException): String =
        if (ex.cause is HttpStatusCodeException) {
            val cex = ex.cause as HttpStatusCodeException
            " statuscode=${cex.statusCode} body=${cex.responseBodyAsString}"
        } else {
            " klientfeilmelding=${ex.message}"
        }

    @ExceptionHandler(RuntimeException::class)
    fun handleRequestParserException(ex: RuntimeException): ProblemDetail {
        val errorMessage = "Feil ved henting av journalpost. ${ex.message}"
        LOG.warn(errorMessage, ex)
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage)
    }

    @GetMapping("sak")
    fun hentSaksnummer(
        @RequestParam(name = "journalpostId") journalpostId: String,
    ): Map<String, String> {
        val saksnummer =
            journalpostService.hentSaksnummer(journalpostId)
                ?: throw JournalpostIkkeFunnetException(journalpostId)

        return mapOf("saksnummer" to saksnummer)
    }

    @GetMapping
    fun hentJournalpost(
        @RequestParam(name = "journalpostId") journalpostId: String,
    ): Journalpost = journalpostService.hentJournalpost(journalpostId)

    @PostMapping
    fun hentJournalpostForBruker(
        @RequestBody journalposterForBrukerRequest: JournalposterForBrukerRequest,
    ): List<Journalpost> = journalpostService.finnJournalposter(journalposterForBrukerRequest)

    @GetMapping("hentdokument/{journalpostId}/{dokumentInfoId}")
    fun hentDokument(
        @PathVariable journalpostId: String,
        @PathVariable dokumentInfoId: String,
        @RequestParam("variantFormat", required = false) variantFormat: String?,
    ): ByteArray = journalpostService.hentDokument(journalpostId, dokumentInfoId, variantFormat ?: "ARKIV")

    companion object {
        private val LOG = LoggerFactory.getLogger(HentJournalpostController::class.java)
        private val secureLogger = LoggerFactory.getLogger("secureLogger")
    }
}
