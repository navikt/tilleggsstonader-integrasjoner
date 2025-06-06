package no.nav.tilleggsstonader.integrasjoner.dagpenger

import no.nav.tilleggsstonader.libs.http.client.AbstractRestClient
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import java.time.LocalDate

@Component
class DagpengerClient(
    @Value("\${clients.dagpenger.uri}") private val baseUrl: URI,
    @Qualifier("azure") restTemplate: RestTemplate,
) : AbstractRestClient(restTemplate) {
    val dagpengerUri =
        UriComponentsBuilder
            .fromUri(baseUrl)
            .pathSegment("dagpenger", "datadeling", "v1", "perioder")
            .encode()
            .toUriString()

    fun hentPerioder(
        ident: String,
        fom: LocalDate,
        tom: LocalDate? = null,
    ): DagpengerPerioderResponse {
        val request =
            mapOf(
                "personidentifikator" to ident,
                "fraOgMedDato" to fom.toString(),
                "tilOgMedDato" to tom?.toString(),
            )
        return postForEntity<DagpengerPerioderResponse>(dagpengerUri, request)
    }
}
