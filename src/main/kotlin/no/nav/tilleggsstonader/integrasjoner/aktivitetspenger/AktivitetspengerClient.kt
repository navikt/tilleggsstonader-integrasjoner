package no.nav.tilleggsstonader.integrasjoner.aktivitetspenger

import no.nav.tilleggsstonader.libs.http.client.postForEntity
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import java.time.LocalDate

@Component
class AktivitetspengerClient(
    @Value("\${clients.ung-sak.uri}") private val baseUrl: URI,
    @Qualifier("azure") private val restTemplate: RestTemplate,
) {
    val perioderUri =
        UriComponentsBuilder
            .fromUri(baseUrl)
            .pathSegment("ung", "sak", "api", "ekstern", "tilleggsstonader", "aktivitetspenger", "perioder")
            .encode()
            .toUriString()

    fun hentPerioder(
        ident: String,
        fom: LocalDate,
        tom: LocalDate,
    ): AktivitetspengerPerioderResponse =
        restTemplate.postForEntity(
            perioderUri,
            mapOf(
                "ident" to ident,
                "fom" to fom,
                "tom" to tom,
            ),
        )
}
