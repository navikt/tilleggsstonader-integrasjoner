package no.nav.tilleggsstonader.integrasjoner.ytelse

import no.nav.tilleggsstonader.integrasjoner.aap.AAPClient
import no.nav.tilleggsstonader.integrasjoner.ensligforsørger.EnsligForsørgerClient
import no.nav.tilleggsstonader.kontrakter.ytelse.PeriodeArbeidsavklaringspenger
import no.nav.tilleggsstonader.kontrakter.ytelse.PeriodeEnsligForsørger
import no.nav.tilleggsstonader.kontrakter.ytelse.PerioderArbeidsavklaringspenger
import no.nav.tilleggsstonader.kontrakter.ytelse.PerioderEnsligForsørger
import no.nav.tilleggsstonader.kontrakter.ytelse.YtelsePerioderDto
import no.nav.tilleggsstonader.libs.log.SecureLogger.secureLogger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class YtelseService(
    private val aapClient: AAPClient,
    private val ensligForsørgerClient: EnsligForsørgerClient,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun hentYtelser(ident: String, fom: LocalDate, tom: LocalDate): YtelsePerioderDto {
        return YtelsePerioderDto(
            arbeidsavklaringspenger = PerioderArbeidsavklaringspenger(suksess = false, perioder = emptyList()), // hentAap(ident, fom, tom),
            ensligForsørger = hentEnslig(ident, fom, tom),
        )
    }

    private fun hentAap(ident: String, fom: LocalDate, tom: LocalDate): PerioderArbeidsavklaringspenger {
        return try {
            val perioder = aapClient.hentPerioder(ident, fom, tom)
            PerioderArbeidsavklaringspenger(
                suksess = true,
                perioder = perioder.perioder
                    .sortedByDescending { it.fraOgMedDato }
                    .map {
                        PeriodeArbeidsavklaringspenger(fom = it.fraOgMedDato, tom = it.tilOgMedDato)
                    },
            )
        } catch (e: Exception) {
            logger.error("Feilet henting av perioder fra aap, se secure logs for mer info")
            secureLogger.error("Feilet henting av perioder fra aap ident=$ident fom=$fom tom=$tom", e)
            PerioderArbeidsavklaringspenger(suksess = false, perioder = emptyList())
        }
    }

    private fun hentEnslig(ident: String, fom: LocalDate, tom: LocalDate): PerioderEnsligForsørger {
        return try {
            val perioder = ensligForsørgerClient.hentPerioder(ident, fom, tom)
            PerioderEnsligForsørger(
                suksess = true,
                perioder = perioder.perioder
                    .sortedByDescending { it.fomDato }
                    .map { PeriodeEnsligForsørger(fom = it.fomDato, tom = it.tomDato) },
            )
        } catch (e: Exception) {
            logger.error("Feilet henting av perioder fra enslig forsørger, se secure logs for mer info")
            secureLogger.error("Feilet henting av perioder fra enslig forsørger ident=$ident fom=$fom tom=$tom", e)
            PerioderEnsligForsørger(suksess = false, perioder = emptyList())
        }
    }
}
