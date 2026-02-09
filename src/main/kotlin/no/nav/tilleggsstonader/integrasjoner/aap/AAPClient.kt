package no.nav.tilleggsstonader.integrasjoner.aap

import no.nav.tilleggsstonader.libs.http.client.postForEntity
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
class AAPClient(
    @Value("\${clients.aap.uri}") private val baseUrl: URI,
    @Qualifier("azure") private val restTemplate: RestTemplate,
) {
    val uriPerioder =
        UriComponentsBuilder
            .fromUri(baseUrl)
            .pathSegment("perioder", "aktivitetfase")
            .encode()
            .toUriString()

    fun hentPerioder(
        ident: String,
        fom: LocalDate,
        tom: LocalDate,
    ): AAPPerioderResponse {
        val request =
            mapOf(
                "personidentifikator" to ident,
                "fraOgMedDato" to fom,
                "tilOgMedDato" to tom,
            )
        return restTemplate.postForEntity<AAPPerioderResponse>(uriPerioder, request)
    }
}
