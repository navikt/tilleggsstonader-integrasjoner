package no.nav.tilleggsstonader.integrasjoner.azure.client

import no.nav.familie.http.client.AbstractRestClient
import no.nav.tilleggsstonader.integrasjoner.azure.domene.AzureAdBruker
import no.nav.tilleggsstonader.integrasjoner.azure.domene.AzureAdBrukere
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

/**  TODO: Bruker familie sin AbstractRestClient og RestOperations for å få at kallet går igjennom til Azure
 * se favro for forklaring og hva problemet er https://favro.com/organization/98c34fb974ce445eac854de0/4d617346d79341c7fbd9a40a?card=NAV-21832
 */

@Service
class AzureGraphRestClient(
    @Qualifier("azure") restTemplate: RestOperations,
    @Value("\${clients.azure-graph.uri}") private val aadGraphURI: URI,
) :
    AbstractRestClient(restTemplate, "azureGraph-ts-integrasjoner") {

    fun saksbehandlerUri(id: String): URI =
        UriComponentsBuilder.fromUri(aadGraphURI)
            .pathSegment(USERS, id)
            .queryParam("\$select", FELTER)
            .build()
            .toUri()

    fun saksbehandlersøkUri(navIdent: String): URI =
        UriComponentsBuilder.fromUri(aadGraphURI)
            .pathSegment(USERS)
            .queryParam("\$search", "\"onPremisesSamAccountName:{navIdent}\"") // Denne linjen blir det feilencoding på mot azureGraph
            .queryParam("\$select", FELTER)
            .buildAndExpand(navIdent)
            .toUri()

    fun finnSaksbehandler(navIdent: String): AzureAdBrukere {
        return getForEntity(
            saksbehandlersøkUri(navIdent),
            HttpHeaders().apply {
                add("ConsistencyLevel", "eventual")
            },
        )
    }

    fun hentSaksbehandler(id: String): AzureAdBruker {
        return getForEntity(saksbehandlerUri(id))
    }

    companion object {
        private const val USERS = "users"
        private const val FELTER = "givenName,surname,onPremisesSamAccountName,id,userPrincipalName,streetAddress"
    }
}
