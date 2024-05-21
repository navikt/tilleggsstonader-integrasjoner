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

    val uriAktiviteter = UriComponentsBuilder.fromUri(baseUrl)
        .pathSegment("api", "v1", "tilleggsstoenad", "aktiviteter")
        .queryParam("fom", "{fom}")
        .queryParam("tom", "{tom}")
        .encode().toUriString()

    val uriMålgrupper = UriComponentsBuilder.fromUri(baseUrl)
        .pathSegment("api", "v1", "maalgrupper")
        .queryParam("fom", "{fom}")
        .queryParam("tom", "{tom}")
        .encode().toUriString()

    /**
     * @param tom Default: 60 dager frem i tid.
     */
    fun hentAktiviteter(ident: String, fom: LocalDate, tom: LocalDate): List<AktivitetArenaResponse> {
        val uriVariables = mutableMapOf<String, Any>("fom" to fom, "tom" to tom)

        val headers = hentAktivitetHeaders(ident)

        try {
            return getForEntity<List<AktivitetArenaResponse>>(uriAktiviteter, headers, uriVariables)
        } catch (e: BadRequest) {
            if (manglerFødselsnummer(e)) return emptyList()
            throw e
        }
    }

    fun hentMålgrupper(ident: String, fom: LocalDate, tom: LocalDate): List<MålgruppeArenaResponse> {
        val uriVariables = mutableMapOf<String, Any>("fom" to fom, "tom" to tom)

        val headers = hentAktivitetHeaders(ident)

        try {
            return getForEntity<List<MålgruppeArenaResponse>>(uriMålgrupper, headers, uriVariables)
        } catch (e: BadRequest) {
            if (manglerFødselsnummer(e)) return emptyList()
            throw e
        }
    }

    private fun manglerFødselsnummer(e: BadRequest): Boolean {
        if (e.responseBodyAsString.contains("Person med fødselsnummer")) {
            logger.warn("Person finnes ikke i Arena")
            return true
        }
        return false
    }

    private fun hentAktivitetHeaders(ident: String): HttpHeaders =
        HttpHeaders().apply { add("NAV-Personident", ident) }
}
