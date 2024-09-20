package no.nav.tilleggsstonader.integrasjoner.dokarkiv.metadata

import no.nav.tilleggsstonader.kontrakter.dokarkiv.Dokumenttype
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.reflect.KClass

internal class DokarkivMetadataTest {

    @Test
    internal fun `brevkode må være under 50 tegn`() {
        val tittelOver50Tegn = hentAlleDokumentMedadataKlasser().filter { (it.brevkode?.length ?: 0) > 50 }
        assertThat(tittelOver50Tegn).isEmpty()
    }

    @Test
    fun `Dokumenttypen i dokumentmetadata mapper tilbake til samme dokumentmetadata`() {
        hentAlleDokumentMedadataKlasser().forEach {
            assertThat(it.dokumenttype.tilMetadata()).isEqualTo(it)
        }
    }

    @Test
    fun `Alle dokumenttyper mapper til metadata med samme dokumenttype i parameterene`() {
        Dokumenttype.entries.filterNot {
            // TODO: Fjern filtreringen når dokumenttypene har blitt implementert
            it in setOf(
                Dokumenttype.LÆREMIDLER_INTERNT_VEDTAK,
                Dokumenttype.LÆREMIDLER_FRITTSTÅENDE_BREV,
                Dokumenttype.LÆREMIDLER_VEDTAKSBREV,
            )
        }.forEach {
            assertThat(it.tilMetadata().dokumenttype).isEqualTo(it)
        }
    }

    private fun hentAlleDokumentMedadataKlasser() = Dokumentmetadata::class.sealedSubclasses
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
