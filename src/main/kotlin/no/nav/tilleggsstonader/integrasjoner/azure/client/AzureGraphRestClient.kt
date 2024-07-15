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
    @Qualifier("jwtBearer") restTemplate: RestTemplate,
    @Value("\${clients.azure-graph.uri}") private val aadGraphURI: URI,
) :
    AbstractRestClient(restTemplate) {

    fun saksbehandlerUri(id: String): URI =
        UriComponentsBuilder.fromUri(aadGraphURI)
            .pathSegment(USERS, id)
            .queryParam("\$select", FELTER)
            .build()
            .toUri()

    fun saksbehandlersøkUri(navIdent: String): URI =
        UriComponentsBuilder.fromUri(aadGraphURI)
            .pathSegment(USERS)
            .queryParam("\$search", FELTSØKPARAM)
            .queryParam("\$select", FELTER)
            .buildAndExpand(navIdent)
            .toUri()

    fun finnSaksbehandler(navIdent: String): AzureAdBrukere {
        return getForEntity(
            saksbehandlersøkUri(navIdent).toString(),
            HttpHeaders().apply {
                add("ConsistencyLevel", "eventual")
            },
        )
    }

    fun hentSaksbehandler(id: String): AzureAdBruker {
        return getForEntity(saksbehandlerUri(id).toString())
    }

    companion object {
        private const val USERS = "users"
        private const val FELTER = "givenName,surname,onPremisesSamAccountName,id,userPrincipalName,streetAddress"
        private const val FELTSØKPARAM = "onPremisesSamAccountName:{navIdent}"
    }
}
