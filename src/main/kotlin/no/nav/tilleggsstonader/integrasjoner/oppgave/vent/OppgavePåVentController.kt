package no.nav.tilleggsstonader.integrasjoner.oppgave.vent

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.tilleggsstonader.kontrakter.oppgave.vent.OppdaterPåVentRequest
import no.nav.tilleggsstonader.kontrakter.oppgave.vent.SettPåVentRequest
import no.nav.tilleggsstonader.kontrakter.oppgave.vent.SettPåVentResponse
import no.nav.tilleggsstonader.kontrakter.oppgave.vent.TaAvVentRequest
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@ProtectedWithClaims(issuer = "azuread")
@RequestMapping("/api/oppgave/vent")
class OppgavePåVentController(
    private val settPåVentService: SettPåVentService,
) {
    @PostMapping("/sett-pa-vent")
    fun settPåVent(
        @RequestBody settPåVentRequest: SettPåVentRequest,
    ): SettPåVentResponse = settPåVentService.settPåVent(settPåVentRequest)

    @PostMapping("/oppdater-pa-vent")
    fun oppdaterPåVent(
        @RequestBody oppdaterPåVentRequest: OppdaterPåVentRequest,
    ): SettPåVentResponse = settPåVentService.oppdaterSettPåVent(oppdaterPåVentRequest)

    @PostMapping("/ta-av-vent")
    fun taAvVent(
        @RequestBody taAvVentRequest: TaAvVentRequest,
    ): SettPåVentResponse = settPåVentService.taAvVent(taAvVentRequest)
}
