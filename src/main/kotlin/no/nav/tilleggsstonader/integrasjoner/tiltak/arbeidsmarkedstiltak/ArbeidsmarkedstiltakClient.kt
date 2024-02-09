package no.nav.tilleggsstonader.integrasjoner.tiltak.arbeidsmarkedstiltak

import no.nav.tilleggsstonader.libs.http.client.AbstractRestClient
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

/**
 * Arbeidsmarkedstiltak hentes fra team komet
 * https://github.com/navikt/amt-tiltak
 */
@Service
class ArbeidsmarkedstiltakClient(
    @Value("\${clients.arbeidsmarkedstiltak.uri}") private val baseUrl: URI,
    @Qualifier("azure") restTemplate: RestTemplate,
) : AbstractRestClient(restTemplate) {

    private val deltakelserUrl = UriComponentsBuilder.fromUri(baseUrl)
        .path("api/external/deltakelser")
        .encode()
        .toUriString()

    fun hentDeltakelser(ident: String): List<DeltakerDto> {
        return postForEntity(deltakelserUrl, mapOf("personIdent" to ident))
    }
}