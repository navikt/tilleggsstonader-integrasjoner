package no.nav.tilleggsstonader.integrasjoner.oppgave

data class OppdaterBehandlingstypeRequest(
    val versjon: Int,
    val behandlingstype: String?,
    val endretAvEnhetsnr: String?,
)
