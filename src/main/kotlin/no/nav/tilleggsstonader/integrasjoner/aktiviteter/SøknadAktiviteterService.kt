package no.nav.tilleggsstonader.integrasjoner.aktiviteter

import no.nav.tilleggsstonader.integrasjoner.arena.ArenaService
import no.nav.tilleggsstonader.kontrakter.aktivitet.AktivitetArenaDto
import no.nav.tilleggsstonader.kontrakter.aktivitet.GruppeAktivitet
import no.nav.tilleggsstonader.kontrakter.aktivitet.TypeAktivitet
import no.nav.tilleggsstonader.kontrakter.felles.Stønadstype
import no.nav.tilleggsstonader.libs.utils.osloDateNow
import org.springframework.stereotype.Service
import kotlin.collections.filter

@Service
class SøknadAktiviteterService(
    private val arenaService: ArenaService,
) {
    fun hentAktiviteter(
        ident: String,
        stønadstype: Stønadstype,
    ): List<AktivitetArenaDto> {
        val fom = osloDateNow().minusMonths(stønadstype.grunnlagAntallMånederBakITiden.toLong())
        val tom = osloDateNow().plusMonths(3)
        return arenaService
            .hentAktiviteter(ident, fom, tom)
            .filter {
                // Ikke alle aktiviteter har fått flagg "stønadsberettiget" i Arena selv om de skulle hatt det, så vi trenger en ekstra sjekk på gruppe
                // Det er alltid gruppe=TILTAK når erStønadsberettiget = true, men ikke alle tiltak er stønadsberettiget
                it.erStønadsberettiget == true || TypeAktivitet.valueOf(it.type).gruppe == GruppeAktivitet.TLTAK
            }
    }
}
