package no.nav.tilleggsstonader.integrasjoner.dokarkiv

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import io.mockk.verify
import no.nav.tilleggsstonader.integrasjoner.dokarkiv.client.DokarkivLogiskVedleggRestClient
import no.nav.tilleggsstonader.integrasjoner.dokarkiv.client.DokarkivRestClient
import no.nav.tilleggsstonader.integrasjoner.dokarkiv.client.domene.JournalpostType
import no.nav.tilleggsstonader.integrasjoner.dokarkiv.client.domene.OpprettJournalpostRequest
import no.nav.tilleggsstonader.integrasjoner.dokarkiv.client.domene.OpprettJournalpostResponse
import no.nav.tilleggsstonader.integrasjoner.dokarkiv.metadata.BarnetilsynSøknadMetadata
import no.nav.tilleggsstonader.integrasjoner.dokarkiv.metadata.BarnetilsynSøknadVedleggMetadata
import no.nav.tilleggsstonader.kontrakter.dokarkiv.ArkiverDokumentRequest
import no.nav.tilleggsstonader.kontrakter.dokarkiv.AvsenderMottaker
import no.nav.tilleggsstonader.kontrakter.dokarkiv.DokarkivBruker
import no.nav.tilleggsstonader.kontrakter.dokarkiv.Dokument
import no.nav.tilleggsstonader.kontrakter.dokarkiv.Dokumenttype
import no.nav.tilleggsstonader.kontrakter.dokarkiv.Filtype
import no.nav.tilleggsstonader.kontrakter.dokarkiv.OppdaterJournalpostRequest
import no.nav.tilleggsstonader.kontrakter.dokarkiv.OppdaterJournalpostResponse
import no.nav.tilleggsstonader.kontrakter.dokarkiv.Sak
import no.nav.tilleggsstonader.kontrakter.felles.BrukerIdType
import no.nav.tilleggsstonader.kontrakter.felles.Fagsystem
import no.nav.tilleggsstonader.kontrakter.felles.Tema
import no.nav.tilleggsstonader.libs.log.mdc.MDCConstants
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.MDC
import java.util.UUID

class DokarkivServiceTest {
    @MockK
    lateinit var dokarkivRestClient: DokarkivRestClient

    @MockK
    lateinit var dokarkivLogiskVedleggRestClient: DokarkivLogiskVedleggRestClient

    private lateinit var dokarkivService: DokarkivService

    @BeforeEach
    fun setUp() {
        MDC.put(MDCConstants.MDC_CALL_ID, UUID.randomUUID().toString()) // settes vanligvis i LogFilter
        MockKAnnotations.init(this)
        dokarkivService =
            DokarkivService(
                dokarkivRestClient,
                dokarkivLogiskVedleggRestClient,
            )
    }

    @AfterEach
    internal fun tearDown() {
        MDC.remove(MDCConstants.MDC_CALL_ID)
    }

    @Test
    fun `oppdaterJournalpost skal legge til default sakstype`() {
        val slot = slot<OppdaterJournalpostRequest>()
        every { dokarkivRestClient.oppdaterJournalpost(capture(slot), any()) }
            .answers { OppdaterJournalpostResponse(JOURNALPOST_ID) }

        val bruker = DokarkivBruker(BrukerIdType.FNR, "12345678910")
        val dto = OppdaterJournalpostRequest(bruker = bruker, tema = Tema.TSO, sak = Sak("11111111", "fagsaksystem"))

        dokarkivService.oppdaterJournalpost(dto, JOURNALPOST_ID)

        val request = slot.captured
        verify {
            dokarkivRestClient.oppdaterJournalpost(slot.captured, JOURNALPOST_ID)
        }
        assertThat(request.sak?.sakstype == "FAGSAK")
    }

    @Test
    fun `skal mappe request til opprettJournalpostRequest av type arkiv pdfa`() {
        val slot = slot<OpprettJournalpostRequest>()
        every { dokarkivRestClient.lagJournalpost(capture(slot), any()) }
            .answers { OpprettJournalpostResponse(journalpostId = "123", journalpostferdigstilt = false) }

        val dto =
            ArkiverDokumentRequest(
                FNR,
                false,
                listOf(Dokument(PDF_DOK, Filtype.PDFA, FILNAVN, null, Dokumenttype.BARNETILSYN_SØKNAD)),
                eksternReferanseId = "id",
                avsenderMottaker = AvsenderMottaker("fnr", BrukerIdType.FNR, "navn"),
            )

        dokarkivService.lagJournalpost(dto)

        val request = slot.captured
        verify {
            dokarkivRestClient.lagJournalpost(slot.captured, false)
        }
        assertOpprettJournalpostRequest(request, "PDFA", PDF_DOK, ARKIV_VARIANTFORMAT)
    }

    @Test
    fun `skal mappe request til opprettJournalpostRequest for barnetilsyn søknad`() {
        val slot = slot<OpprettJournalpostRequest>()

        every { dokarkivRestClient.lagJournalpost(capture(slot), any()) }
            .answers { OpprettJournalpostResponse(journalpostId = "123", journalpostferdigstilt = false) }

        val dto =
            ArkiverDokumentRequest(
                FNR,
                false,
                listOf(Dokument(PDF_DOK, Filtype.PDFA, FILNAVN, null, Dokumenttype.BARNETILSYN_SØKNAD)),
                listOf(
                    Dokument(PDF_DOK, Filtype.PDFA, null, TITTEL, Dokumenttype.BARNETILSYN_SØKNAD_VEDLEGG),
                ),
                fagsakId = FAGSAK_ID,
                eksternReferanseId = "id",
                avsenderMottaker = AvsenderMottaker("fnr", BrukerIdType.FNR, "navn"),
            )

        dokarkivService.lagJournalpost(dto)

        val request = slot.captured
        assertOpprettBarnetrygdVedtakJournalpostRequest(
            request,
            PDF_DOK,
            Sak(
                fagsakId = FAGSAK_ID,
                fagsaksystem = Fagsystem.TILLEGGSSTONADER,
                sakstype = "FAGSAK",
            ),
        )
    }

    @Test
    fun `skal mappe request til opprettJournalpostRequest av type ORIGINAL JSON`() {
        val slot = slot<OpprettJournalpostRequest>()
        every { dokarkivRestClient.lagJournalpost(capture(slot), any()) }
            .answers { OpprettJournalpostResponse(journalpostId = "123", journalpostferdigstilt = false) }

        val dto =
            ArkiverDokumentRequest(
                FNR,
                false,
                listOf(Dokument(JSON_DOK, Filtype.JSON, FILNAVN, null, Dokumenttype.BARNETILSYN_SØKNAD)),
                eksternReferanseId = "id",
                avsenderMottaker = AvsenderMottaker("fnr", BrukerIdType.FNR, "navn"),
            )

        dokarkivService.lagJournalpost(dto)

        val request = slot.captured

        verify {
            dokarkivRestClient.lagJournalpost(request, false)
        }

        assertOpprettJournalpostRequest(
            request,
            "JSON",
            JSON_DOK,
            STRUKTURERT_VARIANTFORMAT,
        )
    }

    @Test
    fun `response fra klient skal returnere arkiverDokumentResponse`() {
        every { dokarkivRestClient.lagJournalpost(any(), any()) }
            .answers { OpprettJournalpostResponse(journalpostId = JOURNALPOST_ID, journalpostferdigstilt = true) }

        val dto =
            ArkiverDokumentRequest(
                FNR,
                false,
                listOf(Dokument(JSON_DOK, Filtype.JSON, FILNAVN, null, Dokumenttype.BARNETILSYN_SØKNAD)),
                eksternReferanseId = "id",
                avsenderMottaker = AvsenderMottaker("fnr", BrukerIdType.FNR, "navn"),
            )

        val arkiverDokumentResponse = dokarkivService.lagJournalpost(dto)

        assertThat(arkiverDokumentResponse.journalpostId).isEqualTo(JOURNALPOST_ID)
        assertThat(arkiverDokumentResponse.ferdigstilt).isTrue
    }

    private fun assertOpprettJournalpostRequest(
        request: OpprettJournalpostRequest,
        pdfa: String,
        pdfDok: ByteArray,
        arkivVariantformat: String,
        sak: Sak? = null,
    ) {
        assertThat(request.avsenderMottaker!!.id).isEqualTo(FNR)
        assertThat(request.avsenderMottaker!!.idType).isEqualTo(BrukerIdType.FNR)
        assertThat(request.bruker!!.id).isEqualTo(FNR)
        assertThat(request.bruker!!.idType).isEqualTo(BrukerIdType.FNR)
        assertThat(request.behandlingstema).isEqualTo(BarnetilsynSøknadMetadata.behandlingstema.value)
        assertThat(request.journalpostType).isEqualTo(JournalpostType.INNGAAENDE)
        assertThat(request.kanal).isEqualTo(BarnetilsynSøknadMetadata.kanal)
        assertThat(request.tema).isEqualTo(BarnetilsynSøknadMetadata.tema.toString())
        assertThat(request.sak).isNull()
        assertThat(request.dokumenter[0].tittel).isEqualTo(BarnetilsynSøknadMetadata.tittel)
        assertThat(request.dokumenter[0].brevkode).isEqualTo(BarnetilsynSøknadMetadata.brevkode)
        assertThat(request.dokumenter[0].dokumentKategori).isEqualTo(BarnetilsynSøknadMetadata.dokumentKategori)
        assertThat(request.dokumenter[0].dokumentvarianter[0].filtype).isEqualTo(pdfa)
        assertThat(request.dokumenter[0].dokumentvarianter[0].fysiskDokument).isEqualTo(pdfDok)
        assertThat(request.dokumenter[0].dokumentvarianter[0].variantformat).isEqualTo(arkivVariantformat)
        assertThat(request.dokumenter[0].dokumentvarianter[0].filnavn).isEqualTo(FILNAVN)
        if (sak != null) {
            assertThat(request.sak!!.fagsakId).isEqualTo(sak.fagsakId)
            assertThat(request.sak!!.fagsaksystem).isEqualTo(sak.fagsaksystem)
            assertThat(request.sak!!.fagsaksystem).isEqualTo(sak.sakstype)
        }
    }

    private fun assertOpprettBarnetrygdVedtakJournalpostRequest(
        request: OpprettJournalpostRequest,
        pdfDok: ByteArray,
        sak: Sak,
    ) {
        assertThat(request.avsenderMottaker!!.id).isEqualTo(FNR)
        assertThat(request.avsenderMottaker!!.idType).isEqualTo(BrukerIdType.FNR)
        assertThat(request.bruker!!.id).isEqualTo(FNR)
        assertThat(request.bruker!!.idType).isEqualTo(BrukerIdType.FNR)
        assertThat(request.behandlingstema).isEqualTo(BarnetilsynSøknadMetadata.behandlingstema.value)
        assertThat(request.journalpostType).isEqualTo(BarnetilsynSøknadMetadata.journalpostType)
        assertThat(request.kanal).isEqualTo(BarnetilsynSøknadMetadata.kanal)
        assertThat(request.tema).isEqualTo(BarnetilsynSøknadMetadata.tema.toString())
        assertThat(request.dokumenter[0].tittel).isEqualTo(BarnetilsynSøknadMetadata.tittel)
        assertThat(request.dokumenter[0].brevkode).isEqualTo(BarnetilsynSøknadMetadata.brevkode)
        assertThat(request.dokumenter[0].dokumentKategori).isEqualTo(BarnetilsynSøknadMetadata.dokumentKategori)
        assertThat(request.dokumenter[0].dokumentvarianter[0].filtype).isEqualTo("PDFA")
        assertThat(request.dokumenter[0].dokumentvarianter[0].fysiskDokument).isEqualTo(pdfDok)
        assertThat(request.dokumenter[0].dokumentvarianter[0].variantformat).isEqualTo("ARKIV")
        assertThat(request.dokumenter[0].dokumentvarianter[0].filnavn).isEqualTo(FILNAVN)
        assertThat(request.dokumenter[1].tittel).isEqualTo(TITTEL)
        assertThat(request.dokumenter[1].brevkode).isEqualTo(BarnetilsynSøknadVedleggMetadata.brevkode)
        assertThat(request.dokumenter[1].dokumentKategori).isEqualTo(BarnetilsynSøknadVedleggMetadata.dokumentKategori)
        assertThat(request.dokumenter[1].dokumentvarianter[0].filtype).isEqualTo("PDFA")
        assertThat(request.dokumenter[1].dokumentvarianter[0].fysiskDokument).isEqualTo(pdfDok)
        assertThat(request.dokumenter[1].dokumentvarianter[0].variantformat).isEqualTo("ARKIV")
        assertThat(request.dokumenter[1].dokumentvarianter[0].filnavn).isEqualTo(null)
        assertThat(request.sak!!.fagsakId).isEqualTo(sak.fagsakId)
        assertThat(request.sak!!.fagsaksystem).isEqualTo(sak.fagsaksystem)
        assertThat(request.sak!!.sakstype).isEqualTo(sak.sakstype)
    }

    companion object {
        private const val FNR = "fnr"
        private val PDF_DOK = "dok".toByteArray()
        private const val ARKIV_VARIANTFORMAT = "ARKIV"
        private val JSON_DOK = "{}".toByteArray()
        private const val STRUKTURERT_VARIANTFORMAT = "ORIGINAL"
        private const val JOURNALPOST_ID = "123"
        private const val FILNAVN = "filnavn"
        private const val TITTEL = "tittel"
        private const val FAGSAK_ID = "s200"
    }
}
