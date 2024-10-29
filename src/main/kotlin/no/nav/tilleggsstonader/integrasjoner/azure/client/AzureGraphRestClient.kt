package no.nav.tilleggsstonader.integrasjoner.azure.client

import no.nav.tilleggsstonader.integrasjoner.azure.domene.AzureAdBruker
import no.nav.tilleggsstonader.integrasjoner.azure.domene.AzureAdBrukere
import no.nav.tilleggsstonader.libs.http.client.AbstractRestClient
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Service
class AzureGraphRestClient(
    @Qualifier("azure") restTemplate: RestTemplate,
    @Value("\${clients.azure-graph.uri}") private val aadGraphURI: URI,
) : AbstractRestClient(restTemplate) {

    fun finnSaksbehandler(navIdent: String): AzureAdBrukere {
        val uri = UriComponentsBuilder.fromUri(aadGraphURI)
            .pathSegment(USERS)
            .queryParam("\$search", "\"onPremisesSamAccountName:{navIdent}\"")
            .queryParam("\$select", FELTER)
            .encode()
            .toUriString()
        return getForEntity(
            uri,
            HttpHeaders().apply {
                add("ConsistencyLevel", "eventual")
            },
            uriVariables = mapOf("navIdent" to navIdent)
        )
    }

    fun hentSaksbehandler(id: String): AzureAdBruker {
        val uri = UriComponentsBuilder.fromUri(aadGraphURI)
            .pathSegment(USERS, "{id}")
            .queryParam("\$select", FELTER)
            .encode()
            .toUriString()
        return getForEntity(uri, uriVariables = mapOf("id" to id))
    }

    companion object {
        private const val USERS = "users"
        private const val FELTER = "givenName,surname,onPremisesSamAccountName,id,userPrincipalName,streetAddress"
    }
}
