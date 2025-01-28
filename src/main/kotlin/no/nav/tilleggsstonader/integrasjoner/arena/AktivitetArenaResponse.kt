package no.nav.tilleggsstonader.integrasjoner.arena

import java.math.BigDecimal
import java.time.LocalDate

data class AktivitetArenaResponse(
    val aktivitetId: String,
    val aktivitetstype: String,
    val aktivitetsnavn: String,
    val periode: PeriodeArena,
    val antallDagerPerUke: Int?,
    val prosentAktivitetsdeltakelse: BigDecimal?,
    val aktivitetsstatus: String?,
    val aktivitetsstatusnavn: String?,
    val erStoenadsberettigetAktivitet: Boolean?,
    val erUtdanningsaktivitet: Boolean?,
    val arrangoer: String?,
) {
    val status: StatusAktivitetArena? get() =
        aktivitetsstatus
            ?.takeIf { it.isNotBlank() }
            ?.let { StatusAktivitetArena.valueOf(it) }
}

/**
 * @param [fom] og [tom] kan være snudd på i tilfeller de har opphørt aktiviteten. Uklart når dette skjer.
 */
data class PeriodeArena(
    val fom: LocalDate?,
    val tom: LocalDate?,
)

/**
 * tabell i Arena: aktivitetstatus
 * Status   antall
 * AKTUL	211
 * AVBR	    2
 * BEHOV	7891
 * DLTAV	35900
 * FULLF	226621
 * GJENN	13458
 * GJNAV	13243
 * JATLB	4
 * OPPHO	1
 * OVERF	2
 * TILBU	121
 * VENTL	2
 * null	    1693
 */
enum class StatusAktivitetArena(
    val beskrivelse: String,
) {
    AKTUL("Aktuell"),
    AVBR("Avbrutt"),
    BEHOV("Behov"),
    DLTAV("Avbrutt deltakelse"),
    FRAF("Frafalt"),
    FULLF("Fullført"),
    GJENN("Gjennomføres"),
    GJNAV("Avbrutt gjennomføring"),
    JATLB("Takket ja til tilbud"),
    OPPHO("Opphørt"), // Angir at aktiviteten er opphørt som følge av innføring av aktivitetsplan i ny flate
    OVERF("Overført"), // Angir at aktiviteten er overført til aktivitetsplan i ny flate
    PLAN("Planlagt"),
    TILBU("Godkjent tiltaksplass"),
    VENTL("Venteliste"),
}
