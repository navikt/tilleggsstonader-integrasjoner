package no.nav.tilleggsstonader.integrasjoner.saksbehandler

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.tilleggsstonader.kontrakter.felles.Saksbehandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(value = ["/api/saksbehandler"])
class SaksbehandlerController(
    private val saksbehandlerService: SaksbehandlerService,
) {
    @GetMapping(path = ["/{id}"])
    @ProtectedWithClaims(issuer = "azuread")
    fun hentSaksbehandler(
        @PathVariable id: String,
    ): Saksbehandler { // id kan være azure-id, e-post eller nav-ident
        return saksbehandlerService.hentSaksbehandler(id)
    }
}
