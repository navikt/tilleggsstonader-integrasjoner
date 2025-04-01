package no.nav.tilleggsstonader.integrasjoner.aktiviteter

import no.nav.tilleggsstonader.integrasjoner.arena.ArenaService
import no.nav.tilleggsstonader.kontrakter.aktivitet.AktivitetArenaDto
import no.nav.tilleggsstonader.kontrakter.felles.Stønadstype
import no.nav.tilleggsstonader.libs.utils.osloDateNow
import org.springframework.stereotype.Service

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
        return arenaService.hentAktiviteter(ident, fom, tom)
    }
}
