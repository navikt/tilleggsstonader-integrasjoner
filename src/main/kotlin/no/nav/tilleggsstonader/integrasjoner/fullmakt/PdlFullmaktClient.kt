package no.nav.tilleggsstonader.integrasjoner.fullmakt

import no.nav.tilleggsstonader.libs.http.client.AbstractRestClient
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.util.UUID

import java.net.URI
import java.time.LocalDate
import java.time.LocalDateTime

@Component
class PdlFullmaktClient(
    @Value("\${clients.pdl-fullmakt.uri}") private val baseUrl: URI,
    @Qualifier("azureClientCredential") restTemplate: RestTemplate,
) : AbstractRestClient(restTemplate) {

    fun hentFullmektige(fullmaktsgiversIdent: String): List<FullmektigDto> {
        val uri = UriComponentsBuilder.fromUri(baseUrl)
            .pathSegment("api", "internbruker", "fullmaktsgiver")
            .encode().toUriString()

        return postForEntity<List<FullmaktsgiverPldDto>>(
            uri = uri,
            payload = FullmaktIdentPdlRequest(fullmaktsgiversIdent),
        ).map { it.tilFullmektigDto() }
    }
}

// TODO: Legg i kontrakter
data class FullmektigDto(
    val fullmektigIdent: String,
    val fullmektigNavn: String? = null,
    val gyldigFraOgMed: LocalDate,
    val gyldigTilOgMed: LocalDate,
    val temaer: List<String>,
)

private data class FullmaktIdentPdlRequest(
    val ident: String,
)

private data class FullmaktsgiverPldDto(
    val fullmaktId: Int,
    val registrert: LocalDateTime,
    val registrertAv: String,
    val endret: LocalDateTime,
    val endretAv: String,
    val opphoert: Boolean,
    val fullmaktsgiver: String,
    val fullmektig: String,
    val omraade: List<OmrådePldDto>,
    val gyldigFraOgMed: LocalDate,
    val gyldigTilOgMed: LocalDate,
    val fullmaktUuid: UUID,
    val opplysningsId: UUID,
    val endringsId: Int,
    val status: String,
    val kilde: String,
    val fullmaktsgiverNavn: String,
    val fullmektigsNavn: String,
) {
    fun tilFullmektigDto(): FullmektigDto {
        return FullmektigDto(
            fullmektigIdent = fullmektig,
            fullmektigNavn = fullmektigsNavn,
            gyldigFraOgMed = gyldigFraOgMed,
            gyldigTilOgMed = gyldigTilOgMed,
            temaer = omraade.map { it.tema }
        )
    }
}

private data class OmrådePldDto(
    val tema: String,
    val handling: Handling,
)

private enum class Handling {
    LES,
    KOMMUNISER,
    SKRIV
}
