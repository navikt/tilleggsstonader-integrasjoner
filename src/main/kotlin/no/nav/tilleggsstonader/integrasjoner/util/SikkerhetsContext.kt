package no.nav.tilleggsstonader.integrasjoner.util

import no.nav.security.token.support.core.context.TokenValidationContext
import no.nav.security.token.support.spring.SpringTokenValidationContextHolder
import no.nav.tilleggsstonader.libs.log.SecureLogger.secureLogger

object SikkerhetsContext {
    private const val SYSTEM_NAVN = "System"
    const val SYSTEM_FORKORTELSE = "VL"

    fun hentSaksbehandler(): String {
        val result = hentSaksbehandlerEllerSystembruker()

        if (result == SYSTEM_FORKORTELSE) {
            error("Finner ikke NAVident i token")
        }
        return result
    }

    fun kallKommerFra(vararg eksternApplikasjon: EksternApplikasjon): Boolean {
        val applikasjonsnavn = applikasjonsnavnFraToken()
        secureLogger.info("Applikasjonsnavn: $applikasjonsnavn")
        return eksternApplikasjon.any { applikasjonsnavn.contains(it.namespaceAppNavn) }
    }

    fun erKallFraTilleggsstønader(): Boolean {
        val applikasjonsnavn = applikasjonsnavnFraToken()
        return applikasjonsnavn.contains("gcp:tilleggsstonader:tilleggsstonader-")
    }

    fun applikasjonsnavnFraToken(): String {
        val tokenValidationContext = SpringTokenValidationContextHolder().getTokenValidationContext()
        if (tokenValidationContext.hasTokenFor("azuread")) {
            val claims = tokenValidationContext.getClaims("azuread")
            return claims.get("azp_name")?.toString() ?: ""
        } else if (tokenValidationContext.hasTokenFor("tokenx")) {
            val claims = tokenValidationContext.getClaims("tokenx")
            return claims.get("client_id")?.toString() ?: ""
        } else {
            error("Finner ikke gyldig token blant issues=${tokenValidationContext.issuers}")
        }
    }

    fun hentSaksbehandlerEllerSystembruker() =
        Result
            .runCatching { SpringTokenValidationContextHolder().getTokenValidationContext() }
            .fold(
                onSuccess = {
                    it.getClaim("NAVident")?.toString() ?: SYSTEM_FORKORTELSE
                },
                onFailure = { SYSTEM_FORKORTELSE },
            )

    fun hentSaksbehandlerNavn(strict: Boolean = false): String =
        Result
            .runCatching { SpringTokenValidationContextHolder().getTokenValidationContext() }
            .fold(
                onSuccess = {
                    it.getClaim("name")?.toString()
                        ?: if (strict) error("Finner ikke navn i azuread token") else SYSTEM_NAVN
                },
                onFailure = { if (strict) error("Finner ikke navn på innlogget bruker") else SYSTEM_NAVN },
            )

    private fun TokenValidationContext.getClaim(name: String) = this.getJwtToken("azuread")?.jwtTokenClaims?.get(name)
}
