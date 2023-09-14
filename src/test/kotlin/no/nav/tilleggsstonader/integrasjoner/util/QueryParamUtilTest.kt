package no.nav.tilleggsstonader.integrasjoner.util

import no.nav.tilleggsstonader.integrasjoner.util.QueryParamUtil.toQueryParams
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.util.LinkedMultiValueMap

class QueryParamUtilTest {
    @Test
    fun `toQueryParams ivaretar lister som lister`() {
        val queryParams: LinkedMultiValueMap<String, String> = toQueryParams(Testdata())

        assertThat(queryParams["list"]).containsExactly("1", "2", "3", "4", "5")
    }

    @Test
    fun `toQueryParams filtrerer vekk tomme lister`() {
        val queryParams: LinkedMultiValueMap<String, String> = toQueryParams(Testdata(list = listOf()))

        assertThat(queryParams.containsKey("list")).isFalse()
    }

    data class Testdata(
        val int: Int = 5,
        val string: String = "Fem",
        val list: List<Int> = listOf(1, 2, 3, 4, 5),
    )
}
