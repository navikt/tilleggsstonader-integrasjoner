package no.nav.tilleggsstonader.integrasjoner.ytelse

import no.nav.tilleggsstonader.integrasjoner.aap.AAPClient
import no.nav.tilleggsstonader.integrasjoner.ensligforsørger.EnsligForsørgerClient
import no.nav.tilleggsstonader.integrasjoner.etterlatte.EtterlatteClient
import no.nav.tilleggsstonader.integrasjoner.infrastruktur.config.getValue
import no.nav.tilleggsstonader.integrasjoner.util.VirtualThreadUtil.parallelt
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
    private val etterlatteClient: EtterlatteClient,
    @Qualifier("shortCache")
    private val cacheManager: CacheManager,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    // TODO fjern filter når AAP har lagt til mulighet for å hente perioder i prod
    fun hentYtelser(request: YtelsePerioderRequest): YtelsePerioderDto {
        val perioder = mutableListOf<YtelsePeriode>()
        val hentetInformasjon = mutableListOf<HentetInformasjon>()

        val data = HentYtelserCacheData(ident = request.ident, fom = request.fom, tom = request.tom)
        request.typer.distinct()
            .map { hentPeriodeFn(it, data) }
            .parallelt()
            .forEach {
                perioder.addAll(it.first)
                hentetInformasjon.add(it.second)
            }

        return YtelsePerioderDto(
            perioder = perioder.sortedByDescending { it.tom },
            hentetInformasjon = hentetInformasjon,
        )
    }

    private fun hentPeriodeFn(
        it: TypeYtelsePeriode,
        data: HentYtelserCacheData,
    ): () -> Pair<List<YtelsePeriode>, HentetInformasjon> = {
        try {
            Pair(hentPerioder(it, data), HentetInformasjon(type = it, status = StatusHentetInformasjon.OK))
        } catch (e: Exception) {
            logError(it, data, e)
            Pair(emptyList(), HentetInformasjon(type = it, status = StatusHentetInformasjon.FEILET))
        }
    }

    private fun hentPerioder(
        it: TypeYtelsePeriode,
        data: HentYtelserCacheData,
    ): List<YtelsePeriode> {
        return when (it) {
            TypeYtelsePeriode.AAP -> hentAap(data)
            TypeYtelsePeriode.ENSLIG_FORSØRGER -> hentEnslig(data)
            TypeYtelsePeriode.OMSTILLINGSSTØNAD -> hentOmstillingsstønad(data)
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

    private fun hentOmstillingsstønad(data: HentYtelserCacheData): List<YtelsePeriode> {
        val perioder = cacheManager.getValue("ytelser-etterlatte", data) {
            etterlatteClient.hentPerioder(data.ident, fom = data.fom)
        }
        return perioder.flatMap { it.perioder }.map {
            YtelsePeriode(
                type = TypeYtelsePeriode.OMSTILLINGSSTØNAD,
                fom = it.fom,
                tom = it.tom,
            )
        }
    }

    private fun logError(type: TypeYtelsePeriode, data: HentYtelserCacheData, e: Exception) {
        val logMsg = "Feilet henting av perioder fra $type"
        logger.error("$logMsg, se secure logs for mer info")
        secureLogger.error("$logMsg ident=${data.ident} fom=${data.fom} tom=${data.tom}", e)
    }
}
