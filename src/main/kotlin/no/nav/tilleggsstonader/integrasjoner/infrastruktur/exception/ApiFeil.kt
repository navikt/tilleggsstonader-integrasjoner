package no.nav.tilleggsstonader.integrasjoner.infrastruktur.exception

import org.springframework.http.HttpStatus

/**
 * Brukes primært som feil som er årsaket av en saksbehandler, som logges som info, og feil blir logge i vanlig logg
 */
open class ApiFeil(
    val feil: String,
    val frontendFeilmelding: String = feil,
    val httpStatus: HttpStatus,
) : RuntimeException(feil) {
    constructor(feil: String, httpStatus: HttpStatus) :
        this(feil = feil, frontendFeilmelding = feil, httpStatus = httpStatus)
}
