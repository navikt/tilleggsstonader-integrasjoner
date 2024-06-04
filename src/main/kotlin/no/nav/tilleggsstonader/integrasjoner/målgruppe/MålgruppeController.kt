package no.nav.tilleggsstonader.integrasjoner.målgruppe

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.tilleggsstonader.integrasjoner.arena.ArenaService
import no.nav.tilleggsstonader.kontrakter.felles.IdentRequest
import no.nav.tilleggsstonader.kontrakter.målgruppe.MålgruppeArenaDto
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@ProtectedWithClaims(issuer = "azuread")
@RequestMapping("/api/maalgruppe")
@Validated
class MålgruppeController(
    private val arenaService: ArenaService,
) {

    @PostMapping("finn")
    fun hentMålgrupper(
        @RequestBody identRequest: IdentRequest,
        @RequestParam fom: LocalDate,
        @RequestParam tom: LocalDate,
    ): List<MålgruppeArenaDto> {
        return arenaService.hentMålgrupper(identRequest.ident, fom, tom)
    }
}
