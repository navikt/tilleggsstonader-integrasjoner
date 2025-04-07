package no.nav.tilleggsstonader.integrasjoner.util

enum class EksternApplikasjon(
    val namespaceAppNavn: String,
) {
    SOKNAD_API("gcp:tilleggsstonader:tilleggsstonader-soknad-api"),

    FYLL_UT_SEND_INN_SØKNAD("gcp:skjemadigitalisering:skjemautfylling"),
}
