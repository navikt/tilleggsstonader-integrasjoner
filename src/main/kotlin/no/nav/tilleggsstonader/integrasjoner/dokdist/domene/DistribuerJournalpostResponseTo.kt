package no.nav.tilleggsstonader.integrasjoner.dokdist.domene

data class DistribuerJournalpostResponseTo(
    val bestillingsId: String,
)

class DokdistConflictException(
    val response: DistribuerJournalpostResponseTo,
) : RuntimeException()
