package no.nav.tilleggsstonader.integrasjoner.ytelse

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.tilleggsstonader.kontrakter.felles.IdentRequest
import no.nav.tilleggsstonader.kontrakter.ytelse.YtelsePerioderDto
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
@RequestMapping("/api/ytelse")
@Validated
class YtelseController(
    private val ytelseService: YtelseService,
) {

    @PostMapping("finn")
    fun hentYtelser(
        @RequestBody identRequest: IdentRequest,
        @RequestParam fom: LocalDate,
        @RequestParam tom: LocalDate,
    ): YtelsePerioderDto {
        return ytelseService.hentYtelser(identRequest.ident, fom, tom)
    }

    @GetMapping("tokenx")
    @ProtectedWithClaims(issuer = EksternBrukerUtils.ISSUER_TOKENX, claimMap = ["acr=Level4"])
    fun hentYtelserTokenX(
        @RequestParam fom: LocalDate,
        @RequestParam tom: LocalDate,
    ): YtelsePerioderDto {
        return ytelseService.hentYtelser(EksternBrukerUtils.hentFnrFraToken(), fom, tom)
    }
}
