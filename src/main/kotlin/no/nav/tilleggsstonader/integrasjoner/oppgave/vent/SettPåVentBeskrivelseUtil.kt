package no.nav.tilleggsstonader.integrasjoner.oppgave.vent

import no.nav.tilleggsstonader.integrasjoner.util.SikkerhetsContext
import no.nav.tilleggsstonader.kontrakter.oppgave.Oppgave
import no.nav.tilleggsstonader.kontrakter.oppgave.vent.OppdaterPåVentRequest
import no.nav.tilleggsstonader.kontrakter.oppgave.vent.SettPåVentRequest
import no.nav.tilleggsstonader.kontrakter.oppgave.vent.TaAvVentRequest
import no.nav.tilleggsstonader.libs.utils.medGosysTid
import no.nav.tilleggsstonader.libs.utils.norskFormat
import no.nav.tilleggsstonader.libs.utils.osloNow
import java.time.LocalDate
import java.time.LocalDateTime

object SettPåVentBeskrivelseUtil {
    fun settPåVent(
        oppgave: Oppgave,
        settPåVent: SettPåVentRequest,
        tidspunkt: LocalDateTime = osloNow(),
    ): String {
        val tilordnetSaksbehandlerBeskrivelse =
            utledTilordnetSaksbehandlerBeskrivelse(oppgave, "")
        val kommentarRad = "Kommentar: ${settPåVent.kommentar}"
        return utledBeskrivelsePrefix(tidspunkt) +
            utledOppgavefristBeskrivelse(oppgave, settPåVent.frist).påNyRadEllerTomString() +
            tilordnetSaksbehandlerBeskrivelse.påNyRadEllerTomString() +
            kommentarRad.påNyRadEllerTomString() +
            nåværendeBeskrivelse(oppgave)
    }

    fun oppdaterSettPåVent(
        oppgave: Oppgave,
        oppdaterPåVent: OppdaterPåVentRequest,
        tidspunkt: LocalDateTime = osloNow(),
    ): String {
        val fristBeskrivelse = utledOppgavefristBeskrivelse(oppgave, oppdaterPåVent.frist)
        val kommentarRad = "Kommentar: ${oppdaterPåVent.kommentar}"
        if (fristBeskrivelse.isEmpty() && kommentarRad.isEmpty()) {
            return oppgave.beskrivelse ?: ""
        }
        val tilordnetSaksbehandlerBeskrivelse =
            utledTilordnetSaksbehandlerBeskrivelse(oppgave, "")
        return utledBeskrivelsePrefix(tidspunkt) +
            fristBeskrivelse.påNyRadEllerTomString() +
            tilordnetSaksbehandlerBeskrivelse.påNyRadEllerTomString() +
            kommentarRad.påNyRadEllerTomString() +
            nåværendeBeskrivelse(oppgave)
    }

    fun taAvVent(
        oppgave: Oppgave,
        settPåVent: TaAvVentRequest,
        tidspunkt: LocalDateTime = osloNow(),
    ): String {
        val tilordnetSaksbehandlerBeskrivelse =
            utledTilordnetSaksbehandlerBeskrivelse(oppgave, SikkerhetsContext.hentSaksbehandlerEllerSystembruker())
        val kommentarRad = "Kommentar: ${settPåVent.kommentar}"
        return utledBeskrivelsePrefix(tidspunkt) +
            "\nTatt av vent" +
            tilordnetSaksbehandlerBeskrivelse.påNyRadEllerTomString() +
            kommentarRad.påNyRadEllerTomString() +
            nåværendeBeskrivelse(oppgave)
    }

    private fun String?.påNyRadEllerTomString(): String = this?.trim()?.takeIf { it.isNotBlank() }?.let { "\n$it" } ?: ""

    private fun nåværendeBeskrivelse(oppgave: Oppgave): String =
        if (oppgave.beskrivelse.isNullOrBlank()) {
            ""
        } else {
            "\n\n${oppgave.beskrivelse}"
        }

    private fun utledBeskrivelsePrefix(tidspunkt: LocalDateTime): String {
        val innloggetSaksbehandlerIdent = SikkerhetsContext.hentSaksbehandlerEllerSystembruker()
        val saksbehandlerNavn = SikkerhetsContext.hentSaksbehandlerNavn(strict = false)

        return "--- ${tidspunkt.medGosysTid()} $saksbehandlerNavn ($innloggetSaksbehandlerIdent) ---"
    }

    private fun utledOppgavefristBeskrivelse(
        oppgave: Oppgave,
        frist: LocalDate,
    ): String {
        val eksisterendeFrist = oppgave.fristFerdigstillelse?.norskFormat() ?: "<ingen>"
        val fristNorskFormat = frist.norskFormat()
        return if (eksisterendeFrist == fristNorskFormat) "" else "Oppgave endret frist fra $eksisterendeFrist til $fristNorskFormat"
    }

    private fun utledTilordnetSaksbehandlerBeskrivelse(
        oppgave: Oppgave,
        tilordnetRessurs: String,
    ): String {
        val eksisterendeSaksbehandler = oppgave.tilordnetRessurs ?: "<ingen>"
        val nySaksbehandler = if (tilordnetRessurs == "") "<ingen>" else tilordnetRessurs

        return if (eksisterendeSaksbehandler == nySaksbehandler) {
            ""
        } else {
            "Oppgave flyttet fra saksbehandler $eksisterendeSaksbehandler til ${nySaksbehandler}\n"
        }
    }
}
