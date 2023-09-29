package no.nav.tilleggsstonader.integrasjoner.dokdist.domene

import no.nav.tilleggsstonader.kontrakter.dokdist.Distribusjonstidspunkt
import no.nav.tilleggsstonader.kontrakter.dokdist.Distribusjonstype

data class DistribuerJournalpostRequestTo(
    val journalpostId: String,
    val batchId: String? = null,
    val bestillendeFagsystem: String,
    val adresse: AdresseTo? = null,
    val dokumentProdApp: String,
    val distribusjonstype: Distribusjonstype?,
    val distribusjonstidspunkt: Distribusjonstidspunkt,
)
