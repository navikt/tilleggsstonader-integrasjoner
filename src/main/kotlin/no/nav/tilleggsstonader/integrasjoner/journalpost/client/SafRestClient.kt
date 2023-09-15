package no.nav.tilleggsstonader.integrasjoner.journalpost.client

import no.nav.familie.http.util.UriUtil
import no.nav.familie.integrasjoner.felles.MDCOperations
import no.nav.familie.integrasjoner.felles.graphqlQuery
import no.nav.tilleggsstonader.integrasjoner.journalpost.JournalpostForbiddenException
import no.nav.tilleggsstonader.integrasjoner.journalpost.JournalpostRequestException
import no.nav.tilleggsstonader.integrasjoner.journalpost.JournalpostRestClientException
import no.nav.tilleggsstonader.integrasjoner.journalpost.internal.JournalposterForVedleggRequest
import no.nav.tilleggsstonader.integrasjoner.journalpost.internal.SafErrorCode
import no.nav.tilleggsstonader.integrasjoner.journalpost.internal.SafJournalpostBrukerData
import no.nav.tilleggsstonader.integrasjoner.journalpost.internal.SafJournalpostData
import no.nav.tilleggsstonader.integrasjoner.journalpost.internal.SafJournalpostRequest
import no.nav.tilleggsstonader.integrasjoner.journalpost.internal.SafJournalpostResponse
import no.nav.tilleggsstonader.integrasjoner.journalpost.internal.SafRequestVariabler
import no.nav.tilleggsstonader.integrasjoner.util.graphqlQuery
import no.nav.tilleggsstonader.kontrakter.journalpost.Journalpost
import no.nav.tilleggsstonader.kontrakter.journalpost.JournalposterForBrukerRequest
import no.nav.tilleggsstonader.libs.http.client.AbstractRestClient
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Service
class SafRestClient(
    @Value("\${clients.saf.uri}") safBaseUrl: URI,
    @Qualifier("azureOnBehalfOf") restTemplate: RestTemplate,
) : AbstractRestClient(restTemplate) {

    private val safUri = UriComponentsBuilder.fromUri(safBaseUrl).pathSegment(PATH_GRAPHQL).toUriString()

    fun hentJournalpost(journalpostId: String): Journalpost {
        val safJournalpostRequest = SafJournalpostRequest(
            SafRequestVariabler(journalpostId),
            graphqlQuery("/saf/journalpostForId.graphql"),
        )
        val response = postForEntity<SafJournalpostResponse<SafJournalpostData>>(
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
                responsFailure.increment()
                throw JournalpostRestClientException(
                    "Kan ikke hente journalpost " + response.errors?.toString(),
                    null,
                    journalpostId,
                )
            }
        }
    }

    fun finnJournalposter(journalposterForVedleggRequest: JournalposterForVedleggRequest): List<Journalpost> {
        val safJournalpostRequest = SafJournalpostRequest(
            journalposterForVedleggRequest.tilSafRequest(),
            graphqlQuery("/saf/journalposterForBruker.graphql"),
        )
        return finnJournalposter(safJournalpostRequest)
    }

    fun finnJournalposter(journalposterForBrukerRequest: JournalposterForBrukerRequest): List<Journalpost> {
        val safJournalpostRequest = SafJournalpostRequest(
            journalposterForBrukerRequest,
            graphqlQuery("/saf/journalposterForBruker.graphql"),
        )
        return finnJournalposter(safJournalpostRequest)
    }

    fun finnJournalposter(safJournalpostRequest: SafJournalpostRequest): List<Journalpost> {
        val response =
            postForEntity<SafJournalpostResponse<SafJournalpostBrukerData>>(
                safUri,
                safJournalpostRequest,
                httpHeaders(),
            )

        if (!response.harFeil()) {
            return response.data?.dokumentoversiktBruker?.journalposter
                ?: throw JournalpostRequestException(
                    "Kan ikke hente journalposter",
                    null,
                    safJournalpostRequest,
                )
        } else {
            val tilgangFeil =
                response.errors?.firstOrNull { it.message?.contains("Tilgang til ressurs ble avvist") == true }

            if (tilgangFeil != null) {
                throw JournalpostForbiddenException(tilgangFeil.message)
            } else {
                responsFailure.increment()
                throw JournalpostRequestException(
                    "Kan ikke hente journalposter " + response.errors?.toString(),
                    null,
                    safJournalpostRequest,
                )
            }
        }
    }

    private fun httpHeaders(): HttpHeaders {
        return HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            accept = listOf(MediaType.APPLICATION_JSON)
            add(NAV_CALL_ID, MDCOperations.getCallId())
        }
    }

    companion object {

        private const val PATH_GRAPHQL = "graphql"
        private const val NAV_CALL_ID = "Nav-Callid"
    }
}
