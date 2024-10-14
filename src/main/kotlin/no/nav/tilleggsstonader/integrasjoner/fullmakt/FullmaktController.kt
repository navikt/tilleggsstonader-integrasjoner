package no.nav.tilleggsstonader.integrasjoner.fullmakt

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.tilleggsstonader.kontrakter.felles.IdentRequest
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@ProtectedWithClaims(issuer = "azuread")
@RequestMapping("/api/fullmakt")
@Validated
class FullmaktController(
    val fullmaktClient: PdlFullmaktClient,
) {
    @PostMapping("fullmaktsgiver")
    fun hentFullmaktsgiver(identRequest: IdentRequest): List<Fullmaktsgiver> {
        return fullmaktClient.hentFullmaktsgiver(identRequest.tilFullmaktIdentRequest())
    }
}