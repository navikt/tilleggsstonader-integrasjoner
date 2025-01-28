package no.nav.tilleggsstonader.integrasjoner.journalpost

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import no.nav.tilleggsstonader.integrasjoner.journalpost.client.SafClient
import no.nav.tilleggsstonader.kontrakter.felles.BrukerIdType
import no.nav.tilleggsstonader.kontrakter.journalpost.Bruker
import no.nav.tilleggsstonader.kontrakter.journalpost.DokumentInfo
import no.nav.tilleggsstonader.kontrakter.journalpost.Dokumentstatus
import no.nav.tilleggsstonader.kontrakter.journalpost.Journalpost
import no.nav.tilleggsstonader.kontrakter.journalpost.Journalposttype
import no.nav.tilleggsstonader.kontrakter.journalpost.Journalstatus
import no.nav.tilleggsstonader.kontrakter.journalpost.LogiskVedlegg
import no.nav.tilleggsstonader.kontrakter.journalpost.Sak
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Configuration
@Profile("mock-saf")
class HentJournalpostTestConfig {
    @Bean
    @Primary
    fun safRestClientMock(): SafClient {
        val klient: SafClient = mockk(relaxed = true)
        val slot = slot<String>()

        every { klient.hentJournalpost(capture(slot)) } answers {
            Journalpost(
                journalpostId = slot.captured,
                journalposttype = Journalposttype.I,
                journalstatus = Journalstatus.MOTTATT,
                tema = "BAR",
                tittel = "Ent tittel",
                behandlingstema = null,
                sak =
                    Sak(
                        "1111" + slot.captured,
                        "GSAK",
                        null,
                        null,
                        null,
                    ),
                bruker = Bruker("1234567890123", BrukerIdType.AKTOERID),
                journalforendeEnhet = "9999",
                kanal = "EIA",
                dokumenter =
                    listOf(
                        DokumentInfo(
                            dokumentInfoId = "1234",
                            tittel = "Søknad om ytelse",
                            dokumentstatus = Dokumentstatus.FERDIGSTILT,
                            dokumentvarianter = emptyList(),
                            logiskeVedlegg =
                                listOf(
                                    LogiskVedlegg(
                                        logiskVedleggId = "0987",
                                        tittel = "Oppholdstillatelse",
                                    ),
                                ),
                        ),
                    ),
                relevanteDatoer = emptyList(),
            )
        }

        return klient
    }
}
