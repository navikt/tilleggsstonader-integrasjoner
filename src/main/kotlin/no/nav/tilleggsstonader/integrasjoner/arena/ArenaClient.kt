package no.nav.tilleggsstonader.integrasjoner.arena

import no.nav.tilleggsstonader.libs.http.client.AbstractRestClient
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException.BadRequest
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import java.time.LocalDate

/**
 * https://confluence.adeo.no/display/ARENA/Arena+-+Tjeneste+Webservice+-+SakOgAktivitet_v1
 */
@Component
class ArenaClient(
    @Value("\${clients.arena.uri}") private val baseUrl: URI,
    @Qualifier("azure") restTemplate: RestTemplate,
) : AbstractRestClient(restTemplate) {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * @param tom Default: 60 dager frem i tid.
     */
    fun hentAktiviteter(ident: String, fom: LocalDate, tom: LocalDate?): List<AktivitetArenaResponse> {
        val uriVariables = mutableMapOf<String, Any>("fom" to fom)

        val uriBuilder = UriComponentsBuilder.fromUri(baseUrl)
            .pathSegment("api", "v1", "tilleggsstoenad", "aktiviteter")
            .queryParam("fom", "{fom}")

        if (tom != null) {
            uriBuilder.queryParam("tom", "{tom}")
            uriVariables["tom"] = tom
        }

        val headers = hentAktivitetHeaders(ident)

        try {
            return getForEntity<List<AktivitetArenaResponse>>(uriBuilder.encode().toUriString(), headers, uriVariables)
        } catch (e: BadRequest) {
            if (e.responseBodyAsString.contains("Person med fødselsnummer")) {
                logger.warn("Person finnes ikke i Arena")
                return emptyList()
            }
            throw e
        }
    }

    private fun hentAktivitetHeaders(ident: String): HttpHeaders =
        HttpHeaders().apply { add("NAV-Personident", ident) }
}
