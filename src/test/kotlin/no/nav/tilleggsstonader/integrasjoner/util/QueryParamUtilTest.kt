package no.nav.tilleggsstonader.integrasjoner.util

import no.nav.tilleggsstonader.integrasjoner.util.QueryParamUtil.toQueryParams
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class QueryParamUtilTest {
    @Test
    fun `toQueryParams ivaretar lister som lister`() {
        val queryParams = toQueryParams(Testdata())

        assertThat(queryParams.verdier["int"]).containsExactly(Pair("int", "5"))
        assertThat(queryParams.verdier["string"]).containsExactly(Pair("string", "fem"))
        assertThat(queryParams.verdier["list"]).containsExactly(Pair("list_1", "11"), Pair("list_2", "22"))
    }

    @Test
    fun `toQueryParams filtrerer vekk tomme lister`() {
        val queryParams = toQueryParams(Testdata(list = listOf()))

        assertThat(queryParams.verdier).doesNotContainKey("list")
    }

    data class Testdata(
        val int: Int = 5,
        val string: String = "Fem",
        val list: List<Int> = listOf(11, 22),
    )
}
