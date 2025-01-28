package no.nav.tilleggsstonader.integrasjoner.oppgave

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.tilleggsstonader.kontrakter.oppgave.FinnMappeRequest
import no.nav.tilleggsstonader.kontrakter.oppgave.FinnMappeResponseDto
import no.nav.tilleggsstonader.kontrakter.oppgave.FinnOppgaveRequest
import no.nav.tilleggsstonader.kontrakter.oppgave.FinnOppgaveResponseDto
import no.nav.tilleggsstonader.kontrakter.oppgave.MappeDto
import no.nav.tilleggsstonader.kontrakter.oppgave.OppdatertOppgaveResponse
import no.nav.tilleggsstonader.kontrakter.oppgave.Oppgave
import no.nav.tilleggsstonader.kontrakter.oppgave.OppgaveResponse
import no.nav.tilleggsstonader.kontrakter.oppgave.OpprettOppgaveRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@ProtectedWithClaims(issuer = "azuread")
@RequestMapping("/api/oppgave")
class OppgaveController(
    private val oppgaveService: OppgaveService,
) {
    @GetMapping("/{oppgaveId}")
    fun hentOppgave(
        @PathVariable(name = "oppgaveId") oppgaveId: String,
    ): Oppgave = oppgaveService.hentOppgave(oppgaveId.toLong())

    @PostMapping("/finn")
    fun finnOppgaverV4(
        @RequestBody finnOppgaveRequest: FinnOppgaveRequest,
    ): FinnOppgaveResponseDto = oppgaveService.finnOppgaver(finnOppgaveRequest)

    @PostMapping("/mappe/sok")
    @Deprecated(message = "Bruk get under") // Hvilken burde vi bruke?
    fun finnMapperV1Deprecated(
        @RequestBody finnMappeRequest: FinnMappeRequest,
    ): FinnMappeResponseDto = oppgaveService.finnMapper(finnMappeRequest)

    @GetMapping("/mappe/sok")
    fun finnMapperV1(finnMappeRequest: FinnMappeRequest): FinnMappeResponseDto = oppgaveService.finnMapper(finnMappeRequest)

    @GetMapping("/mappe/finn/{enhetNr}")
    fun finnMapper(
        @PathVariable enhetNr: String,
    ): List<MappeDto> = oppgaveService.finnMapper(enhetNr)

    @PostMapping("/{oppgaveId}/fordel")
    fun fordelOppgave(
        @PathVariable(name = "oppgaveId") oppgaveId: Long,
        @RequestParam("saksbehandler") saksbehandler: String?,
        @RequestParam("versjon") versjon: Int?,
    ): Oppgave = oppgaveService.fordelOppgave(oppgaveId, saksbehandler, versjon)

    @PatchMapping("/{oppgaveId}/oppdater")
    fun patchOppgave(
        @PathVariable(name = "oppgaveId") oppgaveId: Long,
        @RequestBody oppgave: Oppgave,
    ): OppdatertOppgaveResponse {
        require(oppgaveId == oppgave.id) { "OppgaveId i path($oppgaveId) er ikke lik body(${oppgave.id})" }
        val oppgave = oppgaveService.patchOppgave(oppgave)
        return OppdatertOppgaveResponse(oppgaveId = oppgaveId, versjon = oppgave.versjonEllerFeil())
    }

    @PostMapping("/opprett")
    fun opprettOppgaveV2(
        @RequestBody oppgave: OpprettOppgaveRequest,
    ): ResponseEntity<OppgaveResponse> {
        val oppgaveId = oppgaveService.opprettOppgave(oppgave)
        return ResponseEntity.status(HttpStatus.CREATED).body(OppgaveResponse(oppgaveId = oppgaveId))
    }

    @PatchMapping("/{oppgaveId}/ferdigstill")
    fun ferdigstillOppgave(
        @PathVariable(name = "oppgaveId") oppgaveId: Long,
        @RequestParam(name = "versjon") versjon: Int?,
    ): OppgaveResponse {
        oppgaveService.ferdigstill(oppgaveId, versjon)
        return OppgaveResponse(oppgaveId = oppgaveId)
    }

    @PatchMapping("/{oppgaveId}/fjern-behandles-av-applikasjon")
    fun fjernBehandlesAvApplikasjon(
        @PathVariable(name = "oppgaveId") oppgaveId: Long,
        @RequestParam(name = "versjon") versjon: Int,
    ): OppgaveResponse {
        oppgaveService.fjernBehandlesAvApplikasjon(oppgaveId, versjon)
        return OppgaveResponse(oppgaveId = oppgaveId)
    }
}
