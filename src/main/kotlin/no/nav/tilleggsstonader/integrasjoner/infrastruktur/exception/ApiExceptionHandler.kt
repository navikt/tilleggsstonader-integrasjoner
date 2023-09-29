package no.nav.tilleggsstonader.integrasjoner.infrastruktur.exception

import no.nav.security.token.support.core.exceptions.JwtTokenMissingException
import no.nav.tilleggsstonader.libs.log.SecureLogger
import org.slf4j.LoggerFactory
import org.springframework.core.NestedExceptionUtils
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import java.net.SocketTimeoutException
import java.util.concurrent.TimeoutException

@ControllerAdvice
class ApiExceptionHandler {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val secureLogger = SecureLogger.secureLogger

    @ExceptionHandler(Throwable::class)
    fun handleThrowable(throwable: Throwable): ProblemDetail {
        val responseStatus = throwable::class.annotations.find { it is ResponseStatus }
            ?.let { it as ResponseStatus }
            ?.value
            ?: HttpStatus.INTERNAL_SERVER_ERROR

        val metodeSomFeiler = finnMetodeSomFeiler(throwable)

        val mostSpecificCause = throwable.getMostSpecificCause()
        if (mostSpecificCause is SocketTimeoutException || mostSpecificCause is TimeoutException) {
            secureLogger.warn(
                "Timeout feil: ${mostSpecificCause.message}, $metodeSomFeiler ${rootCause(throwable)}",
                throwable,
            )
            logger.warn("Timeout feil: $metodeSomFeiler ${rootCause(throwable)} ")
            return lagTimeoutfeilRessurs()
        }

        secureLogger.error("Uventet feil: $metodeSomFeiler ${rootCause(throwable)}", throwable)
        logger.error("Uventet feil: $metodeSomFeiler ${rootCause(throwable)} ")

        return ProblemDetail.forStatusAndDetail(responseStatus, "Ukjent feil")
    }

    @ExceptionHandler(OppslagException::class)
    fun handleOppslagException(e: OppslagException): ProblemDetail {
        var feilmelding = "[${e.kilde}][${e.message}]"
        var sensitivFeilmelding = feilmelding
        if (!e.sensitiveInfo.isNullOrEmpty()) {
            sensitivFeilmelding += "[${e.sensitiveInfo}]"
        }
        if (e.error != null) {
            feilmelding += "[${e.error.javaClass.name}]"
            sensitivFeilmelding += "[${e.error.javaClass.name}]"
        }
        when (e.level) {
            OppslagException.Level.KRITISK -> {
                secureLogger.error("OppslagException : $sensitivFeilmelding", e.error)
                logger.error("OppslagException : $feilmelding")
            }

            OppslagException.Level.MEDIUM -> {
                secureLogger.warn("OppslagException : $sensitivFeilmelding", e.error)
                logger.warn("OppslagException : $feilmelding")
            }

            else -> logger.info("OppslagException : $feilmelding")
        }
        return ProblemDetail.forStatusAndDetail(e.httpStatus, feilmelding)
    }

    @ExceptionHandler(JwtTokenMissingException::class)
    fun handleJwtTokenMissingException(jwtTokenMissingException: JwtTokenMissingException): ProblemDetail {
        return ProblemDetail.forStatusAndDetail(
            HttpStatus.UNAUTHORIZED,
            "En uventet feil oppstod: Kall ikke autorisert",
        )
    }

    @ExceptionHandler(PdlNotFoundException::class)
    fun handleThrowable(feil: PdlNotFoundException): ProblemDetail {
        logger.warn("Finner ikke personen i PDL")
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Finner ingen personer for valgt personident")
    }

    private fun lagTimeoutfeilRessurs(): ProblemDetail = ProblemDetail.forStatusAndDetail(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "Kommunikasjonsproblemer med andre systemer - prøv igjen",
    )

    fun finnMetodeSomFeiler(e: Throwable): String {
        val firstElement = e.stackTrace.firstOrNull {
            it.className.startsWith("no.nav.tilleggsstonader.sak") &&
                !it.className.contains("$") &&
                !it.className.contains("InsertUpdateRepositoryImpl")
        }
        if (firstElement != null) {
            val className = firstElement.className.split(".").lastOrNull()
            return "$className::${firstElement.methodName}(${firstElement.lineNumber})"
        }
        return e.cause?.let { finnMetodeSomFeiler(it) } ?: "(Ukjent metode som feiler)"
    }

    private fun rootCause(throwable: Throwable): String {
        return throwable.getMostSpecificCause().javaClass.simpleName
    }

    private fun Throwable.getMostSpecificCause(): Throwable {
        return NestedExceptionUtils.getMostSpecificCause(this)
    }
}
