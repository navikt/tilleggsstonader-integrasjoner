package no.nav.tilleggsstonader.integrasjoner.tiltak

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.tilleggsstonader.kontrakter.felles.IdentRequest
import no.nav.tilleggsstonader.libs.sikkerhet.EksternBrukerUtils
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@ProtectedWithClaims(issuer = "azuread")
@RequestMapping("/api/tiltak")
class TiltakController(
    private val tiltakService: TiltakService
) {

    @GetMapping
    @ProtectedWithClaims(issuer = EksternBrukerUtils.ISSUER_TOKENX, claimMap = ["acr=Level4"])
    fun hentTiltak(): List<TiltakDto> {
        return tiltakService.hentTiltak(EksternBrukerUtils.hentFnrFraToken())
    }

    @PostMapping
    fun finnOppgaverV4(@RequestBody request: IdentRequest): List<TiltakDto> {
        return tiltakService.hentTiltak(request.ident)
    }
}