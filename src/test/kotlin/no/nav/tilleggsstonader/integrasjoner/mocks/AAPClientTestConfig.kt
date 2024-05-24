package no.nav.tilleggsstonader.integrasjoner.mocks

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.tilleggsstonader.integrasjoner.aap.AAPClient
import no.nav.tilleggsstonader.integrasjoner.aap.AAPPerioderResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Configuration
@Profile("mock-aap")
class AAPClientTestConfig {

    @Bean
    @Primary
    fun aapClient(): AAPClient {
        val client = mockk<AAPClient>()
        resetMock(client)
        return client
    }

    companion object {
        fun resetMock(client: AAPClient) {
            clearMocks(client)
            every { client.hentPerioder(any(), any(), any()) } returns AAPPerioderResponse(emptyList())
        }
    }
}
