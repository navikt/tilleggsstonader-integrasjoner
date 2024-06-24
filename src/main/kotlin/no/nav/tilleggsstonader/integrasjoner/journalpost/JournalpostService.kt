package no.nav.tilleggsstonader.integrasjoner.journalpost

import no.nav.tilleggsstonader.integrasjoner.journalpost.client.SafClient
import no.nav.tilleggsstonader.integrasjoner.journalpost.client.SafHentDokumentClient
import no.nav.tilleggsstonader.integrasjoner.util.SikkerhetsContext
import no.nav.tilleggsstonader.kontrakter.journalpost.Journalpost
import no.nav.tilleggsstonader.kontrakter.journalpost.JournalposterForBrukerRequest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class JournalpostService @Autowired constructor(
    private val safClient: SafClient,
    private val safHentDokumentClient: SafHentDokumentClient,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun hentSaksnummer(journalpostId: String): String? {
        val journalpost = safClient.hentJournalpost(journalpostId)
        return if (journalpost.sak != null && journalpost.sak?.arkivsaksystem == "GSAK") {
            journalpost.sak?.arkivsaksnummer
        } else {
            null
        }
    }

    fun hentJournalpost(journalpostId: String): Journalpost {
        return if (skalRutesMotQ2()) {
            safClient.hentJournalpost2(journalpostId)
        } else {
            safClient.hentJournalpost(journalpostId)
        }
    }

    fun finnJournalposter(journalposterForBrukerRequest: JournalposterForBrukerRequest): List<Journalpost> {
        return if (skalRutesMotQ2()) {
            safClient.finnJournalposter2(journalposterForBrukerRequest)
        } else {
            safClient.finnJournalposter(journalposterForBrukerRequest)
        }
    }

    fun hentDokument(journalpostId: String, dokumentInfoId: String, variantFormat: String): ByteArray {
        return if (skalRutesMotQ2()) {
            safHentDokumentClient.hentDokument2(journalpostId, dokumentInfoId, variantFormat)
        } else {
            safHentDokumentClient.hentDokument(journalpostId, dokumentInfoId, variantFormat)
        }
    }

    private fun skalRutesMotQ2() = try {
        val saksbehandler = SikkerhetsContext.hentSaksbehandlerEllerSystembruker()
        logger.info("Henter saf-dokumenter med $saksbehandler")
        saksbehandler
    } catch (e: Exception) {
        logger.error("Feilet utleding av saksbehandler", e)
        ""
    } == "Z994214"
}
