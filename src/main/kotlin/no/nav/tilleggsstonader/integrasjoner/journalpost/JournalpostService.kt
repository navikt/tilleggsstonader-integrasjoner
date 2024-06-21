package no.nav.tilleggsstonader.integrasjoner.journalpost

import no.nav.tilleggsstonader.integrasjoner.journalpost.client.SafClient
import no.nav.tilleggsstonader.integrasjoner.journalpost.client.SafHentDokumentClient
import no.nav.tilleggsstonader.integrasjoner.util.SikkerhetsContext
import no.nav.tilleggsstonader.kontrakter.journalpost.Journalpost
import no.nav.tilleggsstonader.kontrakter.journalpost.JournalposterForBrukerRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class JournalpostService @Autowired constructor(
    private val safClient: SafClient,
    private val safHentDokumentClient: SafHentDokumentClient,
) {

    fun hentSaksnummer(journalpostId: String): String? {
        val journalpost = safClient.hentJournalpost(journalpostId)
        return if (journalpost.sak != null && journalpost.sak?.arkivsaksystem == "GSAK") {
            journalpost.sak?.arkivsaksnummer
        } else {
            null
        }
    }

    fun hentJournalpost(journalpostId: String): Journalpost {
        if (SikkerhetsContext.hentSaksbehandlerEllerSystembruker() == "Z994214") {
            return safClient.hentJournalpost2(journalpostId)
        } else {
            return safClient.hentJournalpost(journalpostId)
        }
    }

    fun finnJournalposter(journalposterForBrukerRequest: JournalposterForBrukerRequest): List<Journalpost> {
        return safClient.finnJournalposter(journalposterForBrukerRequest)
    }

    fun hentDokument(journalpostId: String, dokumentInfoId: String, variantFormat: String): ByteArray {
        if (SikkerhetsContext.hentSaksbehandlerEllerSystembruker() == "Z994214") {
            return safHentDokumentClient.hentDokument2(journalpostId, dokumentInfoId, variantFormat)
        } else {
            return safHentDokumentClient.hentDokument(journalpostId, dokumentInfoId, variantFormat)
        }
    }
}
