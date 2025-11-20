package no.nav.tilleggsstonader.integrasjoner.ytelse

import io.mockk.called
import io.mockk.every
import io.mockk.verify
import no.nav.tilleggsstonader.integrasjoner.IntegrationTest
import no.nav.tilleggsstonader.integrasjoner.aap.AAPClient
import no.nav.tilleggsstonader.integrasjoner.aap.AAPPerioderResponse
import no.nav.tilleggsstonader.integrasjoner.dagpenger.DagpengerClient
import no.nav.tilleggsstonader.integrasjoner.dagpenger.DagpengerPerioderResponse
import no.nav.tilleggsstonader.integrasjoner.ensligforsørger.EnsligForsørgerClient
import no.nav.tilleggsstonader.integrasjoner.ensligforsørger.EnsligForsørgerPerioderResponse
import no.nav.tilleggsstonader.integrasjoner.etterlatte.EtterlatteClient
import no.nav.tilleggsstonader.integrasjoner.etterlatte.Samordningsvedtak
import no.nav.tilleggsstonader.integrasjoner.mocks.AAPClientTestConfig.Companion.resetMock
import no.nav.tilleggsstonader.integrasjoner.mocks.DagpengerClientTestConfig.Companion.resetMock
import no.nav.tilleggsstonader.integrasjoner.mocks.EnsligForsørgerClientTestConfig.Companion.resetMock
import no.nav.tilleggsstonader.integrasjoner.mocks.EtterlatteClientTestConfig.Companion.resetMock
import no.nav.tilleggsstonader.integrasjoner.mocks.TiltakspengerClientTestConfig.Companion.resetMock
import no.nav.tilleggsstonader.integrasjoner.tiltakspenger.TiltakspengerClient
import no.nav.tilleggsstonader.integrasjoner.tiltakspenger.TiltakspengerDetaljerResponse
import no.nav.tilleggsstonader.integrasjoner.tiltakspenger.TiltakspengerPerioderResponse
import no.nav.tilleggsstonader.kontrakter.ytelse.ResultatKilde
import no.nav.tilleggsstonader.kontrakter.ytelse.TypeYtelsePeriode
import no.nav.tilleggsstonader.kontrakter.ytelse.YtelsePerioderRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.client.postForEntity
import java.time.LocalDate

class YtelseServiceTest : IntegrationTest() {
    @Autowired
    lateinit var ytelseService: YtelseService

    @Autowired
    lateinit var aapClient: AAPClient

    @Autowired
    lateinit var dagpengerClient: DagpengerClient

    @Autowired
    lateinit var ensligForsørgerClient: EnsligForsørgerClient

    @Autowired
    lateinit var etterlatteClient: EtterlatteClient

    @Autowired
    lateinit var tiltakspengerClient: TiltakspengerClient

    @AfterEach
    override fun tearDown() {
        super.tearDown()
        resetMock(aapClient)
        resetMock(dagpengerClient)
        resetMock(tiltakspengerClient)
        resetMock(ensligForsørgerClient)
        resetMock(etterlatteClient)
    }

    @Test
    fun `skal cache svar fra klienten`() {
        ytelseService.hentYtelser(ytelsePerioderRequest())
        ytelseService.hentYtelser(ytelsePerioderRequest())

        verify(exactly = 1) { aapClient.hentPerioder(any(), any(), any()) }
        verify(exactly = 1) { dagpengerClient.hentPerioder(any(), any(), any()) }
        verify(exactly = 1) { ensligForsørgerClient.hentPerioder(any(), any(), any()) }
        verify(exactly = 1) { etterlatteClient.hentPerioder(any(), any()) }
        verify(exactly = 1) { tiltakspengerClient.hentPerioder(any(), any(), any()) }
        verify(exactly = 1) { tiltakspengerClient.hentDetaljer(any(), any(), any()) }
    }

    @Test
    fun `skal gjøre nytt kall hvis et felt i requesten endrer seg `() {
        ytelseService.hentYtelser(ytelsePerioderRequest())
        ytelseService.hentYtelser(ytelsePerioderRequest(ident = "ident2"))
        ytelseService.hentYtelser(ytelsePerioderRequest(fom = LocalDate.now().plusDays(1)))
        ytelseService.hentYtelser(ytelsePerioderRequest(tom = LocalDate.now().plusDays(1)))

        verify(exactly = 4) { aapClient.hentPerioder(any(), any(), any()) }
        verify(exactly = 4) { dagpengerClient.hentPerioder(any(), any(), any()) }
        verify(exactly = 4) { ensligForsørgerClient.hentPerioder(any(), any(), any()) }
        verify(exactly = 4) { etterlatteClient.hentPerioder(any(), any()) }
        verify(exactly = 4) { tiltakspengerClient.hentPerioder(any(), any(), any()) }
        verify(exactly = 4) { tiltakspengerClient.hentDetaljer(any(), any(), any()) }
    }

    @Test
    fun `skal kun hente perioder fra aap hvis det forespørres`() {
        val dto = ytelseService.hentYtelser(ytelsePerioderRequest(typer = listOf(TypeYtelsePeriode.AAP)))

        assertThat(dto.perioder.map { it.type }).containsOnly(TypeYtelsePeriode.AAP)

        verify(exactly = 1) { aapClient.hentPerioder(any(), any(), any()) }
        verify { dagpengerClient wasNot called }
        verify { ensligForsørgerClient wasNot called }
        verify { etterlatteClient wasNot called }
        verify { tiltakspengerClient wasNot called }
    }

    @Test
    fun `skal kun hente perioder fra enslig forsørger hvis det forespørres`() {
        val dto = ytelseService.hentYtelser(ytelsePerioderRequest(typer = listOf(TypeYtelsePeriode.ENSLIG_FORSØRGER)))

        assertThat(dto.perioder.map { it.type }).containsOnly(TypeYtelsePeriode.ENSLIG_FORSØRGER)

        verify(exactly = 1) { ensligForsørgerClient.hentPerioder(any(), any(), any()) }
        verify { aapClient wasNot called }
        verify { dagpengerClient wasNot called }
        verify { etterlatteClient wasNot called }
        verify { tiltakspengerClient wasNot called }
    }

    @Test
    fun `skal kun hente perioder fra etterlatte hvis det forespørres`() {
        val dto = ytelseService.hentYtelser(ytelsePerioderRequest(typer = listOf(TypeYtelsePeriode.OMSTILLINGSSTØNAD)))

        assertThat(dto.perioder.map { it.type }).containsOnly(TypeYtelsePeriode.OMSTILLINGSSTØNAD)

        verify(exactly = 1) { etterlatteClient.hentPerioder(any(), any()) }
        verify { aapClient wasNot called }
        verify { dagpengerClient wasNot called }
        verify { ensligForsørgerClient wasNot called }
        verify { tiltakspengerClient wasNot called }
    }

    @Test
    fun `skal kun hente perioder fra dagpenger hvis det forespørres`() {
        val dto = ytelseService.hentYtelser(ytelsePerioderRequest(typer = listOf(TypeYtelsePeriode.DAGPENGER)))

        assertThat(dto.perioder.map { it.type }).containsOnly(TypeYtelsePeriode.DAGPENGER)

        verify(exactly = 1) { dagpengerClient.hentPerioder(any(), any(), any()) }
        verify { aapClient wasNot called }
        verify { ensligForsørgerClient wasNot called }
        verify { etterlatteClient wasNot called }
        verify { tiltakspengerClient wasNot called }
    }

    @Test
    fun `skal kun hente perioder fra tiltakspenger (Arena) hvis det forespørres`() {
        val dto = ytelseService.hentYtelser(ytelsePerioderRequest(typer = listOf(TypeYtelsePeriode.TILTAKSPENGER_ARENA)))
        assertThat(dto.perioder.map { it.type }).containsOnly(TypeYtelsePeriode.TILTAKSPENGER_ARENA)
        verify(exactly = 1) { tiltakspengerClient.hentPerioder(any(), any(), any()) }
        verify { aapClient wasNot called }
        verify { ensligForsørgerClient wasNot called }
        verify { etterlatteClient wasNot called }
        verify { dagpengerClient wasNot called }
    }

    @Test
    fun `skal kun hente perioder fra tiltakspenger (TPSAK) hvis det er ønskelig`() {
        val dto = ytelseService.hentYtelser(ytelsePerioderRequest(typer = listOf(TypeYtelsePeriode.TILTAKSPENGER_TPSAK)))
        assertThat(dto.perioder.map { it.type }).containsOnly(TypeYtelsePeriode.TILTAKSPENGER_TPSAK)
        verify(exactly = 1) { tiltakspengerClient.hentDetaljer(any(), any(), any()) }
        verify { aapClient wasNot called }
        verify { ensligForsørgerClient wasNot called }
        verify { etterlatteClient wasNot called }
        verify { dagpengerClient wasNot called }
    }

    @Test
    fun `skal håndtere feil fra en klient`() {
        every { aapClient.hentPerioder(any(), any(), any()) } answers {
            restTemplate
                .postForEntity<AAPPerioderResponse>("http://localhost:1234", null)
                .body!!
        }

        every { dagpengerClient.hentPerioder(any(), any(), any()) } answers {
            restTemplate
                .postForEntity<DagpengerPerioderResponse>("http://localhost:1234", null)
                .body!!
        }

        every { ensligForsørgerClient.hentPerioder(any(), any(), any()) } answers {
            restTemplate
                .postForEntity<EnsligForsørgerPerioderResponse>("http://localhost:1234", null)
                .body!!
        }

        every { etterlatteClient.hentPerioder(any(), any()) } answers {
            restTemplate
                .postForEntity<List<Samordningsvedtak>>("http://localhost:1234", null)
                .body!!
        }
        every { tiltakspengerClient.hentPerioder(any(), any(), any()) } answers {
            restTemplate.postForEntity<List<TiltakspengerPerioderResponse>>("http://localhost:1234", null).body!!
        }
        every { tiltakspengerClient.hentDetaljer(any(), any(), any()) } answers {
            restTemplate.postForEntity<List<TiltakspengerDetaljerResponse>>("http://localhost:1234", null).body!!
        }

        val dto = ytelseService.hentYtelser(ytelsePerioderRequest())

        assertThat(dto.perioder.isEmpty())
        assertThat(dto.kildeResultat).hasSize(6)

        val typeYtelsePeriode = dto.kildeResultat.map { it.type }
        assertThat(typeYtelsePeriode).containsExactlyInAnyOrderElementsOf(TypeYtelsePeriode.entries)

        dto.kildeResultat.forEach {
            assertThat(it.resultat).isEqualTo(ResultatKilde.FEILET)
        }
    }

    private fun ytelsePerioderRequest(
        ident: String = "ident",
        fom: LocalDate = LocalDate.now(),
        tom: LocalDate = LocalDate.now(),
        typer: List<TypeYtelsePeriode> = TypeYtelsePeriode.entries,
    ) = YtelsePerioderRequest(
        ident = ident,
        fom = fom,
        tom = tom,
        typer = typer,
    )
}
