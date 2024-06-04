package no.nav.tilleggsstonader.integrasjoner.ytelse

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.tilleggsstonader.kontrakter.ytelse.YtelsePerioderDto
import no.nav.tilleggsstonader.kontrakter.ytelse.YtelsePerioderRequest
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@ProtectedWithClaims(issuer = "azuread")
@RequestMapping("/api/ytelse")
@Validated
class YtelseController(
    private val ytelseService: YtelseService,
) {

    @PostMapping("finn")
    fun hentYtelser(
        @RequestBody request: YtelsePerioderRequest,
    ): YtelsePerioderDto {
        return ytelseService.hentYtelser(request)
    }
}
