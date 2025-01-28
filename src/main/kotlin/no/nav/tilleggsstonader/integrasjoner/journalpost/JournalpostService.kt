package no.nav.tilleggsstonader.integrasjoner.journalpost

import no.nav.tilleggsstonader.integrasjoner.journalpost.client.SafClient
import no.nav.tilleggsstonader.integrasjoner.journalpost.client.SafHentDokumentClient
import no.nav.tilleggsstonader.kontrakter.journalpost.Journalpost
import no.nav.tilleggsstonader.kontrakter.journalpost.JournalposterForBrukerRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class JournalpostService
    @Autowired
    constructor(
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

        fun hentJournalpost(journalpostId: String): Journalpost = safClient.hentJournalpost(journalpostId)

        fun finnJournalposter(journalposterForBrukerRequest: JournalposterForBrukerRequest): List<Journalpost> =
            safClient.finnJournalposter(journalposterForBrukerRequest)

        fun hentDokument(
            journalpostId: String,
            dokumentInfoId: String,
            variantFormat: String,
        ): ByteArray = safHentDokumentClient.hentDokument(journalpostId, dokumentInfoId, variantFormat)
    }
