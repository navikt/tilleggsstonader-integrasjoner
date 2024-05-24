package no.nav.tilleggsstonader.integrasjoner.ensligforsørger

import no.nav.tilleggsstonader.libs.http.client.AbstractRestClient
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import java.time.LocalDate

/**
 * https://confluence.adeo.no/display/ARENA/Arena+-+Tjeneste+Webservice+-+SakOgAktivitet_v1
 */
@Component
class EnsligForsørgerClient(
    @Value("\${clients.enslig.uri}") private val baseUrl: URI,
    @Qualifier("azure") restTemplate: RestTemplate,
) : AbstractRestClient(restTemplate) {

    val uriPerioder = UriComponentsBuilder.fromUri(baseUrl)
        .pathSegment("api", "ekstern", "perioder", "alle-stonader")
        .encode()
        .toUriString()

    fun hentPerioder(ident: String, fom: LocalDate, tom: LocalDate): EnsligForsørgerPerioderResponse {
        val request = mapOf(
            "personIdent" to ident,
            "fomDato" to fom,
            "tomDato" to tom,
        )
        return postForEntity<EnsligForsørgerPerioderResponse>(uriPerioder, request)
    }
}
