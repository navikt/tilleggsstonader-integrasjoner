package no.nav.tilleggsstonader.integrasjoner.dokarkiv.metadata

import no.nav.tilleggsstonader.kontrakter.dokarkiv.Dokumenttype
import no.nav.tilleggsstonader.kontrakter.felles.Stønadstype
import no.nav.tilleggsstonader.kontrakter.felles.tilBehandlingstema
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import kotlin.reflect.KClass

internal class DokarkivMetadataTest {
    val alleDokumentMedadataKlasser = hentAlleDokumentMedadataKlasser()

    @Test
    internal fun `brevkode må være under 50 tegn`() {
        val tittelOver50Tegn = alleDokumentMedadataKlasser.filter { (it.brevkode?.length ?: 0) > 50 }
        assertThat(tittelOver50Tegn).isEmpty()
    }

    @Test
    fun `Dokumenttypen i dokumentmetadata mapper tilbake til samme dokumentmetadata`() {
        alleDokumentMedadataKlasser.forEach {
            assertThat(it.dokumenttype.tilMetadata()).isEqualTo(it)
        }
    }

    @ParameterizedTest
    @EnumSource(value = Stønadstype::class)
    fun `alle stønader skal være koblet til metadata for div typer brev`(stønadstype: Stønadstype) {
        // klassenavn til suffix mapping
        listOf(
            "SøknadMetadata" to "_SØKNAD",
            "SøknadVedleggMetadata" to "_SØKNAD_VEDLEGG",
            "FrittståendeBrevMetadata" to "_FRITTSTÅENDE_BREV",
            "InterntVedtakMetadata" to "_INTERNT_VEDTAK",
            "VedtaksbrevMetadata" to "_VEDTAKSBREV",
            "KlageInterntVedtak" to "_KLAGE_INTERNT_VEDTAK",
            "KlageVedtak" to "_KLAGE_VEDTAKSBREV",
        ).forEach { (klassenavn, suffix) ->
            val metadata = finnPåkrevdBrev(stønadstype, klassenavn)
            metadata.behandlingstema?.let { assertThat(it).isEqualTo(stønadstype.tilBehandlingstema()) }
            assertThat(metadata.dokumenttype.name).isEqualTo("$stønadstype$suffix")
        }
    }

    private fun finnPåkrevdBrev(
        stønadstype: Stønadstype,
        type: String,
    ) = alleDokumentMedadataKlasser.single {
        it::class.simpleName!!.equals("${stønadstype.name}$type", ignoreCase = true)
    }

    @ParameterizedTest
    @EnumSource(value = Dokumenttype::class)
    fun `dokumenttype mapper til metadata med samme dokumenttype i parameterene`(dokumenttype: Dokumenttype) {
        assertThat(dokumenttype.tilMetadata().dokumenttype).isEqualTo(dokumenttype)
    }

    private fun hentAlleDokumentMedadataKlasser() =
        Dokumentmetadata::class
            .sealedSubclasses
            .hentNøstedeKlasser()
            .mapNotNull { it.objectInstance }

    private fun <T : Any> List<KClass<out T>>.hentNøstedeKlasser(): List<KClass<out T>> =
        flatMap {
            if (it.isSealed) {
                it.sealedSubclasses.hentNøstedeKlasser()
            } else {
                listOf(it)
            }
        }
}
