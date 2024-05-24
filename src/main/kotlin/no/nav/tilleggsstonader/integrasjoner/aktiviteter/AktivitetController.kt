package no.nav.tilleggsstonader.integrasjoner.aktiviteter

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.tilleggsstonader.integrasjoner.arena.ArenaService
import no.nav.tilleggsstonader.kontrakter.aktivitet.AktivitetArenaDto
import no.nav.tilleggsstonader.kontrakter.felles.IdentRequest
import no.nav.tilleggsstonader.libs.sikkerhet.EksternBrukerUtils
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@ProtectedWithClaims(issuer = "azuread")
@RequestMapping("/api/aktivitet")
@Validated
class AktivitetController(
    private val arenaService: ArenaService,
) {

    @PostMapping("finn")
    fun hentAktiviteter(
        @RequestBody identRequest: IdentRequest,
        @RequestParam fom: LocalDate,
        @RequestParam tom: LocalDate,
    ): List<AktivitetArenaDto> {
        return arenaService.hentAktiviteter(identRequest.ident, fom, tom)
    }

    @GetMapping("tokenx")
    @ProtectedWithClaims(issuer = EksternBrukerUtils.ISSUER_TOKENX, claimMap = ["acr=Level4"])
    fun hentAktiviteterTokenX(
        @RequestParam fom: LocalDate,
        @RequestParam tom: LocalDate,
    ): List<AktivitetArenaDto> {
        return arenaService.hentAktiviteter(EksternBrukerUtils.hentFnrFraToken(), fom, tom)
    }
}
