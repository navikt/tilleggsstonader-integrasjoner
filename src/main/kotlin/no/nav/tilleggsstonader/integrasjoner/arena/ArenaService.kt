package no.nav.tilleggsstonader.integrasjoner.arena

import no.nav.tilleggsstonader.kontrakter.aktivitet.AktivitetDto
import no.nav.tilleggsstonader.libs.log.SecureLogger.secureLogger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class ArenaService(
    private val arenaClient: ArenaClient,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun hentAktiviteter(ident: String, fom: LocalDate, tom: LocalDate?): List<AktivitetDto> {
        val aktiviteter = arenaClient.hentAktiviteter(ident, fom, tom)

        return aktiviteter.mapNotNull {
            try {
                AktivitetDtoMapper.map(it)
            } catch (e: Exception) {
                logger.error("Feilet mapping av aktivitet, se secure logs for mer info")
                secureLogger.error("Feilet mapping av aktivitet, $it")
                null
            }
        }
    }
}
