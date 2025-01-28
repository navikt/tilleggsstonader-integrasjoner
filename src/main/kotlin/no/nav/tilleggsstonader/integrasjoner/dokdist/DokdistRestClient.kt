package no.nav.tilleggsstonader.integrasjoner.dokdist

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.tilleggsstonader.integrasjoner.dokdist.domene.DistribuerJournalpostRequestTo
import no.nav.tilleggsstonader.integrasjoner.dokdist.domene.DistribuerJournalpostResponseTo
import no.nav.tilleggsstonader.integrasjoner.dokdist.domene.DokdistConflictException
import no.nav.tilleggsstonader.integrasjoner.infrastruktur.exception.OppslagException
import no.nav.tilleggsstonader.kontrakter.felles.ObjectMapperProvider.objectMapper
import no.nav.tilleggsstonader.libs.http.client.AbstractRestClient
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Component
class DokdistRestClient(
    @Value("\${clients.dokdist.uri}") private val dokdistUri: URI,
    @Qualifier("azure") restTemplate: RestTemplate,
) : AbstractRestClient(restTemplate) {
    val distribuerUri = UriComponentsBuilder.fromUri(dokdistUri).path(PATH_DISTRIBUERJOURNALPOST).toUriString()

    fun distribuerJournalpost(req: DistribuerJournalpostRequestTo): DistribuerJournalpostResponseTo =
        try {
            postForEntity(distribuerUri, req)
        } catch (e: RuntimeException) {
            if (e is HttpClientErrorException.Conflict) {
                håndterConflict(e)
            }
            throw e
        }

    private fun håndterConflict(e: HttpClientErrorException.Conflict) {
        var response: DistribuerJournalpostResponseTo? = null
        try {
            response = objectMapper.readValue<DistribuerJournalpostResponseTo>(e.responseBodyAsString)
        } catch (ex: Exception) {
            throw OppslagException(
                "Klarer ikke å parse response fra dokdist ved 409",
                "Dokarkiv",
                OppslagException.Level.KRITISK,
                HttpStatus.INTERNAL_SERVER_ERROR,
                e,
                sensitiveInfo = "Body=${e.responseBodyAsString}",
            )
        }
        throw DokdistConflictException(response)
    }

    companion object {
        private const val PATH_DISTRIBUERJOURNALPOST = "rest/v1/distribuerjournalpost"
    }
}
