package no.nav.tilleggsstonader.integrasjoner.dokarkiv.client

import no.nav.tilleggsstonader.integrasjoner.dokarkiv.client.domene.FerdigstillJournalPost
import no.nav.tilleggsstonader.integrasjoner.dokarkiv.client.domene.OpprettJournalpostRequest
import no.nav.tilleggsstonader.integrasjoner.dokarkiv.client.domene.OpprettJournalpostResponse
import no.nav.tilleggsstonader.integrasjoner.infrastruktur.exception.OppslagException
import no.nav.tilleggsstonader.integrasjoner.util.MDCOperations
import no.nav.tilleggsstonader.kontrakter.dokarkiv.ArkiverDokumentResponse
import no.nav.tilleggsstonader.kontrakter.dokarkiv.OppdaterJournalpostRequest
import no.nav.tilleggsstonader.kontrakter.dokarkiv.OppdaterJournalpostResponse
import no.nav.tilleggsstonader.kontrakter.felles.JsonMapperProvider.jsonMapper
import no.nav.tilleggsstonader.libs.http.client.ProblemDetailException
import no.nav.tilleggsstonader.libs.http.client.patchForEntity
import no.nav.tilleggsstonader.libs.http.client.postForEntity
import no.nav.tilleggsstonader.libs.http.client.putForEntity
import no.nav.tilleggsstonader.libs.log.NavHttpHeaders
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import tools.jackson.module.kotlin.readValue
import java.net.URI

@Component
class DokarkivRestClient(
    @Value("\${clients.dokarkiv.uri}") private val dokarkivUrl: URI,
    @Qualifier("azure") private val restTemplate: RestTemplate,
) {
    fun lagJournalpostUri(ferdigstill: Boolean): String =
        UriComponentsBuilder
            .fromUri(dokarkivUrl)
            .path(PATH_JOURNALPOST)
            .query(QUERY_FERDIGSTILL)
            .encode()
            .toUriString()

    fun lagJournalpost(
        request: OpprettJournalpostRequest,
        ferdigstill: Boolean,
        navIdent: String? = null,
    ): OpprettJournalpostResponse {
        val uri = lagJournalpostUri(ferdigstill)
        try {
            return restTemplate.postForEntity(uri, request, headers(navIdent), mapOf("ferdigstill" to ferdigstill))
        } catch (e: RuntimeException) {
            if (e is HttpClientErrorException.Conflict) {
                håndterConflict(e)
            }
            throw oppslagExceptionVed("opprettelse", e, request.bruker?.id)
        }
    }

    private fun håndterConflict(e: HttpClientErrorException.Conflict) {
        var response: ArkiverDokumentResponse? = null
        try {
            response = jsonMapper.readValue<ArkiverDokumentResponse>(e.responseBodyAsString)
        } catch (ex: Exception) {
            throw OppslagException(
                "Klarer ikke å parse response fra dokarkiv ved response 409. Mest sannsynnlig er journalposten allerede opprettet. Verifiser dette med en saksbehandler",
                "Dokarkiv",
                OppslagException.Level.KRITISK,
                HttpStatus.INTERNAL_SERVER_ERROR,
                e,
                sensitiveInfo = "Body=${e.responseBodyAsString}",
            )
        }
        throw DokarkivConflictException(response)
    }

    fun oppdaterJournalpost(
        request: OppdaterJournalpostRequest,
        journalpostId: String,
        navIdent: String? = null,
    ): OppdaterJournalpostResponse {
        val uri =
            UriComponentsBuilder
                .fromUri(dokarkivUrl)
                .path(PATH_JOURNALPOST)
                .pathSegment("{journalpostId}")
                .encode()
                .toUriString()
        try {
            return restTemplate.putForEntity(uri, request, headers(navIdent), mapOf("journalpostId" to journalpostId))
        } catch (e: RuntimeException) {
            throw oppslagExceptionVed("oppdatering", e, request.bruker?.id)
        }
    }

    private fun oppslagExceptionVed(
        requestType: String,
        e: RuntimeException,
        brukerId: String?,
    ): Throwable {
        val message = "Feil ved $requestType av journalpost "
        val sensitiveInfo =
            if (e is HttpStatusCodeException) e.responseBodyAsString else "$message for bruker $brukerId "
        val httpStatus = if (e is HttpStatusCodeException) e.statusCode else HttpStatus.INTERNAL_SERVER_ERROR
        return OppslagException(
            message,
            "Dokarkiv",
            OppslagException.Level.MEDIUM,
            httpStatus,
            e,
            sensitiveInfo,
        )
    }

    fun ferdigstillJournalpost(
        journalpostId: String,
        journalførendeEnhet: String,
        navIdent: String?,
    ) {
        val uri =
            UriComponentsBuilder
                .fromUri(dokarkivUrl)
                .path(PATH_JOURNALPOST)
                .pathSegment("{journalpostId}", "ferdigstill")
                .encode()
                .toUriString()
        try {
            restTemplate.patchForEntity<String>(
                uri,
                FerdigstillJournalPost(journalførendeEnhet),
                headers(navIdent),
                mapOf("journalpostId" to journalpostId),
            )
        } catch (e: ProblemDetailException) {
            if (e.httpStatus == HttpStatus.BAD_REQUEST) {
                throw KanIkkeFerdigstilleJournalpostException(
                    e.detail.detail ?: "Ukjent feil ved ferdigstilling av journalpost",
                )
            }
            throw e
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DokarkivRestClient::class.java)

        private const val PATH_JOURNALPOST = "rest/journalpostapi/v1/journalpost"
        private const val QUERY_FERDIGSTILL = "forsoekFerdigstill={ferdigstill}"
        private const val NAV_CALL_ID = "Nav-Callid"

        private val NAVIDENT_REGEX = """^[a-zA-Z]\d{6}$""".toRegex()

        fun headers(navIdent: String?): HttpHeaders =
            HttpHeaders().apply {
                add(NAV_CALL_ID, MDCOperations.getCallId())
                if (!navIdent.isNullOrEmpty()) {
                    if (NAVIDENT_REGEX.matches(navIdent)) {
                        add(NavHttpHeaders.NAV_USER_ID.asString(), navIdent)
                    } else {
                        logger.info("Sender ikke med navIdent navIdent=$navIdent")
                    }
                }
            }
    }
}
