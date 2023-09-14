package no.nav.tilleggsstonader.integrasjoner.oppgave

import no.nav.tilleggsstonader.integrasjoner.infrastruktur.exception.OppslagException
import no.nav.tilleggsstonader.integrasjoner.util.QueryParamUtil.toQueryParams
import no.nav.tilleggsstonader.kontrakter.oppgave.FinnMappeRequest
import no.nav.tilleggsstonader.kontrakter.oppgave.FinnMappeResponseDto
import no.nav.tilleggsstonader.kontrakter.oppgave.FinnOppgaveRequest
import no.nav.tilleggsstonader.kontrakter.oppgave.FinnOppgaveResponseDto
import no.nav.tilleggsstonader.kontrakter.oppgave.Oppgave
import no.nav.tilleggsstonader.libs.http.client.AbstractRestClient
import no.nav.tilleggsstonader.libs.log.mdc.MDCConstants
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import kotlin.math.min

@Component
class OppgaveClient(
    @Value("\${OPPGAVE_URL}") private val oppgaveBaseUrl: URI,
    @Qualifier("azure") restTemplate: RestTemplate,
) : AbstractRestClient(restTemplate) {

    private val logger = LoggerFactory.getLogger(OppgaveClient::class.java)

    fun finnÅpenBehandleSakOppgave(request: Oppgave): Oppgave {
        request.takeUnless { it.aktoerId == null } ?: error("Finner ikke aktør id på request")
        request.takeUnless {
            it.journalpostId == null
        } ?: error("Finner ikke journalpost id på request")

        val requestUrl = lagRequestUrlMed(
            request.aktoerId!!,
            request.journalpostId!!,
            request.tema?.name ?: error("Mangler tema"),
        )
        return requestOppgaveJson(requestUrl)
    }

    fun finnOppgaveMedId(oppgaveId: Long): Oppgave {
        return getForEntity(requestUrl(oppgaveId), httpHeaders())
    }

    fun buildOppgaveRequestUri(oppgaveRequest: OppgaveRequest): URI =
        UriComponentsBuilder.fromUri(oppgaveBaseUrl)
            .path(PATH_OPPGAVE)
            .queryParams(toQueryParams(oppgaveRequest))
            .build()
            .toUri()

    fun buildMappeRequestUri(mappeRequest: FinnMappeRequest) =
        UriComponentsBuilder.fromUri(oppgaveBaseUrl)
            .path(PATH_MAPPE)
            .queryParams(toQueryParams(mappeRequest))
            .build()
            .toUri()

    fun finnOppgaver(finnOppgaveRequest: FinnOppgaveRequest): FinnOppgaveResponseDto {
        val oppgaveRequest = finnOppgaveRequest.toDto()
        var offset = oppgaveRequest.offset

        val oppgaverOgAntall =
            getForEntity<FinnOppgaveResponseDto>(buildOppgaveRequestUri(oppgaveRequest), httpHeaders())
        val oppgaver: MutableList<Oppgave> = oppgaverOgAntall.oppgaver.toMutableList()
        val grense =
            if (finnOppgaveRequest.limit == null) {
                oppgaverOgAntall.antallTreffTotalt
            } else {
                oppgaveRequest.offset + finnOppgaveRequest.limit!!
            }
        offset += limitMotOppgave

        while (offset < grense) {
            val nyeOppgaver =
                getForEntity<FinnOppgaveResponseDto>(
                    buildOppgaveRequestUri(
                        oppgaveRequest.copy(offset = offset, limit = min((grense - offset), limitMotOppgave)),
                    ),
                    httpHeaders(),
                )
            oppgaver.addAll(nyeOppgaver.oppgaver)
            offset += limitMotOppgave
        }

        return FinnOppgaveResponseDto(oppgaverOgAntall.antallTreffTotalt, oppgaver)
    }

    fun finnMapper(finnMappeRequest: FinnMappeRequest): FinnMappeResponseDto {
        return getForEntity(buildMappeRequestUri(finnMappeRequest), httpHeaders())
    }

    fun oppdaterOppgave(patchDto: Oppgave): Oppgave? {
        return Result.runCatching {
            patchForEntity<Oppgave>(
                requestUrl(patchDto.id ?: error("Kan ikke finne oppgaveId på oppgaven")),
                patchDto,
                httpHeaders(),
            )
        }.fold(
            onSuccess = { it },
            onFailure = {
                var feilmelding = "Feil ved oppdatering av oppgave for ${patchDto.id}."
                if (it is HttpStatusCodeException) {
                    feilmelding += " Response fra oppgave = ${it.responseBodyAsString}"

                    if (it.statusCode == HttpStatus.CONFLICT) {
                        throw OppslagException(
                            feilmelding,
                            "Oppgave.oppdaterOppgave",
                            OppslagException.Level.LAV,
                            HttpStatus.CONFLICT,
                            it,
                        )
                    }
                }

                throw OppslagException(
                    feilmelding,
                    "Oppgave.oppdaterOppgave",
                    OppslagException.Level.KRITISK,
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    it,
                )
            },
        )
    }

    fun oppdaterEnhet(byttEnhetPatch: OppgaveByttEnhet): Oppgave? {
        return Result.runCatching {
            patchForEntity<Oppgave>(
                requestUrl(byttEnhetPatch.id),
                byttEnhetPatch,
                httpHeaders(),
            )
        }.fold(
            onSuccess = { it },
            onFailure = {
                var feilmelding = "Feil ved bytte av enhet for oppgave for ${byttEnhetPatch.id}."
                if (it is HttpStatusCodeException) {
                    feilmelding += " Response fra oppgave = ${it.responseBodyAsString}"
                }

                throw OppslagException(
                    feilmelding,
                    "Oppgave.byttEnhet",
                    OppslagException.Level.MEDIUM,
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    it,
                )
            },
        )
    }

    fun fjernBehandlesAvApplikasjon(fjernBehandlesAvApplikasjon: OppgaveFjernBehandlesAvApplikasjon): Oppgave? {
        return Result.runCatching {
            patchForEntity<Oppgave>(
                requestUrl(fjernBehandlesAvApplikasjon.id),
                fjernBehandlesAvApplikasjon,
                httpHeaders(),
            )
        }.fold(
            onSuccess = { it },
            onFailure = {
                var feilmelding = "Feil ved fjerning av behandlesAvApplikasjon for ${fjernBehandlesAvApplikasjon.id}."
                val statusCode = if (it is HttpStatusCodeException) {
                    feilmelding += " Response fra oppgave = ${it.responseBodyAsString}"

                    if (it.statusCode == HttpStatus.CONFLICT) {
                        HttpStatus.CONFLICT
                    } else {
                        HttpStatus.INTERNAL_SERVER_ERROR
                    }
                } else {
                    HttpStatus.INTERNAL_SERVER_ERROR
                }

                throw OppslagException(
                    feilmelding,
                    "Oppgave.fjernBehandlesAvApplikasjon",
                    OppslagException.Level.LAV,
                    statusCode,
                    it,
                )
            },
        )
    }

    fun opprettOppgave(dto: Oppgave): Long {
        val uri = UriComponentsBuilder.fromUri(oppgaveBaseUrl).path(PATH_OPPGAVE).build().toUri()
        return Result.runCatching { postForEntity<Oppgave>(uri, dto, httpHeaders()) }
            .map { it.id ?: error("Kan ikke finne oppgaveId på oppgaven $it") }
            .onFailure {
                var feilmelding = "Feil ved oppretting av oppgave for ${dto.aktoerId}."
                if (it is HttpStatusCodeException) {
                    feilmelding += " Response fra oppgave = ${it.responseBodyAsString}"
                }

                throw OppslagException(
                    feilmelding,
                    "Oppgave.opprettOppgave",
                    OppslagException.Level.MEDIUM,
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    it,
                )
            }
            .getOrThrow()
    }

    private fun lagRequestUrlMed(aktoerId: String, journalpostId: String, tema: String): URI {
        return UriComponentsBuilder.fromUri(oppgaveBaseUrl)
            .path(PATH_OPPGAVE)
            .queryParam("aktoerId", aktoerId)
            .queryParam("tema", tema)
            .queryParam("oppgavetype", OPPGAVE_TYPE)
            .queryParam("journalpostId", journalpostId)
            .queryParam("statuskategori", "AAPEN")
            .build()
            .toUri()
    }

    private fun requestUrl(oppgaveId: Long): String {
        return oppgaveBaseUrl + PATH_OPPGAVE
        return UriComponentsBuilder.fromUri(oppgaveBaseUrl).path(PATH_OPPGAVE).pathSegment(oppgaveId.toString()).build()
            .toUri()
    }

    private fun requestOppgaveJson(requestUrl: URI): Oppgave {
        val finnOppgaveResponseDto = getForEntity<FinnOppgaveResponseDto>(requestUrl, httpHeaders())
            ?: error("Response fra FinnOppgave er null")
        if (finnOppgaveResponseDto.oppgaver.isEmpty()) {
            throw OppslagException(
                "Ingen oppgaver funnet for $requestUrl",
                "oppgave",
                OppslagException.Level.MEDIUM,
                HttpStatus.NOT_FOUND,
            )
        }
        if (finnOppgaveResponseDto.oppgaver.size > 1) {
            logger.warn("Returnerte mer enn 1 oppgave, antall: ${finnOppgaveResponseDto.oppgaver.size}, oppgave: $requestUrl")
        }
        return finnOppgaveResponseDto.oppgaver[0]
    }

    private fun httpHeaders(): HttpHeaders = HttpHeaders().apply {
        add(X_CORRELATION_ID, MDC.get(MDCConstants.MDC_CALL_ID))
    }

    companion object {

        private const val PATH_OPPGAVE = "/api/v1/oppgaver"
        private const val PATH_MAPPE = "/api/v1/mapper"
        private const val OPPGAVE_TYPE = "BEH_SAK"
        private const val X_CORRELATION_ID = "X-Correlation-ID"
    }
}
