package no.nav.tilleggsstonader.integrasjoner.dokarkiv.metadata

import no.nav.tilleggsstonader.kontrakter.dokarkiv.Dokumenttype
import no.nav.tilleggsstonader.kontrakter.felles.Behandlingstema
import no.nav.tilleggsstonader.kontrakter.felles.Stønadstype
import no.nav.tilleggsstonader.kontrakter.felles.Tema
import no.nav.tilleggsstonader.kontrakter.sak.DokumentBrevkode
import org.springframework.stereotype.Component

@Component
data object BarnetilsynSøknadMetadata : SøknadMetadata(
    tema = Tema.TSO,
    behandlingstema = Behandlingstema.TilsynBarn,
    dokumenttype = Dokumenttype.BARNETILSYN_SØKNAD,
    tittel = "Søknad om ${Stønadstype.BARNETILSYN.visningsnavn}",
    brevkode = DokumentBrevkode.BARNETILSYN.verdi,
)
