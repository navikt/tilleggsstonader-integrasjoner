package no.nav.tilleggsstonader.integrasjoner.oppgave

import no.nav.tilleggsstonader.integrasjoner.infrastruktur.exception.OppslagException
import no.nav.tilleggsstonader.integrasjoner.util.QueryParamUtil.medQueryParams
import no.nav.tilleggsstonader.integrasjoner.util.QueryParamUtil.toQueryParams
import no.nav.tilleggsstonader.integrasjoner.util.QueryParams
import no.nav.tilleggsstonader.kontrakter.oppgave.FinnMappeRequest
import no.nav.tilleggsstonader.kontrakter.oppgave.FinnMappeResponseDto
import no.nav.tilleggsstonader.kontrakter.oppgave.FinnOppgaveRequest
import no.nav.tilleggsstonader.kontrakter.oppgave.FinnOppgaveResponseDto
import no.nav.tilleggsstonader.kontrakter.oppgave.Oppgave
import no.nav.tilleggsstonader.libs.http.client.AbstractRestClient
import no.nav.tilleggsstonader.libs.log.mdc.MDCConstants
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import kotlin.math.min

@Component
class OppgaveClient(
    @Value("\${clients.oppgave.uri}") private val oppgaveBaseUrl: URI,
    @Qualifier("azure") restTemplate: RestTemplate,
) : AbstractRestClient(restTemplate) {
    fun finnOppgaveMedId(oppgaveId: Long): Oppgave {
        try {
            return getForEntity(oppgaveIdUrl, httpHeaders(), oppgaveIdUriVariables(oppgaveId))
        } catch (e: HttpClientErrorException.NotFound) {
            throw OppslagException(
                "Finner ikke oppgave med id=$oppgaveId",
                "Oppgave.finnOppgaveMedId",
                OppslagException.Level.KRITISK,
                HttpStatus.INTERNAL_SERVER_ERROR,
            )
        }
    }

    fun buildOppgaveRequestUri(queryParams: QueryParams): String =
        UriComponentsBuilder
            .fromUri(oppgaveBaseUrl)
            .path(PATH_OPPGAVE)
            .medQueryParams(queryParams)
            .encode()
            .toUriString()

    fun buildMappeRequestUri(queryParams: QueryParams): String =
        UriComponentsBuilder
            .fromUri(oppgaveBaseUrl)
            .path(PATH_MAPPE)
            .medQueryParams(queryParams)
            .encode()
            .toUriString()

    fun finnOppgaver(finnOppgaveRequest: FinnOppgaveRequest): FinnOppgaveResponseDto {
        val oppgaveRequest = finnOppgaveRequest.toDto()
        var offset = oppgaveRequest.offset

        var queryParams = toQueryParams(oppgaveRequest)
        val oppgaverOgAntall = finnOppgave(queryParams)
        val oppgaver: MutableList<Oppgave> = oppgaverOgAntall.oppgaver.toMutableList()
        val grense =
            if (finnOppgaveRequest.limit == null) {
                oppgaverOgAntall.antallTreffTotalt
            } else {
                oppgaveRequest.offset + finnOppgaveRequest.limit!!
            }
        offset += LIMIT_MOT_OPPGAVE

        while (offset < grense) {
            queryParams =
                toQueryParams(oppgaveRequest.copy(offset = offset, limit = min((grense - offset), LIMIT_MOT_OPPGAVE)))
            val nyeOppgaver = finnOppgave(queryParams)
            oppgaver.addAll(nyeOppgaver.oppgaver)
            offset += LIMIT_MOT_OPPGAVE
        }

        return FinnOppgaveResponseDto(oppgaverOgAntall.antallTreffTotalt, oppgaver)
    }

    private fun finnOppgave(queryParams: QueryParams) =
        getForEntity<FinnOppgaveResponseDto>(
            uri = buildOppgaveRequestUri(queryParams),
            httpHeaders = httpHeaders(),
            uriVariables = queryParams.tilUriVariables(),
        )

    fun finnMapper(finnMappeRequest: FinnMappeRequest): FinnMappeResponseDto {
        val queryParams = toQueryParams(finnMappeRequest)
        return getForEntity(buildMappeRequestUri(queryParams), httpHeaders(), queryParams.tilUriVariables())
    }

    fun oppdaterOppgave(patchDto: Oppgave): Oppgave =
        Result
            .runCatching {
                patchForEntity<Oppgave>(
                    oppgaveIdUrl,
                    patchDto,
                    httpHeaders(),
                    oppgaveIdUriVariables(patchDto.id),
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
                                it.statusCode,
                                it,
                            )
                        } else if (it.statusCode == HttpStatus.BAD_REQUEST) {
                            throw OppslagException(
                                feilmelding,
                                "Oppgave.oppdaterOppgave",
                                OppslagException.Level.MEDIUM,
                                HttpStatus.BAD_REQUEST,
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

    fun fjernBehandlesAvApplikasjon(fjernBehandlesAvApplikasjon: OppgaveFjernBehandlesAvApplikasjon): Oppgave? =
        Result
            .runCatching {
                patchForEntity<Oppgave>(
                    oppgaveIdUrl,
                    fjernBehandlesAvApplikasjon,
                    httpHeaders(),
                    oppgaveIdUriVariables(fjernBehandlesAvApplikasjon.id),
                )
            }.fold(
                onSuccess = { it },
                onFailure = {
                    var feilmelding = "Feil ved fjerning av behandlesAvApplikasjon for ${fjernBehandlesAvApplikasjon.id}."
                    val statusCode =
                        if (it is HttpStatusCodeException) {
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

    fun opprettOppgave(dto: OpprettOppgaveRequestDto): Long {
        val uri = UriComponentsBuilder.fromUri(oppgaveBaseUrl).path(PATH_OPPGAVE).toUriString()
        return Result
            .runCatching { postForEntity<Oppgave>(uri, dto, httpHeaders()) }
            .map { it.id }
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
            }.getOrThrow()
    }

    private fun oppgaveIdUriVariables(oppgaveId: Long) = mapOf("id" to oppgaveId)

    private val oppgaveIdUrl =
        UriComponentsBuilder
            .fromUri(oppgaveBaseUrl)
            .path(PATH_OPPGAVE)
            .pathSegment("{id}")
            .encode()
            .toUriString()

    private fun httpHeaders(): HttpHeaders =
        HttpHeaders().apply {
            add(X_CORRELATION_ID, MDC.get(MDCConstants.MDC_CALL_ID))
        }

    companion object {
        private const val PATH_OPPGAVE = "/api/v1/oppgaver"
        private const val PATH_MAPPE = "/api/v1/mapper"
        private const val X_CORRELATION_ID = "X-Correlation-ID"
    }
}
