package no.nav.tilleggsstonader.integrasjoner.fullmakt

import no.nav.tilleggsstonader.kontrakter.felles.IdentRequest

data class FullmaktIdentRequest(
    private val ident: String,
)

fun IdentRequest.tilFullmaktIdentRequest() = FullmaktIdentRequest(ident = this.ident)
