package no.nav.tilleggsstonader.integrasjoner.saksbehandler

import no.nav.tilleggsstonader.kontrakter.felles.Saksbehandler
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class SaksbehandlerService(
    private val azureGraphRestClient: AzureGraphRestClient,
    private val environment: Environment,
) {
    private val lengdeNavIdent = 7

    fun hentSaksbehandler(id: String): Saksbehandler {
        // TODO: Midlertidig for å få ut funksjonalitet. Fjern når ba-sak-e2e fases ut.
        if (environment.activeProfiles.any { it == "e2e" }) {
            return Saksbehandler(
                azureId = UUID.randomUUID(),
                navIdent = id,
                fornavn = "Mocka",
                etternavn = "Saksbehandler",
                enhet = "4402",
            )
        }

        if (id == ID_VEDTAKSLØSNINGEN) {
            return Saksbehandler(
                UUID.randomUUID(),
                ID_VEDTAKSLØSNINGEN,
                "Vedtaksløsning",
                "Nav",
                "9999",
            )
        }

        val azureAdBruker =
            if (id.length == lengdeNavIdent) {
                val azureAdBrukere = azureGraphRestClient.finnSaksbehandler(id)

                if (azureAdBrukere.value.size != 1) {
                    error("Feil ved søk. Oppslag på navIdent $id returnerte ${azureAdBrukere.value.size} forekomster.")
                }
                azureAdBrukere.value.first()
            } else {
                azureGraphRestClient.hentSaksbehandler(id)
            }

        return Saksbehandler(
            azureAdBruker.id,
            azureAdBruker.onPremisesSamAccountName,
            azureAdBruker.givenName,
            azureAdBruker.surname,
            azureAdBruker.streetAddress,
        )
    }

    fun hentNavIdent(saksbehandlerId: String): String =
        saksbehandlerId.takeIf { it.length == lengdeNavIdent } ?: hentSaksbehandler(saksbehandlerId).navIdent

    companion object {
        const val ID_VEDTAKSLØSNINGEN = "VL"
    }
}
