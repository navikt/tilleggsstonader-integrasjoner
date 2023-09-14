package no.nav.tilleggsstonader.integrasjoner.oppgave

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.tilleggsstonader.kontrakter.oppgave.FinnMappeRequest
import no.nav.tilleggsstonader.kontrakter.oppgave.FinnMappeResponseDto
import no.nav.tilleggsstonader.kontrakter.oppgave.FinnOppgaveRequest
import no.nav.tilleggsstonader.kontrakter.oppgave.FinnOppgaveResponseDto
import no.nav.tilleggsstonader.kontrakter.oppgave.MappeDto
import no.nav.tilleggsstonader.kontrakter.oppgave.Oppgave
import no.nav.tilleggsstonader.kontrakter.oppgave.OppgaveResponse
import no.nav.tilleggsstonader.kontrakter.oppgave.OpprettOppgaveRequest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
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
class OppgaveController(private val oppgaveService: OppgaveService) {

    @GetMapping("/{oppgaveId}")
    fun hentOppgave(@PathVariable(name = "oppgaveId") oppgaveId: String): Oppgave {
        return oppgaveService.hentOppgave(oppgaveId.toLong())
    }

    @PostMapping("/finn")
    fun finnOppgaverV4(@RequestBody finnOppgaveRequest: FinnOppgaveRequest): FinnOppgaveResponseDto {
        return oppgaveService.finnOppgaver(finnOppgaveRequest)
    }

    @PostMapping("/mappe/sok")
    @Deprecated(message = "Bruk get under") // Hvilken burde vi bruke?
    fun finnMapperV1Deprecated(@RequestBody finnMappeRequest: FinnMappeRequest): FinnMappeResponseDto {
        return oppgaveService.finnMapper(finnMappeRequest)
    }

    @GetMapping("/mappe/sok")
    fun finnMapperV1(finnMappeRequest: FinnMappeRequest): FinnMappeResponseDto {
        return oppgaveService.finnMapper(finnMappeRequest)
    }

    @GetMapping("/mappe/finn/{enhetNr}")
    fun finnMapper(@PathVariable enhetNr: String): List<MappeDto> {
        return oppgaveService.finnMapper(enhetNr)
    }

    @PostMapping("/{oppgaveId}/fordel")
    fun fordelOppgave(
        @PathVariable(name = "oppgaveId") oppgaveId: Long,
        @RequestParam("saksbehandler") saksbehandler: String?,
        @RequestParam("versjon") versjon: Int?,
    ): OppgaveResponse {
        if (saksbehandler == null) {
            oppgaveService.tilbakestillFordelingPåOppgave(oppgaveId, versjon)
        } else {
            oppgaveService.fordelOppgave(oppgaveId, saksbehandler, versjon)
        }

        return OppgaveResponse(oppgaveId = oppgaveId)
    }

    @PostMapping("/oppdater")
    fun oppdaterOppgave(@RequestBody oppgave: Oppgave): OppgaveResponse {
        val oppgaveId = oppgaveService.oppdaterOppgave(oppgave)
        return OppgaveResponse(oppgaveId = oppgaveId)
    }

    @PatchMapping("/{oppgaveId}/oppdater")
    fun patchOppgave(@RequestBody oppgave: Oppgave): OppgaveResponse {
        val oppgaveId = oppgaveService.patchOppgave(oppgave)
        return OppgaveResponse(oppgaveId = oppgaveId)
    }

    @PostMapping("/opprett")
    fun opprettOppgaveV2(@RequestBody oppgave: OpprettOppgaveRequest): ResponseEntity<OppgaveResponse> {
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

    @Operation(description = "Flytter oppgaven fra en enhet til en annen enhet.")
    @PatchMapping("/{oppgaveId}/enhet/{enhet}")
    fun tilordneOppgaveNyEnhet(
        @PathVariable(name = "oppgaveId")
        oppgaveId: Long,
        @Parameter(description = "Enhet oppgaven skal flytte til")
        @PathVariable(name = "enhet")
        enhet: String,
        @Parameter(description = "Settes til true hvis man ønsker å flytte en oppgave uten å ta med seg mappa opp på oppgaven. Noen mapper hører spesifikt til en enhet, og man får da ikke flyttet oppgaven uten at mappen fjernes ")
        @RequestParam(name = "fjernMappeFraOppgave")
        fjernMappeFraOppgave: Boolean,
        @Parameter(description = "Vil feile med 409 Conflict dersom versjonen ikke stemmer overens med oppgavesystemets versjon")
        @RequestParam(name = "versjon")
        versjon: Int?,
    ): OppgaveResponse {
        oppgaveService.tilordneEnhet(oppgaveId, enhet, fjernMappeFraOppgave, versjon)
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
