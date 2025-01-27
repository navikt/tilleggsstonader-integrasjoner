package no.nav.tilleggsstonader.integrasjoner.dokdist

import no.nav.tilleggsstonader.integrasjoner.dokdist.domene.AdresseTo
import no.nav.tilleggsstonader.integrasjoner.dokdist.domene.DistribuerJournalpostRequestTo
import no.nav.tilleggsstonader.integrasjoner.dokdist.domene.DistribuerJournalpostResponseTo
import no.nav.tilleggsstonader.kontrakter.dokdist.DistribuerJournalpostRequest
import org.springframework.stereotype.Service

@Service
class DokdistService(
    val dokdistRestClient: DokdistRestClient,
) {
    fun distribuerDokumentForJournalpost(request: DistribuerJournalpostRequest): DistribuerJournalpostResponseTo =
        dokdistRestClient.distribuerJournalpost(mapTilDistribuerJournalpostRequestTo(request))

    private fun mapTilDistribuerJournalpostRequestTo(request: DistribuerJournalpostRequest): DistribuerJournalpostRequestTo =
        DistribuerJournalpostRequestTo(
            journalpostId = request.journalpostId,
            bestillendeFagsystem = request.bestillendeFagsystem.name,
            dokumentProdApp = request.dokumentProdApp,
            distribusjonstidspunkt = request.distribusjonstidspunkt,
            distribusjonstype = request.distribusjonstype,
            adresse =
                request.adresse?.let { adresse ->
                    AdresseTo(
                        adressetype = adresse.adresseType.name,
                        adresselinje1 = adresse.adresselinje1,
                        adresselinje2 = adresse.adresselinje2,
                        adresselinje3 = adresse.adresselinje3,
                        poststed = adresse.poststed,
                        postnummer = adresse.postnummer,
                        land = adresse.land,
                    )
                },
        )
}
