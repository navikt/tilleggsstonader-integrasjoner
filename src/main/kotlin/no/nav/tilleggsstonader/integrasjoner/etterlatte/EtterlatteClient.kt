package no.nav.tilleggsstonader.integrasjoner.etterlatte

import no.nav.tilleggsstonader.libs.http.client.AbstractRestClient
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import java.time.LocalDate

@Component
class EtterlatteClient(
    @Value("\${clients.etterlatte.uri}") private val baseUrl: URI,
    @Qualifier("azure") restTemplate: RestTemplate,
) : AbstractRestClient(restTemplate) {
    val uriPerioder =
        UriComponentsBuilder
            .fromUri(baseUrl)
            .pathSegment("api", "pensjon", "vedtak")
            .queryParam("fomDato", "{fomDato}")
            .encode()
            .toUriString()

    fun hentPerioder(
        ident: String,
        fom: LocalDate,
    ): List<Samordningsvedtak> =
        getForEntity<List<Samordningsvedtak>>(
            uriPerioder,
            httpHeaders = HttpHeaders().apply { add("fnr", ident) },
            uriVariables = mapOf("fomDato" to fom),
        )
}
