package no.nav.tilleggsstonader.integrasjoner.mocks

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.tilleggsstonader.integrasjoner.etterlatte.EtterlatteClient
import no.nav.tilleggsstonader.integrasjoner.etterlatte.PeriodeEtterlatte
import no.nav.tilleggsstonader.integrasjoner.etterlatte.Samordningsvedtak
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import java.time.LocalDate

@Configuration
@Profile("mock-etterlatte")
class EtterlatteClientTestConfig {

    @Bean
    @Primary
    fun etterlatteClient(): EtterlatteClient {
        val client = mockk<EtterlatteClient>()
        resetMock(client)
        return client
    }

    companion object {
        fun resetMock(client: EtterlatteClient) {
            clearMocks(client)
            val response = listOf(Samordningsvedtak(perioder = listOf(PeriodeEtterlatte(LocalDate.now(), LocalDate.now()))))
            every { client.hentPerioder(any(), any()) } returns response
        }
    }
}
