package no.nav.tilleggsstonader.integrasjoner.saksbehandler

import no.nav.tilleggsstonader.integrasjoner.azure.client.AzureGraphRestClient
import no.nav.tilleggsstonader.kontrakter.felles.Saksbehandler
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class SaksbehandlerService(
    private val azureGraphRestClient: AzureGraphRestClient,
) {
    private val lengdeNavIdent = 7

    fun hentSaksbehandler(id: String): Saksbehandler {
        // håntering av at operasjoner kan utføres av saksbehandlingsløsningen selv, og dermed ikke har ID i azure-ad
        if (id == ID_VEDTAKSLØSNINGEN) {
            return Saksbehandler(
                azureId = UUID.randomUUID(),
                navIdent = ID_VEDTAKSLØSNINGEN,
                fornavn = "Tilleggsstønader Vedtaksløsning",
                etternavn = "Nav",
                enhet = "9999",
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
            azureId = azureAdBruker.id,
            navIdent = azureAdBruker.onPremisesSamAccountName,
            fornavn = azureAdBruker.givenName,
            etternavn = azureAdBruker.surname,
            enhet = azureAdBruker.streetAddress, // sic!
        )
    }

    companion object {
        const val ID_VEDTAKSLØSNINGEN = "VL"
    }
}
