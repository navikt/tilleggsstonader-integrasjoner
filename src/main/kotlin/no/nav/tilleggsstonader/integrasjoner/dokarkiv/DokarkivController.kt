package no.nav.tilleggsstonader.integrasjoner.dokarkiv

import jakarta.validation.Valid
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.tilleggsstonader.integrasjoner.dokarkiv.client.DokarkivConflictException
import no.nav.tilleggsstonader.integrasjoner.dokarkiv.client.KanIkkeFerdigstilleJournalpostException
import no.nav.tilleggsstonader.kontrakter.dokarkiv.ArkiverDokumentRequest
import no.nav.tilleggsstonader.kontrakter.dokarkiv.ArkiverDokumentResponse
import no.nav.tilleggsstonader.kontrakter.dokarkiv.LogiskVedleggRequest
import no.nav.tilleggsstonader.kontrakter.dokarkiv.LogiskVedleggResponse
import no.nav.tilleggsstonader.kontrakter.dokarkiv.OppdaterJournalpostRequest
import no.nav.tilleggsstonader.kontrakter.dokarkiv.OppdaterJournalpostResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.function.Consumer

@RestController
@ProtectedWithClaims(issuer = "azuread")
@RequestMapping("/api/arkiv")
@Validated
class DokarkivController(private val journalføringService: DokarkivService) {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ProblemDetail {
        val errors: MutableMap<String, String> = HashMap()
        ex.bindingResult.allErrors.forEach(
            Consumer { error: ObjectError ->
                val fieldName = (error as FieldError).field
                val errorMessage = error.getDefaultMessage() ?: ""
                errors[fieldName] = errorMessage
            },
        )
        LOG.warn("Valideringsfeil av input ved arkivering: $errors")
        return ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "Valideringsfeil av input ved arkivering $errors",
        )
    }

    @ExceptionHandler(KanIkkeFerdigstilleJournalpostException::class)
    fun handleKanIkkeFerdigstilleException(ex: KanIkkeFerdigstilleJournalpostException): ProblemDetail {
        LOG.warn("Feil ved ferdigstilling {}", ex.message)
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.message ?: "Uventer feil")
    }

    @ExceptionHandler(DokarkivConflictException::class)
    fun handleDokarkivConflictException(ex: DokarkivConflictException): ResponseEntity<ArkiverDokumentResponse> {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.response)
    }

    @PostMapping
    fun arkiverDokument(
        @RequestBody @Valid
        arkiverDokumentRequest: ArkiverDokumentRequest,
        @RequestHeader(name = NAV_USER_ID) navIdent: String? = null,
    ): ResponseEntity<ArkiverDokumentResponse> {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(journalføringService.lagJournalpost(arkiverDokumentRequest, navIdent))
    }

    @PutMapping("{journalpostId}")
    fun oppdaterJournalpost(
        @PathVariable(name = "journalpostId") journalpostId: String,
        @RequestHeader(name = NAV_USER_ID) navIdent: String? = null,
        @RequestBody @Valid
        oppdaterJournalpostRequest: OppdaterJournalpostRequest,
    ): OppdaterJournalpostResponse {
        return journalføringService.oppdaterJournalpost(oppdaterJournalpostRequest, journalpostId, navIdent)
    }

    @PutMapping("{journalpostId}/ferdigstill")
    @ResponseStatus(HttpStatus.OK)
    fun ferdigstillJournalpost(
        @PathVariable(name = "journalpostId") journalpostId: String,
        @RequestParam(name = "journalfoerendeEnhet") journalførendeEnhet: String,
        @RequestHeader(name = NAV_USER_ID) navIdent: String? = null,
    ): Map<String, String> {
        journalføringService.ferdistillJournalpost(journalpostId, journalførendeEnhet, navIdent)
        return mapOf("journalpostId" to journalpostId)
    }

    @PostMapping(path = ["/dokument/{dokumentinfoId}/logiskVedlegg"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun leggTilLogiskVedlegg(
        @PathVariable(name = "dokumentinfoId") dokumentinfoId: String,
        @RequestBody @Valid
        request: LogiskVedleggRequest,
    ): ResponseEntity<LogiskVedleggResponse> {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(journalføringService.lagNyttLogiskVedlegg(dokumentinfoId, request))
    }

    @DeleteMapping(path = ["/dokument/{dokumentinfoId}/logiskVedlegg/{logiskVedleggId}"])
    fun slettLogiskVedlegg(
        @PathVariable(name = "dokumentinfoId") dokumentinfoId: String,
        @PathVariable(name = "logiskVedleggId") logiskVedleggId: String,
    ): LogiskVedleggResponse {
        journalføringService.slettLogiskVedlegg(dokumentinfoId, logiskVedleggId)
        return LogiskVedleggResponse(logiskVedleggId.toLong())
    }

    companion object {

        private val LOG = LoggerFactory.getLogger(DokarkivController::class.java)
        const val ARKIVERT_OK_MELDING = "Arkivert journalpost OK"
        const val NAV_USER_ID = "Nav-User-Id"
    }
}
