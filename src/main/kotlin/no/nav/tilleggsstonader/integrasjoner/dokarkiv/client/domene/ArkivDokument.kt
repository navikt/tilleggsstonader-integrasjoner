package no.nav.tilleggsstonader.integrasjoner.dokarkiv.client.domene

import no.nav.tilleggsstonader.integrasjoner.dokarkiv.metadata.Dokumentkategori

class ArkivDokument(
    val tittel: String? = null,
    val brevkode: String? = null,
    val dokumentKategori: Dokumentkategori? = null,
    val dokumentvarianter: List<Dokumentvariant> = ArrayList(),
)
