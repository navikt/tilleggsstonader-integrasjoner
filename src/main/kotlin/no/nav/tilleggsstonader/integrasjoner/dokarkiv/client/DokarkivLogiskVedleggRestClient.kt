package no.nav.tilleggsstonader.integrasjoner.dokarkiv.client

import no.nav.tilleggsstonader.integrasjoner.infrastruktur.exception.OppslagException
import no.nav.tilleggsstonader.integrasjoner.util.MDCOperations
import no.nav.tilleggsstonader.kontrakter.dokarkiv.LogiskVedleggRequest
import no.nav.tilleggsstonader.kontrakter.dokarkiv.LogiskVedleggResponse
import no.nav.tilleggsstonader.libs.http.client.AbstractRestClient
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Component
class DokarkivLogiskVedleggRestClient(
    @Value("\${clients.dokarkiv.uri}") private val dokarkivUrl: URI,
    @Qualifier("azure") restTemplate: RestTemplate,
) :
    AbstractRestClient(restTemplate) {

    fun opprettLogiskVedlegg(dokumentInfoId: String, request: LogiskVedleggRequest): LogiskVedleggResponse {
        val uri = UriComponentsBuilder
            .fromUri(dokarkivUrl)
            .path(PATH_LOGISKVEDLEGG)
            .encode()
            .toUriString()
        try {
            return postForEntity(uri, request, headers(), mapOf("dokumentInfo" to dokumentInfoId))
        } catch (e: RuntimeException) {
            val responsebody = if (e is HttpStatusCodeException) e.responseBodyAsString else ""
            val message = "Kan ikke opprette logisk vedlegg for dokumentinfo $dokumentInfoId $responsebody"
            throw OppslagException(
                message,
                "Dokarkiv.logiskVedlegg.opprett",
                OppslagException.Level.MEDIUM,
                HttpStatus.INTERNAL_SERVER_ERROR,
                e,
            )
        }
    }

    fun slettLogiskVedlegg(dokumentInfoId: String, logiskVedleggId: String) {
        val uri = UriComponentsBuilder
            .fromUri(dokarkivUrl)
            .path(PATH_SLETT_LOGISK_VEDLEGG)
            .encode()
            .toUriString()
        try {
            val uriVariables = mapOf("dokumentInfo" to dokumentInfoId, "logiskVedleggId" to logiskVedleggId)
            deleteForEntity<String>(uri, null, headers(), uriVariables)
        } catch (e: RuntimeException) {
            val responsebody = if (e is HttpStatusCodeException) e.responseBodyAsString else ""
            val message = "Kan ikke slette logisk vedlegg for dokumentinfo $dokumentInfoId $responsebody"
            throw OppslagException(
                message,
                "Dokarkiv.logiskVedlegg.slett",
                OppslagException.Level.MEDIUM,
                HttpStatus.INTERNAL_SERVER_ERROR,
                e,
            )
        }
    }

    companion object {

        private const val PATH_LOGISKVEDLEGG = "rest/journalpostapi/v1/dokumentInfo/{dokumentInfo}/logiskVedlegg/"
        private const val PATH_SLETT_LOGISK_VEDLEGG = "$PATH_LOGISKVEDLEGG/{logiskVedleggId}"

        private const val NAV_CALL_ID = "Nav-Callid"

        private fun headers(): HttpHeaders {
            return HttpHeaders().apply {
                add(NAV_CALL_ID, MDCOperations.getCallId())
            }
        }
    }
}
