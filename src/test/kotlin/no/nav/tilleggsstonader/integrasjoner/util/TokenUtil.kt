package no.nav.tilleggsstonader.integrasjoner.util

import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback
import java.util.UUID

object TokenUtil {
    /**
     * client token
     * oid = unik id på applikasjon A i Azure AD
     * sub = unik id på applikasjon A i Azure AD, alltid lik oid
     */
    fun clientToken(
        mockOAuth2Server: MockOAuth2Server,
        clientId: String,
        accessAsApplication: Boolean,
    ): String {
        val thisId = UUID.randomUUID().toString()

        val claims =
            mapOf(
                "oid" to thisId,
                "azp" to clientId,
                "roles" to if (accessAsApplication) listOf("access_as_application") else emptyList(),
            )

        return mockOAuth2Server
            .issueToken(
                issuerId = "azuread",
                subject = thisId,
                audience = "aud-localhost",
                claims = claims,
            ).serialize()
    }

    /**
     * On behalf
     * oid = unik id på brukeren i Azure AD
     * sub = unik id på brukeren i kombinasjon med applikasjon det ble logget inn i
     */
    fun onBehalfOfToken(
        mockOAuth2Server: MockOAuth2Server,
        role: String,
        saksbehandler: String,
        applikasjon: String,
    ): String {
        val clientId = UUID.randomUUID().toString()
        val brukerId = UUID.randomUUID().toString()

        val claims =
            mapOf(
                "oid" to brukerId,
                "azp" to clientId,
                "azp_name" to applikasjon,
                "name" to saksbehandler,
                "NAVident" to saksbehandler,
                "groups" to listOf(role),
            )

        return mockOAuth2Server
            .issueToken(
                issuerId = "azuread",
                subject = UUID.randomUUID().toString(),
                audience = "aud-localhost",
                claims = claims,
            ).serialize()
    }

    fun tokenX(
        mockOAuth2Server: MockOAuth2Server,
        ident: String = "11111122222",
        applikasjon: String = "dev-gcp:tilleggsstonader:tilleggsstonader-sak",
    ): String {
        val clientId = UUID.randomUUID().toString()

        val claims =
            mapOf(
                "acr" to "Level4",
                "pid" to ident,
                "client_id" to applikasjon,
            )
        return mockOAuth2Server
            .issueToken(
                "tokenx",
                clientId,
                DefaultOAuth2TokenCallback(
                    issuerId = "tokenx",
                    subject = ident,
                    audience = listOf("aud-localhost"),
                    claims = claims,
                    expiry = 3600,
                ),
            ).serialize()
    }
}
