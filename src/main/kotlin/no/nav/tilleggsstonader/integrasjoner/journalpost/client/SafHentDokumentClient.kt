package no.nav.tilleggsstonader.integrasjoner.journalpost.client

import no.nav.tilleggsstonader.integrasjoner.journalpost.JournalpostForbiddenException
import no.nav.tilleggsstonader.integrasjoner.journalpost.JournalpostRestClientException
import no.nav.tilleggsstonader.integrasjoner.util.MDCOperations
import no.nav.tilleggsstonader.libs.http.client.AbstractRestClient
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

/**
 * Henting av dokumenter krever saksbehandler context.
 * Bruk av denne clienten vil fungere med et azure systemtoken,
 * men vil stange i saf sin implementasjon mot abac.
 */
@Service
class SafHentDokumentClient(
    @Value("\${clients.saf.uri}") safBaseUrl: URI,
    @Qualifier("azureOnBehalfOf") restTemplate: RestTemplate,
) : AbstractRestClient(restTemplate) {

    private val safHentdokumentUri = UriComponentsBuilder.fromUri(safBaseUrl).path(PATH_HENT_DOKUMENT)
        .encode()
        .toUriString()

    private fun httpHeaders(): HttpHeaders {
        return HttpHeaders().apply {
            accept = listOf(MediaType.ALL)
            add(NAV_CALL_ID, MDCOperations.getCallId())
        }
    }

    fun hentDokument(journalpostId: String, dokumentInfoId: String, variantFormat: String): ByteArray {
        try {
            val uriVariables = mapOf(
                "journalpostId" to journalpostId,
                "dokumentInfoId" to dokumentInfoId,
                "variantFormat" to variantFormat,
            )
            return getForEntity(safHentdokumentUri, httpHeaders(), uriVariables = uriVariables)
        } catch (e: HttpClientErrorException.Forbidden) {
            throw JournalpostForbiddenException(e.message, e)
        } catch (e: Exception) {
            throw JournalpostRestClientException(e.message, e, journalpostId)
        }
    }

    companion object {
        private const val PATH_HENT_DOKUMENT = "/rest/hentdokument/{journalpostId}/{dokumentInfoId}/{variantFormat}"
        private const val NAV_CALL_ID = "Nav-Callid"
    }
}
