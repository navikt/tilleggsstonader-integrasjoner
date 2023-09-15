package no.nav.tilleggsstonader.integrasjoner.dokdist

import no.nav.tilleggsstonader.integrasjoner.dokdist.domene.DistribuerJournalpostRequestTo
import no.nav.tilleggsstonader.integrasjoner.dokdist.domene.DistribuerJournalpostResponseTo
import no.nav.tilleggsstonader.libs.http.client.AbstractRestClient
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Component
class DokdistRestClient(
    @Value("\${clients.dokdist.uri}") private val dokdistUri: URI,
    @Qualifier("azure") restTemplate: RestTemplate,
) :
    AbstractRestClient(restTemplate) {

    val distribuerUri = UriComponentsBuilder.fromUri(dokdistUri).path(PATH_DISTRIBUERJOURNALPOST).toUriString()

    fun distribuerJournalpost(req: DistribuerJournalpostRequestTo): DistribuerJournalpostResponseTo =
        postForEntity(distribuerUri, req)

    companion object {
        private const val PATH_DISTRIBUERJOURNALPOST = "rest/v1/distribuerjournalpost"
    }
}
