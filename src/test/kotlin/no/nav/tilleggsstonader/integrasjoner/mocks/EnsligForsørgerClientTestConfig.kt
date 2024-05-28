package no.nav.tilleggsstonader.integrasjoner.mocks

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.tilleggsstonader.integrasjoner.ensligforsørger.EnsligForsørgerClient
import no.nav.tilleggsstonader.integrasjoner.ensligforsørger.EnsligForsørgerPerioder
import no.nav.tilleggsstonader.integrasjoner.ensligforsørger.EnsligForsørgerPerioderResponse
import no.nav.tilleggsstonader.integrasjoner.ensligforsørger.Periode
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import java.time.LocalDate

@Configuration
@Profile("mock-enslig")
class EnsligForsørgerClientTestConfig {

    @Bean
    @Primary
    fun ensligForsørgerClient(): EnsligForsørgerClient {
        val client = mockk<EnsligForsørgerClient>()
        resetMock(client)
        return client
    }

    companion object {
        fun resetMock(client: EnsligForsørgerClient) {
            clearMocks(client)
            val perioder = listOf(Periode(LocalDate.now(), LocalDate.now()))
            val response = EnsligForsørgerPerioderResponse(EnsligForsørgerPerioder(perioder))
            every { client.hentPerioder(any(), any(), any()) } returns response
        }
    }
}
