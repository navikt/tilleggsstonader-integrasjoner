package no.nav.tilleggsstonader.integrasjoner.dokarkiv.client.domene

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.tilleggsstonader.kontrakter.dokarkiv.DokumentInfo
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder

class OpprettJournalpostResponse(
    val journalpostId: String? = null,
    val melding: String? = null,
    val journalpostferdigstilt: Boolean? = false,
    val dokumenter: List<DokumentInfo>? = null,
)

fun main() {
    Jackson2ObjectMapperBuilder.json().build<ObjectMapper>().readValue<OpprettJournalpostResponse>(
        """{
  "dokumenter": [
    {
      "brevkode": "NAV 34-00.08",
      "dokumentInfoId": "123",
      "tittel": "Søknad om kontanstøtte"
    }
  ],
  "journalpostId": "12345678",
  "journalpostferdigstilt": false,
  "journalstatus": "MIDLERTIDIG"
}""",
    )
}
