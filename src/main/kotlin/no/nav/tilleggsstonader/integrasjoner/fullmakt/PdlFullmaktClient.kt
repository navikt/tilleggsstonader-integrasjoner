package no.nav.tilleggsstonader.integrasjoner.fullmakt;

import no.nav.tilleggsstonader.libs.http.client.AbstractRestClient
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder
import java.util.UUID

import java.net.URI;
import java.time.LocalDate
import java.time.LocalDateTime

@Component
class PdlFullmaktClient(
    @Value("\${clients.pdl-fullmakt.uri}") private val baseUrl: URI,
    @Qualifier("azure") restTemplate: RestTemplate,
) : AbstractRestClient(restTemplate) {

    fun hentFullmaktsgiver(identRequest: FullmaktIdentRequest): List<Fullmaktsgiver> {
        val uri = UriComponentsBuilder.fromUri(baseUrl)
            .pathSegment("api", "internbruker", "fullmaktsgiver")
            .encode().toUriString()

        return postForEntity<List<Fullmaktsgiver>>(
            uri = uri,
            payload = identRequest,
        )
    }
}

// TODO: Flytt til kontrakter
data class Fullmaktsgiver(
    val fullmaktId: Int,
    val registrert: LocalDateTime,
    val registrertAv: String,
    val endret: LocalDateTime,
    val endretAv: String,
    val opphoert: Boolean,
    val fullmaktsgiver: String,
    val fullmektig: String,
    val omraade: List<OmrådeMedHandling>,
    val gyldigFraOgMed: LocalDate,
    val gyldigTilOgMed: LocalDate,
    val fullmaktUuid: UUID,
    val opplysningsId: UUID,
    val endringsId: Int,
    val status: String,
    val kilde: String,
    val fullmaktsgiverNavn: String,
    val fullmektigsNavn: String,
)


data class OmrådeMedHandling(
    val tema: String,
    val handling: Handling,
)

enum class Handling {
    LES,
    KOMMUNISER,
    SKRIV
}
