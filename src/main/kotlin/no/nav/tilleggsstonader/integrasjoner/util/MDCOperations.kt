package no.nav.tilleggsstonader.integrasjoner.util

import no.nav.tilleggsstonader.libs.log.mdc.MDCConstants.MDC_CALL_ID
import org.slf4j.MDC

object MDCOperations {
    fun getCallId(): String = MDC.get(MDC_CALL_ID)
}
