package no.nav.tilleggsstonader.integrasjoner.ytelse

import no.nav.tilleggsstonader.integrasjoner.aap.AAPClient
import no.nav.tilleggsstonader.integrasjoner.ensligforsørger.EnsligForsørgerClient
import no.nav.tilleggsstonader.integrasjoner.infrastruktur.config.getValue
import no.nav.tilleggsstonader.kontrakter.ytelse.PeriodeArbeidsavklaringspenger
import no.nav.tilleggsstonader.kontrakter.ytelse.PeriodeEnsligForsørger
import no.nav.tilleggsstonader.kontrakter.ytelse.PerioderArbeidsavklaringspenger
import no.nav.tilleggsstonader.kontrakter.ytelse.PerioderEnsligForsørger
import no.nav.tilleggsstonader.kontrakter.ytelse.YtelsePerioderDto
import no.nav.tilleggsstonader.libs.log.SecureLogger.secureLogger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Service

@Service
class YtelseService(
    private val aapClient: AAPClient,
    private val ensligForsørgerClient: EnsligForsørgerClient,
    @Qualifier("shortCache")
    private val cacheManager: CacheManager,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun hentYtelser(data: HentYtelserData): YtelsePerioderDto {
        val arbeidsavklaringspenger = PerioderArbeidsavklaringspenger(suksess = false, perioder = emptyList())
        return YtelsePerioderDto(
            arbeidsavklaringspenger = arbeidsavklaringspenger, // hentAap(ident, fom, tom),
            ensligForsørger = hentEnslig(data),
        )
    }

    private fun hentAap(data: HentYtelserData): PerioderArbeidsavklaringspenger {
        return try {
            val perioder = cacheManager.getValue("ytelser-aap", data) {
                aapClient.hentPerioder(data.ident, fom = data.fom, tom = data.tom)
            }
            PerioderArbeidsavklaringspenger(
                suksess = true,
                perioder = perioder.perioder
                    .sortedByDescending { it.fraOgMedDato }
                    .map { PeriodeArbeidsavklaringspenger(fom = it.fraOgMedDato, tom = it.tilOgMedDato) },
            )
        } catch (e: Exception) {
            logError("aap", data, e)
            PerioderArbeidsavklaringspenger(suksess = false, perioder = emptyList())
        }
    }

    private fun hentEnslig(data: HentYtelserData): PerioderEnsligForsørger {
        return try {
            val perioder = cacheManager.getValue("ytelser-enslig", data) {
                ensligForsørgerClient.hentPerioder(data.ident, fom = data.fom, tom = data.tom)
            }
            PerioderEnsligForsørger(
                suksess = true,
                perioder = perioder.data.perioder
                    .sortedByDescending { it.fomDato }
                    .map { PeriodeEnsligForsørger(fom = it.fomDato, tom = it.tomDato) },
            )
        } catch (e: Exception) {
            logError("enslig forsørger", data, e)
            PerioderEnsligForsørger(suksess = false, perioder = emptyList())
        }
    }

    private fun logError(system: String, data: HentYtelserData, e: Exception) {
        val logmsg = "Feilet henting av perioder fra $system"
        logger.error("$logmsg, se secure logs for mer info")
        secureLogger.error("$logmsg ident=${data.ident} fom=${data.fom} tom=${data.tom}", e)
    }
}
