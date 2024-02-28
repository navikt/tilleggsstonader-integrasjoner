package no.nav.tilleggsstonader.integrasjoner.infrastruktur.exception

import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode

data class OppslagException(
    override val message: String?,
    val kilde: String,
    val level: Level,
    val httpStatus: HttpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR,
    val error: Throwable? = null,
    val sensitiveInfo: String? = null,
) : RuntimeException(message, error) {

    enum class Level {
        LAV,
        MEDIUM,
        KRITISK,
    }
}
