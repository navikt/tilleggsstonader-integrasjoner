package no.nav.tilleggsstonader.integrasjoner.fullmakt

import no.nav.tilleggsstonader.integrasjoner.infrastruktur.exception.OppslagException
import no.nav.tilleggsstonader.integrasjoner.infrastruktur.exception.OppslagException.Level.MEDIUM
import no.nav.tilleggsstonader.kontrakter.felles.IdentRequest
import no.nav.tilleggsstonader.kontrakter.fullmakt.FullmektigDto
import no.nav.tilleggsstonader.libs.http.client.postForEntity
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Component
class FullmaktClient(
    @Value("\${clients.repr-api.uri}") private val baseUrl: URI,
    @Qualifier("azureClientCredential") private val restTemplate: RestTemplate,
) {
    fun hentFullmektige(fullmaktsgiversIdent: IdentRequest): List<FullmektigDto> {
        val uri =
            UriComponentsBuilder
                .fromUri(baseUrl)
                .pathSegment("api", "internbruker", "fullmakt", "fullmaktsgiver")
                .encode()
                .toUriString()

        return try {
            restTemplate
                .postForEntity<List<FullmaktsgiverResponse>>(
                    uri = uri,
                    payload = FullmaktIdentRequest.fra(fullmaktsgiversIdent),
                ).map { it.tilFullmektigDto() }
        } catch (ex: RestClientResponseException) {
            throw OppslagException(
                message = "Kunne ikke hente ut fullmakter fra REPR",
                kilde = "hentFullmektige",
                level = MEDIUM,
                httpStatus = ex.statusCode,
                error = ex,
                sensitiveInfo = "ident: $fullmaktsgiversIdent",
            )
        }
    }
}

private data class FullmaktIdentRequest(
    val ident: String, // Base64-encodet
) {
    companion object {
        fun fra(identRequest: IdentRequest): FullmaktIdentRequest = FullmaktIdentRequest(identRequest.ident)
    }
}

private data class FullmaktsgiverResponse(
    val fullmaktId: Int,
    val registrert: LocalDateTime,
    val registrertAv: String,
    val endret: LocalDateTime?,
    val endretAv: String?,
    val opphoert: Boolean,
    val fullmaktsgiver: String,
    val fullmektig: String,
    val omraade: List<OmrådeResponse>,
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
    fun tilFullmektigDto(): FullmektigDto =
        FullmektigDto(
            fullmektigIdent = fullmektig,
            fullmektigNavn = fullmektigsNavn,
            gyldigFraOgMed = gyldigFraOgMed,
            gyldigTilOgMed = gyldigTilOgMed,
            temaer = omraade.map { it.tema },
        )
}

private data class OmrådeResponse(
    val tema: String,
    val handling: List<Handling>,
)

private enum class Handling {
    LES,
    KOMMUNISER,
    SKRIV,
}
