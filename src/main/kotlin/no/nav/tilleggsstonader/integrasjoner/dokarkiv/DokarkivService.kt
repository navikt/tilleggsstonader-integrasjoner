package no.nav.tilleggsstonader.integrasjoner.dokarkiv

import no.nav.tilleggsstonader.integrasjoner.dokarkiv.client.DokarkivLogiskVedleggRestClient
import no.nav.tilleggsstonader.integrasjoner.dokarkiv.client.DokarkivRestClient
import no.nav.tilleggsstonader.integrasjoner.dokarkiv.client.domene.ArkivDokument
import no.nav.tilleggsstonader.integrasjoner.dokarkiv.client.domene.Dokumentvariant
import no.nav.tilleggsstonader.integrasjoner.dokarkiv.client.domene.OpprettJournalpostRequest
import no.nav.tilleggsstonader.integrasjoner.dokarkiv.client.domene.OpprettJournalpostResponse
import no.nav.tilleggsstonader.integrasjoner.dokarkiv.metadata.tilMetadata
import no.nav.tilleggsstonader.kontrakter.dokarkiv.ArkiverDokumentRequest
import no.nav.tilleggsstonader.kontrakter.dokarkiv.ArkiverDokumentResponse
import no.nav.tilleggsstonader.kontrakter.dokarkiv.AvsenderMottaker
import no.nav.tilleggsstonader.kontrakter.dokarkiv.DokarkivBruker
import no.nav.tilleggsstonader.kontrakter.dokarkiv.Dokument
import no.nav.tilleggsstonader.kontrakter.dokarkiv.Filtype
import no.nav.tilleggsstonader.kontrakter.dokarkiv.LogiskVedleggRequest
import no.nav.tilleggsstonader.kontrakter.dokarkiv.LogiskVedleggResponse
import no.nav.tilleggsstonader.kontrakter.dokarkiv.OppdaterJournalpostRequest
import no.nav.tilleggsstonader.kontrakter.dokarkiv.OppdaterJournalpostResponse
import no.nav.tilleggsstonader.kontrakter.dokarkiv.Sak
import no.nav.tilleggsstonader.kontrakter.felles.BrukerIdType
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class DokarkivService(
    private val dokarkivRestClient: DokarkivRestClient,
    private val dokarkivLogiskVedleggRestClient: DokarkivLogiskVedleggRestClient,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun ferdistillJournalpost(journalpost: String, journalførendeEnhet: String, navIdent: String? = null) {
        dokarkivRestClient.ferdigstillJournalpost(journalpost, journalførendeEnhet, navIdent)
    }

    fun lagJournalpost(
        arkiverDokumentRequest: ArkiverDokumentRequest,
        navIdent: String? = null,
    ): ArkiverDokumentResponse {
        val request = mapTilOpprettJournalpostRequest(arkiverDokumentRequest)
        val response = dokarkivRestClient.lagJournalpost(request, arkiverDokumentRequest.forsøkFerdigstill, navIdent)
        return mapTilArkiverDokumentResponse(response)
    }

    private fun mapTilOpprettJournalpostRequest(arkiverDokumentRequest: ArkiverDokumentRequest): OpprettJournalpostRequest {
        val dokarkivBruker = DokarkivBruker(BrukerIdType.FNR, arkiverDokumentRequest.fnr)
        val hoveddokument = arkiverDokumentRequest.hoveddokumentvarianter[0]
        val metadata = hoveddokument.dokumenttype.tilMetadata()
        val avsenderMottaker = arkiverDokumentRequest.avsenderMottaker
            ?: AvsenderMottaker(arkiverDokumentRequest.fnr, BrukerIdType.FNR, navn = null)

        val dokumenter = mutableListOf(mapHoveddokument(arkiverDokumentRequest.hoveddokumentvarianter))
        dokumenter.addAll(arkiverDokumentRequest.vedleggsdokumenter.map(this::mapTilArkivdokument))
        val sak = arkiverDokumentRequest.fagsakId?.let {
            Sak(fagsakId = it, sakstype = "FAGSAK", fagsaksystem = metadata.fagsakSystem)
        }

        logger.info("Journalfører fagsak ${sak?.fagsakId} med tittel ${hoveddokument.tittel ?: metadata.tittel}")
        return OpprettJournalpostRequest(
            journalpostType = metadata.journalpostType,
            behandlingstema = metadata.behandlingstema?.value,
            kanal = metadata.kanal,
            tittel = hoveddokument.tittel ?: metadata.tittel,
            tema = metadata.tema.name,
            avsenderMottaker = avsenderMottaker,
            bruker = dokarkivBruker,
            dokumenter = dokumenter.toList(),
            eksternReferanseId = arkiverDokumentRequest.eksternReferanseId,
            journalfoerendeEnhet = arkiverDokumentRequest.journalførendeEnhet,
            sak = sak,
        )
    }

    fun oppdaterJournalpost(
        request: OppdaterJournalpostRequest,
        journalpostId: String,
        navIdent: String? = null,
    ): OppdaterJournalpostResponse {
        return dokarkivRestClient.oppdaterJournalpost(supplerDefaultVerdier(request), journalpostId, navIdent)
    }

    private fun supplerDefaultVerdier(request: OppdaterJournalpostRequest): OppdaterJournalpostRequest {
        return request.copy(sak = request.sak?.copy(sakstype = request.sak?.sakstype ?: "FAGSAK"))
    }

    private fun mapHoveddokument(dokumenter: List<Dokument>): ArkivDokument {
        val dokument = dokumenter[0]
        val metadata = dokument.dokumenttype.tilMetadata()
        val dokumentvarianter = dokumenter.map {
            val variantFormat: String = hentVariantformat(it)
            Dokumentvariant(it.filtype.name, variantFormat, it.dokument, it.filnavn)
        }

        return ArkivDokument(
            brevkode = metadata.brevkode,
            dokumentKategori = metadata.dokumentKategori,
            tittel = metadata.tittel ?: dokument.tittel,
            dokumentvarianter = dokumentvarianter,
        )
    }

    private fun hentVariantformat(dokument: Dokument): String {
        return if (dokument.filtype == Filtype.PDFA) {
            "ARKIV" // ustrukturert dokumentDto
        } else {
            "ORIGINAL" // strukturert dokumentDto
        }
    }

    private fun mapTilArkivdokument(dokument: Dokument): ArkivDokument {
        val metadata = dokument.dokumenttype.tilMetadata()
        val variantFormat: String = hentVariantformat(dokument)
        return ArkivDokument(
            brevkode = metadata.brevkode,
            dokumentKategori = metadata.dokumentKategori,
            tittel = metadata.tittel ?: dokument.tittel,
            dokumentvarianter = listOf(
                Dokumentvariant(
                    dokument.filtype.name,
                    variantFormat,
                    dokument.dokument,
                    dokument.filnavn,
                ),
            ),
        )
    }

    private fun mapTilArkiverDokumentResponse(response: OpprettJournalpostResponse): ArkiverDokumentResponse {
        return ArkiverDokumentResponse(
            response.journalpostId!!,
            response.journalpostferdigstilt ?: false,
            response.dokumenter,
        )
    }

    fun lagNyttLogiskVedlegg(
        dokumentInfoId: String,
        request: LogiskVedleggRequest,
    ): LogiskVedleggResponse {
        return dokarkivLogiskVedleggRestClient.opprettLogiskVedlegg(dokumentInfoId, request)
    }

    fun slettLogiskVedlegg(dokumentInfoId: String, logiskVedleggId: String) {
        dokarkivLogiskVedleggRestClient.slettLogiskVedlegg(dokumentInfoId, logiskVedleggId)
    }
}
