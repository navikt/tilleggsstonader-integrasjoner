package no.nav.tilleggsstonader.integrasjoner.arena

import java.time.LocalDate

data class MålgruppeArenaResponse(
    val gyldighetsperiode: Gyldighetsperiode,
    val maalgruppetype: TypeMålgruppeArena,
    val maalgruppenavn: String,
)

data class Gyldighetsperiode(
    val fom: LocalDate,
    val tom: LocalDate,
)

enum class TypeMålgruppeArena(
    val beskrivelse: String,
) {
    NEDSARBEVN("Person med nedsatt arbeidsevne pga. sykdom"),
    ENSFORUTD("Enslig forsørger under utdanning"),
    ENSFORARBS("Enslig forsørger som søker arbeid"),
    TIDLFAMPL("Tidligere familiepleier under utdanning"),
    GJENEKUTD("Gjenlevende ektefelle under utdanning"),
    GJENEKARBS("Gjenlevende ektefelle som søker arbeid"),
    MOTTILTPEN("Mottaker av tiltakspenger"),
    ARBSOKERE("Arbeidssøker"),
    MOTDAGPEN("Mottaker av dagpenger"),
}
