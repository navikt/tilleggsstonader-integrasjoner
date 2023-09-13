package no.nav.tilleggsstonader.integrasjoner.infrastruktur.config

import org.slf4j.Logger
import org.slf4j.LoggerFactory

object SecureLogger {
    // TODO legge på mer context? (classname)
    val secureLogger: Logger = LoggerFactory.getLogger("secureLogger")
}
