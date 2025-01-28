package no.nav.tilleggsstonader.integrasjoner.util

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.tilleggsstonader.kontrakter.felles.ObjectMapperProvider.objectMapper
import org.springframework.web.util.UriComponentsBuilder

object QueryParamUtil {
    fun toQueryParams(any: Any): QueryParams {
        val writeValueAsString = objectMapper.writeValueAsString(any)
        val readValue: LinkedHashMap<String, Any?> = objectMapper.readValue(writeValueAsString)
        val queryParams = mutableMapOf<String, List<Pair<String, String>>>()
        readValue
            .filterNot { it.value == null }
            .filterNot { it.value is List<*> && (it.value as List<*>).isEmpty() }
            .forEach {
                if (it.value is List<*>) {
                    val liste = (it.value as List<*>).map { elem -> elem.toString() }
                    queryParams[it.key] =
                        if (liste.size == 1) {
                            listOf(Pair(it.key, liste.single()))
                        } else {
                            liste.mapIndexed { index, s -> Pair(it.key + "_" + index, s) }
                        }
                } else {
                    queryParams[it.key] = listOf(Pair(it.key, it.value.toString()))
                }
            }
        return QueryParams(queryParams)
    }

    fun UriComponentsBuilder.medQueryParams(params: QueryParams): UriComponentsBuilder {
        params.verdier.entries.forEach {
            this.queryParam(it.key, it.value.map { "{${it.first}}" })
        }
        return this
    }
}

/**
 * @param verdier map med key for queryParam, values med templateName og templateValue
 */
data class QueryParams(
    val verdier: MutableMap<String, List<Pair<String, String>>>,
) {
    fun tilUriVariables(): Map<String, String> = verdier.values.flatten().toMap()
}
