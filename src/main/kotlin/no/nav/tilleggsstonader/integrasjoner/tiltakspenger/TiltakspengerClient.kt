package no.nav.tilleggsstonader.integrasjoner.tiltakspenger

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
    @Deprecated(
        "Periodene vi mottar fra dette endepunktet tar ikke hensyn til omgjøringer, som gjør at de ikke blir helt til å stole på.",
        ReplaceWith("hentDetaljer"),
    )
    fun hentPerioderGammelVersjon(
        ident: String,
        fom: LocalDate,
        tom: LocalDate,
    ): List<TiltakspengerPerioderResponseGammel> {
        val request =
            VedtakRequestDto(
                ident = ident,
                fom = fom,
                tom = tom,
            )
        val uri =
            UriComponentsBuilder
                .fromUri(baseUrl)
                .pathSegment("vedtak", "perioder")
                .encode()
                .toUriString()

        return postForEntity<List<TiltakspengerPerioderResponseGammel>>(uri, request)
    }

    /**
     * Henter Tiltakspenger fra Arena. Disse periodene tar ikke hensyn til omgjøringer og kan derfor være ukorrekte.
     */
    fun hentPerioderNyVersjon(
        ident: String,
        fom: LocalDate,
        tom: LocalDate,
    ): List<TiltakspengerPerioderResponseNy> {
        val request =
            VedtakRequestDto(
                ident = ident,
                fom = fom,
                tom = tom,
            )
        val uri =
            UriComponentsBuilder
                .fromUri(baseUrl)
                .pathSegment("vedtak", "perioder")
                .encode()
                .toUriString()

        return postForEntity<List<TiltakspengerPerioderResponseNy>>(uri, request)
    }

    /**
     * Henter perioder fra TPSAK. Disse har tatt hensyn til omgjøring og bør derfor være korrekte.
     */
    fun hentDetaljer(
        ident: String,
        fom: LocalDate,
        tom: LocalDate,
    ): List<TiltakspengerDetaljerResponse> {
        val request =
            VedtakRequestDto(
                ident = ident,
                fom = fom,
                tom = tom,
            )
        val uri =
            UriComponentsBuilder
                .fromUri(baseUrl)
                .pathSegment("vedtak", "detaljer")
                .encode()
                .toUriString()

        return postForEntity<List<TiltakspengerDetaljerResponse>>(uri, request)
    }
}
