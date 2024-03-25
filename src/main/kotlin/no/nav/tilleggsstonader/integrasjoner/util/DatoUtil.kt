package no.nav.tilleggsstonader.integrasjoner.util

import java.time.LocalDate

object DatoUtil {

    fun min(first: LocalDate?, second: LocalDate?): LocalDate? {
        return when {
            first == null -> second
            second == null -> first
            else -> minOf(first, second)
        }
    }

    fun max(first: LocalDate?, second: LocalDate?): LocalDate? {
        return when {
            first == null -> second
            second == null -> first
            else -> maxOf(first, second)
        }
    }
}
