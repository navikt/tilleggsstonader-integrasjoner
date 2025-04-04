package no.nav.tilleggsstonader.integrasjoner.infrastruktur

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpFilter
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import no.nav.tilleggsstonader.integrasjoner.util.SikkerhetsContext.erKallFraTilleggsstønader
import no.nav.tilleggsstonader.kontrakter.felles.ObjectMapperProvider.objectMapper
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail

/**
 * Validerer at alle kall som går mot andre endepunkter enn ekstern er applikasjoner fra tilleggsstønader
 */
class ValiderKallErFraTilleggsstønader : HttpFilter() {
    override fun doFilter(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        if (!request.requestURI.contains("/ekstern") && !shouldNotFilter(request.requestURI)) {
            if (!erKallFraTilleggsstønader()) {
                val problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "Ikke tilgang til endepunkt")
                response.writer.write(objectMapper.writeValueAsString(problemDetail))
                response.status = HttpStatus.FORBIDDEN.value()
                return
            }
        }
        filterChain.doFilter(request, response)
    }

    private fun shouldNotFilter(uri: String): Boolean = uri.contains("/internal") || uri == "/api/ping"
}
