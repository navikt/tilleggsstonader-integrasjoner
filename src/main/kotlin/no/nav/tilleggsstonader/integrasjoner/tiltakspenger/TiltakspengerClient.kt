package no.nav.tilleggsstonader.integrasjoner.tiltakspenger

import no.nav.tilleggsstonader.integrasjoner.ensligforsørger.EnsligForsørgerPerioderResponse
import no.nav.tilleggsstonader.libs.http.client.AbstractRestClient
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import java.time.LocalDate

@Component
class TiltakspengerClient(
    @Value("\${clients.tiltakspenger.uri}") private val baseUrl: URI,
    @Qualifier("azure") restTemplate: RestTemplate,
) : AbstractRestClient(restTemplate) {
    val uriPerioder =
        UriComponentsBuilder
            .fromUri(baseUrl)
            .pathSegment("vedtak", "perioder")
            .encode()
            .toUriString()

    fun hentPerioder(
        ident: String,
        fom: LocalDate,
        tom: LocalDate,
    ): TiltakspengerPerioderResponse{
        val request =
            mapOf(
                "ident" to ident,
                "fom" to fom,
                "tom" to tom,
            )
        return postForEntity<TiltakspengerPerioderResponse>(uriPerioder, request)
    }
}
