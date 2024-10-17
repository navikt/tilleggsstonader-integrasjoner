package no.nav.tilleggsstonader.integrasjoner.fullmakt

import no.nav.tilleggsstonader.kontrakter.felles.IdentRequest
import no.nav.tilleggsstonader.libs.http.client.AbstractRestClient
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.util.UUID

import java.net.URI
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Base64

@Component
class PdlFullmaktClient(
    @Value("\${clients.pdl-fullmakt.uri}") private val baseUrl: URI,
    @Qualifier("azureClientCredential") restTemplate: RestTemplate,
) : AbstractRestClient(restTemplate) {

    fun hentFullmektige(fullmaktsgiversIdent: IdentRequest): List<FullmektigDto> {
        val uri = UriComponentsBuilder.fromUri(baseUrl)
            .pathSegment("api", "internbruker", "fullmaktsgiver")
            .encode().toUriString()

        return postForEntity<List<FullmaktsgiverResponseDto>>(
            uri = uri,
            payload = FullmaktIdentRequest.fra(fullmaktsgiversIdent),
        ).map { it.tilFullmektigDto() }
    }
}

// TODO: Legg i kontrakter
data class FullmektigDto(
    val fullmektigIdent: String,
    val fullmektigNavn: String,
    val gyldigFraOgMed: LocalDate,
    val gyldigTilOgMed: LocalDate?,
    val temaer: List<String>,
)

class FullmaktIdentRequest private constructor(
    val ident: String, // Base64 encoded
) {
    companion object {
        fun fra(identRequest: IdentRequest): FullmaktIdentRequest {
            val encodedIdent = Base64.getEncoder().encodeToString(identRequest.ident.toByteArray())
            return FullmaktIdentRequest(encodedIdent)
        }
    }
}

data class FullmaktsgiverResponseDto(
    val fullmaktId: Int,
    val registrert: LocalDateTime,
    val registrertAv: String,
    val endret: LocalDateTime?,
    val endretAv: String?,
    val opphoert: Boolean,
    val fullmaktsgiver: String,
    val fullmektig: String,
    val omraade: List<OmrådeReprDto>,
    val gyldigFraOgMed: LocalDate,
    val gyldigTilOgMed: LocalDate?,
    val fullmaktUuid: UUID,
    val opplysningsId: UUID?,
    val endringsId: Int?,
    val status: String?,
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

data class OmrådeReprDto(
    val tema: String,
    val handling: List<HandlingRepr>,
)

enum class HandlingRepr {
    LES,
    KOMMUNISER,
    SKRIV
}
