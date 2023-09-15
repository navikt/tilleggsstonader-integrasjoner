package no.nav.tilleggsstonader.integrasjoner.util

import org.apache.commons.lang3.StringUtils
import org.springframework.core.io.ClassPathResource

fun String.graphqlCompatible(): String {
    return StringUtils.normalizeSpace(this.replace("\n", ""))
}

fun graphqlQuery(path: String) = ClassPathResource(path).url.readText().graphqlCompatible()
