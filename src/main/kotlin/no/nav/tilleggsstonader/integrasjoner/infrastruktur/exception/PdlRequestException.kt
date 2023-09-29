package no.nav.tilleggsstonader.integrasjoner.infrastruktur.exception

open class PdlRequestException(melding: String? = null) : Exception(melding)

class PdlNotFoundException : PdlRequestException()
