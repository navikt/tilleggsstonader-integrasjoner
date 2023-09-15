package no.nav.tilleggsstonader.integrasjoner.dokarkiv

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import no.nav.tilleggsstonader.integrasjoner.dokarkiv.client.DokarkivLogiskVedleggRestClient
import no.nav.tilleggsstonader.integrasjoner.dokarkiv.client.DokarkivRestClient
import no.nav.tilleggsstonader.integrasjoner.dokarkiv.client.domene.OpprettJournalpostResponse
import no.nav.tilleggsstonader.kontrakter.dokarkiv.LogiskVedleggResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Configuration
@Profile("mock-dokarkiv")
class DokarkivRestClientTestConfig {

    @Bean
    @Primary
    fun dokarkivMockRestClient(): DokarkivRestClient {
        val klient = mockk<DokarkivRestClient>(relaxed = true)
        val pattern = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
        val response = OpprettJournalpostResponse(
            journalpostId = LocalDateTime.now().format(pattern),
            journalpostferdigstilt = false,
        )
        every {
            klient.lagJournalpost(any(), any())
        } returns response

        every {
            klient.ferdigstillJournalpost(any(), any(), any())
        } just Runs

        return klient
    }

    @Bean
    @Primary
    fun dokarkivLogiskVedleggMockRestClient(): DokarkivLogiskVedleggRestClient {
        val klient: DokarkivLogiskVedleggRestClient = mockk(relaxed = true)

        every {
            klient.opprettLogiskVedlegg(any(), any())
        } returns LogiskVedleggResponse(123456789L)

        every {
            klient.slettLogiskVedlegg(any(), any())
        } just Runs

        return klient
    }
}
