package no.nav.tilleggsstonader.integrasjoner.dokarkiv.client

import no.nav.tilleggsstonader.kontrakter.dokarkiv.ArkiverDokumentResponse

class KanIkkeFerdigstilleJournalpostException(
    message: String?,
) : RuntimeException(message)

class DokarkivConflictException(
    val response: ArkiverDokumentResponse,
) : RuntimeException()
