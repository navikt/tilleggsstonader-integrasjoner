package no.nav.tilleggsstonader.integrasjoner.arena

import no.nav.tilleggsstonader.kontrakter.aktivitet.AktivitetArenaDto
import no.nav.tilleggsstonader.kontrakter.felles.ObjectMapperProvider.objectMapper
import no.nav.tilleggsstonader.kontrakter.målgruppe.MålgruppeArenaDto
import no.nav.tilleggsstonader.kontrakter.målgruppe.TypeMålgruppe
import no.nav.tilleggsstonader.libs.log.SecureLogger.secureLogger
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class ArenaService(
    private val arenaClient: ArenaClient,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Cacheable("aktiviteter", cacheManager = "shortCache")
    fun hentAktiviteter(
        ident: String,
        fom: LocalDate,
        tom: LocalDate,
    ): List<AktivitetArenaDto> {
        val aktiviteter = arenaClient.hentAktiviteter(ident, fom, tom)

        return aktiviteter.mapNotNull {
            try {
                AktivitetDtoMapper.map(it)
            } catch (e: Exception) {
                logger.error("Feilet mapping av aktivitet, se secure logs for mer info")
                secureLogger.error("Feilet mapping av aktivitet, ${objectMapper.writeValueAsString(it)}", e)
                null
            }
        }
    }

    @Cacheable("målgrupper", cacheManager = "shortCache")
    fun hentMålgrupper(
        ident: String,
        fom: LocalDate,
        tom: LocalDate,
    ): List<MålgruppeArenaDto> {
        val målgrupper = arenaClient.hentMålgrupper(ident, fom, tom)

        return målgrupper.mapNotNull {
            try {
                MålgruppeArenaDto(
                    fom = it.gyldighetsperiode.fom,
                    tom = it.gyldighetsperiode.tom,
                    type = it.maalgruppetype.tilType(),
                    arenaType = it.maalgruppetype.name,
                    arenaTypeNavn = it.maalgruppenavn,
                )
            } catch (e: Exception) {
                logger.error("Feilet mapping av målgruppe, se secure logs for mer info")
                secureLogger.error("Feilet mapping av målgruppe, ${objectMapper.writeValueAsString(it)}", e)
                null
            }
        }
    }

    private fun TypeMålgruppeArena.tilType(): TypeMålgruppe =
        when (this) {
            TypeMålgruppeArena.NEDSARBEVN -> TypeMålgruppe.NEDSATT_ARBEIDSEVNE
            TypeMålgruppeArena.ENSFORUTD -> TypeMålgruppe.ENSLIG_FORSØRGER
            TypeMålgruppeArena.ENSFORARBS -> TypeMålgruppe.ENSLIG_FORSØRGER
            TypeMålgruppeArena.TIDLFAMPL -> TypeMålgruppe.TIDLIGERE_FAMILIEPLEIER
            TypeMålgruppeArena.GJENEKUTD -> TypeMålgruppe.GJENLEVENDE_EKTEFELLE
            TypeMålgruppeArena.GJENEKARBS -> TypeMålgruppe.GJENLEVENDE_EKTEFELLE
            TypeMålgruppeArena.MOTTILTPEN -> TypeMålgruppe.TILTAKSPENGER
            TypeMålgruppeArena.ARBSOKERE -> TypeMålgruppe.ARBEIDSSØKER
            TypeMålgruppeArena.MOTDAGPEN -> TypeMålgruppe.DAGPENGER
        }
}
