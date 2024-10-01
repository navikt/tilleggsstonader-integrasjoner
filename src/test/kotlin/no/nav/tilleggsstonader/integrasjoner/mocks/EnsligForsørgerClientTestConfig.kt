package no.nav.tilleggsstonader.integrasjoner.mocks

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.tilleggsstonader.integrasjoner.ensligforsørger.*
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
            val perioder = listOf(
                EnsligForsørgerPeriode(
                    LocalDate.now(),
                    LocalDate.now(),
                    stønadstype = EnsligForsørgerStønadstype.OVERGANGSSTØNAD
                )
            )
            every { client.hentPerioder(any(), any(), any()) } answers {
                EnsligForsørgerPerioderResponse(EnsligForsørgerPerioder(firstArg(), perioder))
            }
        }
    }
}
