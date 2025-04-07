package no.nav.tilleggsstonader.integrasjoner.aktiviteter

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.tilleggsstonader.integrasjoner.util.EksternApplikasjon
import no.nav.tilleggsstonader.integrasjoner.util.SikkerhetsContext
import no.nav.tilleggsstonader.kontrakter.aktivitet.AktivitetArenaDto
import no.nav.tilleggsstonader.kontrakter.felles.Stønadstype
import no.nav.tilleggsstonader.libs.sikkerhet.EksternBrukerUtils
import norskDatoTekstligMåned
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@ProtectedWithClaims(issuer = "azuread")
@RequestMapping("/api/ekstern/aktivitet")
@Validated
class EksternAktivitetController(
    private val søknadAktiviteterService: SøknadAktiviteterService,
) {
    @GetMapping
    @ProtectedWithClaims(issuer = EksternBrukerUtils.ISSUER_TOKENX, claimMap = ["acr=Level4"])
    fun hentAktiviteter(
        @RequestParam stønadstype: Stønadstype,
    ): List<AktivitetSøknadDto> {
        require(SikkerhetsContext.kallKommerFra(EksternApplikasjon.FYLL_UT_SEND_INN_SØKNAD)) {
            "Kall kommer fra ${SikkerhetsContext.applikasjonsnavnFraToken()} som ikke har tilgang til endepunkt"
        }
        return søknadAktiviteterService
            .hentAktiviteter(EksternBrukerUtils.hentFnrFraToken(), stønadstype)
            .sortedByDescending { it.fom ?: LocalDate.MAX }
            .map { it.tilDto() }
    }
}

data class AktivitetSøknadDto(
    val id: String,
    val tekst: String,
    val type: AktivitetSøknadType,
)

enum class AktivitetSøknadType {
    TILTAK,
    UTDANNING,
}

fun AktivitetArenaDto.tilDto(): AktivitetSøknadDto {
    val dato =
        fom?.let {
            "${it.norskDatoTekstligMåned()} - ${tom?.norskDatoTekstligMåned()}"
        } ?: ""
    return AktivitetSøknadDto(
        id = id,
        tekst = "$typeNavn: $dato",
        type = if (erUtdanning == true) AktivitetSøknadType.UTDANNING else AktivitetSøknadType.TILTAK,
    )
}
