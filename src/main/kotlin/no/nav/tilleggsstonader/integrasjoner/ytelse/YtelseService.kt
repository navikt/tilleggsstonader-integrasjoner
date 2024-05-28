package no.nav.tilleggsstonader.integrasjoner.ytelse

import no.nav.tilleggsstonader.integrasjoner.aap.AAPClient
import no.nav.tilleggsstonader.integrasjoner.ensligforsørger.EnsligForsørgerClient
import no.nav.tilleggsstonader.integrasjoner.infrastruktur.config.getValue
import no.nav.tilleggsstonader.kontrakter.ytelse.HentetInformasjon
import no.nav.tilleggsstonader.kontrakter.ytelse.StatusHentetInformasjon
import no.nav.tilleggsstonader.kontrakter.ytelse.TypeYtelsePeriode
import no.nav.tilleggsstonader.kontrakter.ytelse.YtelsePeriode
import no.nav.tilleggsstonader.kontrakter.ytelse.YtelsePerioderDto
import no.nav.tilleggsstonader.kontrakter.ytelse.YtelsePerioderRequest
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

    fun hentYtelser(request: YtelsePerioderRequest): YtelsePerioderDto {
        val perioder = mutableListOf<YtelsePeriode>()
        val hentetInformasjon = mutableListOf<HentetInformasjon>()

        val data = HentYtelserCacheData(ident = request.ident, fom = request.fom, tom = request.tom)
        request.typer.distinct().forEach {
            try {
                perioder.addAll(hentPerioder(it, data))
                hentetInformasjon.add(HentetInformasjon(type = it, status = StatusHentetInformasjon.OK))
            } catch (e: Exception) {
                hentetInformasjon.add(HentetInformasjon(type = it, status = StatusHentetInformasjon.FEILET))
                logError(it, data, e)
            }
        }
        return YtelsePerioderDto(
            perioder = perioder.sortedByDescending { it.tom },
            hentetInformasjon = hentetInformasjon,
        )
    }

    private fun hentPerioder(
        it: TypeYtelsePeriode,
        data: HentYtelserCacheData,
    ): List<YtelsePeriode> {
        return when (it) {
            TypeYtelsePeriode.AAP -> hentAap(data)
            TypeYtelsePeriode.ENSLIG_FORSØRGER -> hentEnslig(data)
        }
    }

    private fun hentAap(data: HentYtelserCacheData): List<YtelsePeriode> {
        val perioder = cacheManager.getValue("ytelser-aap", data) {
            aapClient.hentPerioder(data.ident, fom = data.fom, tom = data.tom)
        }
        return perioder.perioder.map {
            YtelsePeriode(
                type = TypeYtelsePeriode.AAP,
                fom = it.fraOgMedDato,
                tom = it.tilOgMedDato,
            )
        }
    }

    private fun hentEnslig(data: HentYtelserCacheData): List<YtelsePeriode> {
        val perioder = cacheManager.getValue("ytelser-enslig", data) {
            ensligForsørgerClient.hentPerioder(data.ident, fom = data.fom, tom = data.tom)
        }
        return perioder.data.perioder.map {
            YtelsePeriode(
                type = TypeYtelsePeriode.ENSLIG_FORSØRGER,
                fom = it.fomDato,
                tom = it.tomDato,
            )
        }
    }

    private fun logError(type: TypeYtelsePeriode, data: HentYtelserCacheData, e: Exception) {
        val logMsg = "Feilet henting av perioder fra $type"
        logger.error("$logMsg, se secure logs for mer info")
        secureLogger.error("$logMsg ident=${data.ident} fom=${data.fom} tom=${data.tom}", e)
    }
}
