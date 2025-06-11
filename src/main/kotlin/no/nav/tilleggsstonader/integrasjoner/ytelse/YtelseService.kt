package no.nav.tilleggsstonader.integrasjoner.ytelse

import no.nav.tilleggsstonader.integrasjoner.aap.AAPClient
import no.nav.tilleggsstonader.integrasjoner.dagpenger.DagpengerClient
import no.nav.tilleggsstonader.integrasjoner.ensligforsørger.EnsligForsørgerClient
import no.nav.tilleggsstonader.integrasjoner.etterlatte.EtterlatteClient
import no.nav.tilleggsstonader.integrasjoner.infrastruktur.config.getValue
import no.nav.tilleggsstonader.integrasjoner.tiltakspenger.TiltakspengerClient
import no.nav.tilleggsstonader.integrasjoner.util.VirtualThreadUtil.parallelt
import no.nav.tilleggsstonader.kontrakter.ytelse.EnsligForsørgerStønadstype
import no.nav.tilleggsstonader.kontrakter.ytelse.ResultatKilde
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
    private val dagpengerClient: DagpengerClient,
    private val ensligForsørgerClient: EnsligForsørgerClient,
    private val etterlatteClient: EtterlatteClient,
    private val tiltakspengerClient: TiltakspengerClient,
    @Qualifier("shortCache")
    private val cacheManager: CacheManager,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun hentYtelser(request: YtelsePerioderRequest): YtelsePerioderDto {
        val perioder = mutableListOf<YtelsePeriode>()
        val kildeResultat = mutableListOf<YtelsePerioderDto.KildeResultatYtelse>()

        val data = HentYtelserCacheData(ident = request.ident, fom = request.fom, tom = request.tom)
        request.typer
            .distinct()
            .map { hentPeriodeFn(it, data) }
            .parallelt()
            .forEach {
                perioder.addAll(it.first)
                kildeResultat.add(it.second)
            }

        return YtelsePerioderDto(
            perioder = perioder.sortedByDescending { it.tom },
            kildeResultat = kildeResultat,
        )
    }

    private fun hentPeriodeFn(
        it: TypeYtelsePeriode,
        data: HentYtelserCacheData,
    ): () -> Pair<List<YtelsePeriode>, YtelsePerioderDto.KildeResultatYtelse> =
        {
            try {
                Pair(
                    hentPerioder(it, data),
                    YtelsePerioderDto.KildeResultatYtelse(type = it, resultat = ResultatKilde.OK),
                )
            } catch (e: Exception) {
                logError(it, data, e)
                Pair(
                    emptyList(),
                    YtelsePerioderDto.KildeResultatYtelse(type = it, resultat = ResultatKilde.FEILET),
                )
            }
        }

    private fun hentPerioder(
        it: TypeYtelsePeriode,
        data: HentYtelserCacheData,
    ): List<YtelsePeriode> =
        when (it) {
            TypeYtelsePeriode.AAP -> hentAap(data)
            TypeYtelsePeriode.DAGPENGER -> hentDagpenger(data)
            TypeYtelsePeriode.ENSLIG_FORSØRGER -> hentEnslig(data)
            TypeYtelsePeriode.OMSTILLINGSSTØNAD -> hentOmstillingsstønad(data)
            TypeYtelsePeriode.TILTAKSPENGER -> hentTiltakspenger(data)
        }

    private fun hentAap(data: HentYtelserCacheData): List<YtelsePeriode> {
        val perioder =
            cacheManager.getValue("ytelser-aap", data) {
                aapClient.hentPerioder(data.ident, fom = data.fom, tom = data.tom)
            }
        return perioder.perioder.map {
            YtelsePeriode(
                type = TypeYtelsePeriode.AAP,
                fom = it.periode.fraOgMedDato,
                tom = it.periode.tilOgMedDato,
                aapErFerdigAvklart = it.aktivitetsfaseKode.equals("FA", ignoreCase = true), // FA = Ferdig avklart
            )
        }
    }

    private fun hentEnslig(data: HentYtelserCacheData): List<YtelsePeriode> {
        val perioder =
            cacheManager.getValue("ytelser-enslig", data) {
                ensligForsørgerClient.hentPerioder(data.ident, fom = data.fom, tom = data.tom)
            }
        return perioder.data.perioder.map {
            YtelsePeriode(
                type = TypeYtelsePeriode.ENSLIG_FORSØRGER,
                fom = it.fomDato,
                tom = it.tomDato,
                ensligForsørgerStønadstype = EnsligForsørgerStønadstype.valueOf(it.stønadstype.name),
            )
        }
    }

    private fun hentDagpenger(data: HentYtelserCacheData): List<YtelsePeriode> {
        val dagpengerResponse =
            cacheManager.getValue("ytelser-dagpenger", data) {
                dagpengerClient.hentPerioder(data.ident, fom = data.fom, tom = data.tom)
            }
        return dagpengerResponse.perioder.map { periode ->
            YtelsePeriode(
                type = TypeYtelsePeriode.DAGPENGER,
                fom = periode.fraOgMedDato,
                tom = periode.tilOgMedDato,
            )
        }
    }

    private fun hentTiltakspenger(data: HentYtelserCacheData): List<YtelsePeriode> {
        val tiltakspengerResponse =
            cacheManager.getValue("ytelser-tiltakspenger", data) {
                tiltakspengerClient.hentPerioder(data.ident, fom = data.fom, tom = data.tom)
            }
        return tiltakspengerResponse.map { it ->
            YtelsePeriode(
                type = TypeYtelsePeriode.TILTAKSPENGER,
                fom = it.periode.fraOgMed,
                tom = it.periode.tilOgMed,
            )
        }
    }

    private fun hentOmstillingsstønad(data: HentYtelserCacheData): List<YtelsePeriode> {
        val perioder =
            cacheManager.getValue("ytelser-etterlatte", data) {
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

    private fun logError(
        type: TypeYtelsePeriode,
        data: HentYtelserCacheData,
        e: Exception,
    ) {
        val logMsg = "Feilet henting av perioder fra $type"
        logger.error("$logMsg, se secure logs for mer info")
        secureLogger.error("$logMsg ident=${data.ident} fom=${data.fom} tom=${data.tom}", e)
    }
}
