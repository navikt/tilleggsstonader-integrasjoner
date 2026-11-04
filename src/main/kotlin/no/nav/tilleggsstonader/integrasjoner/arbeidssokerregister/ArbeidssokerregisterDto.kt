package no.nav.tilleggsstonader.integrasjoner.arbeidssokerregister

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.OffsetDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class Arbeidssokerperiode(
    val startet: OffsetDateTime,
    val avsluttet: OffsetDateTime? = null,
)

internal class ArbeidssokerregisterRequest(
    val identitetsnummer: String,
    val type: String = "IDENTITETSNUMMER",
)
