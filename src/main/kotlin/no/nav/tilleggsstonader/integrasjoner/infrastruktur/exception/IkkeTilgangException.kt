package no.nav.tilleggsstonader.integrasjoner.infrastruktur.exception

data class IkkeTilgangException(
    override val message: String,
) : RuntimeException(message)
