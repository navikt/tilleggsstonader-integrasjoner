package no.nav.tilleggsstonader.integrasjoner.journalpost.client

import no.nav.tilleggsstonader.integrasjoner.journalpost.JournalpostForbiddenException
import no.nav.tilleggsstonader.integrasjoner.journalpost.JournalpostRequestException
import no.nav.tilleggsstonader.integrasjoner.journalpost.JournalpostRestClientException
import no.nav.tilleggsstonader.integrasjoner.journalpost.internal.SafErrorCode
import no.nav.tilleggsstonader.integrasjoner.journalpost.internal.SafFagsakInput
import no.nav.tilleggsstonader.integrasjoner.journalpost.internal.SafFagsakVariabler
import no.nav.tilleggsstonader.integrasjoner.journalpost.internal.SafJournalpostBrukerData
import no.nav.tilleggsstonader.integrasjoner.journalpost.internal.SafJournalpostData
import no.nav.tilleggsstonader.integrasjoner.journalpost.internal.SafJournalpostFagsakData
import no.nav.tilleggsstonader.integrasjoner.journalpost.internal.SafJournalpostRequest
import no.nav.tilleggsstonader.integrasjoner.journalpost.internal.SafJournalpostResponse
import no.nav.tilleggsstonader.integrasjoner.journalpost.internal.SafRequestVariabler
import no.nav.tilleggsstonader.integrasjoner.util.MDCOperations
import no.nav.tilleggsstonader.integrasjoner.util.graphqlQuery
import no.nav.tilleggsstonader.kontrakter.felles.Fagsystem
import no.nav.tilleggsstonader.kontrakter.journalpost.Journalpost
import no.nav.tilleggsstonader.kontrakter.journalpost.JournalposterForBrukerRequest
import no.nav.tilleggsstonader.kontrakter.journalpost.Journalposttype
import no.nav.tilleggsstonader.libs.http.client.postForEntity
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Service
class SafClient(
    @Value("\${clients.saf.uri}") safBaseUrl: URI,
    @Qualifier("azure") private val restTemplate: RestTemplate,
) {
    private val safUri = UriComponentsBuilder.fromUri(safBaseUrl).pathSegment(PATH_GRAPHQL).toUriString()

    fun hentJournalpost(journalpostId: String): Journalpost {
        val safJournalpostRequest =
            SafJournalpostRequest(
                SafRequestVariabler(journalpostId),
                graphqlQuery("/saf/journalpostForId.graphql"),
            )
        val response =
            restTemplate.postForEntity<SafJournalpostResponse<SafJournalpostData>>(
                safUri,
                safJournalpostRequest,
                httpHeaders(),
            )
        if (!response.harFeil()) {
            return response.data?.journalpost ?: throw JournalpostRestClientException(
                "Kan ikke hente journalpost",
                null,
                journalpostId,
            )
        } else {
            val tilgangFeil = response.errors?.firstOrNull { it.extensions.code == SafErrorCode.forbidden }

            if (tilgangFeil != null) {
                throw JournalpostForbiddenException(tilgangFeil.message)
            } else {
                throw JournalpostRestClientException(
                    "Kan ikke hente journalpost " + response.errors?.toString(),
                    null,
                    journalpostId,
                )
            }
        }
    }

    fun finnJournalposterForFagsak(
        fagsakId: String,
        journalposttyper: List<Journalposttype> = emptyList(),
    ): List<Journalpost> {
        val safJournalpostRequest =
            SafJournalpostRequest(
                variables =
                    SafFagsakVariabler(
                        fagsak =
                            SafFagsakInput(
                                fagsakId = fagsakId,
                                fagsaksystem = Fagsystem.TILLEGGSSTONADER.toString(),
                            ),
                        journalposttype = journalposttyper,
                    ),
                query = graphqlQuery("/saf/journalposterForFagsak.graphql"),
            )
        val response =
            restTemplate.postForEntity<SafJournalpostResponse<SafJournalpostFagsakData>>(
                uri = safUri,
                payload = safJournalpostRequest,
                httpHeaders = httpHeaders(),
            )
        return håndterJournalpostResponse(
            response = response,
            hentJournalposter = { it?.dokumentoversiktFagsak?.journalposter },
            request = safJournalpostRequest,
        )
    }

    fun finnJournalposterForBruker(journalposterForBrukerRequest: JournalposterForBrukerRequest): List<Journalpost> {
        val safJournalpostRequest =
            SafJournalpostRequest(
                journalposterForBrukerRequest,
                graphqlQuery("/saf/journalposterForBruker.graphql"),
            )
        val response =
            restTemplate.postForEntity<SafJournalpostResponse<SafJournalpostBrukerData>>(
                uri = safUri,
                payload = safJournalpostRequest,
                httpHeaders = httpHeaders(),
            )
        return håndterJournalpostResponse(
            response = response,
            hentJournalposter = { it?.dokumentoversiktBruker?.journalposter },
            request = safJournalpostRequest,
        )
    }

    private fun <T> håndterJournalpostResponse(
        response: SafJournalpostResponse<T>,
        hentJournalposter: (T?) -> List<Journalpost>?,
        request: SafJournalpostRequest,
    ): List<Journalpost> {
        if (!response.harFeil()) {
            return hentJournalposter(response.data)
                ?: throw JournalpostRequestException(
                    message = "Kan ikke hente journalposter",
                    cause = null,
                    safJournalpostRequest = request,
                )
        }
        val tilgangFeil = response.errors?.firstOrNull { it.message.contains("Tilgang til ressurs ble avvist") }
        if (tilgangFeil != null) {
            throw JournalpostForbiddenException(tilgangFeil.message)
        }
        throw JournalpostRequestException(
            message = "Kan ikke hente journalposter " + response.errors?.toString(),
            cause = null,
            safJournalpostRequest = request,
        )
    }

    private fun httpHeaders(): HttpHeaders =
        HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            accept = listOf(MediaType.APPLICATION_JSON)
            add(NAV_CALL_ID, MDCOperations.getCallId())
        }

    companion object {
        private const val PATH_GRAPHQL = "graphql"
        private const val NAV_CALL_ID = "Nav-Callid"
    }
}
