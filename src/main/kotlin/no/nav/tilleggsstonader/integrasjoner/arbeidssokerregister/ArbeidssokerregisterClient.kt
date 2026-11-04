package no.nav.tilleggsstonader.integrasjoner.arbeidssokerregister

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class ArbeidssokerregisterClient(
    @Qualifier("arbeidssokerregisterRestClient") private val restClient: RestClient,
) {
    internal fun hentPerioder(ident: String): List<Arbeidssokerperiode> =
        checkNotNull(
            restClient
                .post()
                .uri { it.pathSegment("api", "v3", "perioder").build() }
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(ArbeidssokerregisterRequest(identitetsnummer = ident))
                .retrieve()
                .body(object : ParameterizedTypeReference<List<Arbeidssokerperiode>>() {}),
        ) { "Mangler responsbody fra arbeidssøkerregisteret" }
}
