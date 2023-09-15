package no.nav.tilleggsstonader.integrasjoner.journalpost

import no.nav.tilleggsstonader.integrasjoner.journalpost.client.SafHentDokumentRestClient
import no.nav.tilleggsstonader.integrasjoner.journalpost.client.SafRestClient
import no.nav.tilleggsstonader.integrasjoner.journalpost.internal.JournalposterForVedleggRequest
import no.nav.tilleggsstonader.kontrakter.journalpost.Journalpost
import no.nav.tilleggsstonader.kontrakter.journalpost.JournalposterForBrukerRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class JournalpostService @Autowired constructor(
    private val safRestClient: SafRestClient,
    private val safHentDokumentRestClient: SafHentDokumentRestClient,
) {

    fun hentSaksnummer(journalpostId: String): String? {
        val journalpost = safRestClient.hentJournalpost(journalpostId)
        return if (journalpost.sak != null && journalpost.sak?.arkivsaksystem == "GSAK") {
            journalpost.sak?.arkivsaksnummer
        } else {
            null
        }
    }

    fun hentJournalpost(journalpostId: String): Journalpost {
        return safRestClient.hentJournalpost(journalpostId)
    }

    fun finnJournalposter(journalposterForBrukerRequest: JournalposterForBrukerRequest): List<Journalpost> {
        return safRestClient.finnJournalposter(journalposterForBrukerRequest)
    }

    fun finnJournalposter(journalposterForVedleggRequest: JournalposterForVedleggRequest): List<Journalpost> {
        return safRestClient.finnJournalposter(journalposterForVedleggRequest)
    }

    fun hentDokument(journalpostId: String, dokumentInfoId: String, variantFormat: String): ByteArray {
        return safHentDokumentRestClient.hentDokument(journalpostId, dokumentInfoId, variantFormat)
    }
}
