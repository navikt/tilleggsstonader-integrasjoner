package no.nav.tilleggsstonader.integrasjoner.ytelse

import io.mockk.called
import io.mockk.verify
import no.nav.tilleggsstonader.integrasjoner.IntegrationTest
import no.nav.tilleggsstonader.integrasjoner.aap.AAPClient
import no.nav.tilleggsstonader.integrasjoner.ensligforsørger.EnsligForsørgerClient
import no.nav.tilleggsstonader.integrasjoner.mocks.AAPClientTestConfig.Companion.resetMock
import no.nav.tilleggsstonader.integrasjoner.mocks.EnsligForsørgerClientTestConfig.Companion.resetMock
import no.nav.tilleggsstonader.kontrakter.ytelse.TypeYtelsePeriode
import no.nav.tilleggsstonader.kontrakter.ytelse.YtelsePerioderRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate

class YtelseServiceTest : IntegrationTest() {

    @Autowired
    lateinit var ytelseService: YtelseService

    @Autowired
    lateinit var aapClient: AAPClient

    @Autowired
    lateinit var ensligForsørgerClient: EnsligForsørgerClient

    @AfterEach
    override fun tearDown() {
        super.tearDown()
        resetMock(aapClient)
        resetMock(ensligForsørgerClient)
    }

    @Test
    fun `skal cachea svar fra klienten`() {
        ytelseService.hentYtelser(ytelsePerioderRequest())
        ytelseService.hentYtelser(ytelsePerioderRequest())

        verify(exactly = 1) { aapClient.hentPerioder(any(), any(), any()) }
        verify(exactly = 1) { ensligForsørgerClient.hentPerioder(any(), any(), any()) }
    }

    @Test
    fun `skal gjøre nytt kal hvis et felt endrer seg `() {
        ytelseService.hentYtelser(ytelsePerioderRequest())
        ytelseService.hentYtelser(ytelsePerioderRequest(ident = "ident2"))
        ytelseService.hentYtelser(ytelsePerioderRequest(fom = LocalDate.now().plusDays(1)))
        ytelseService.hentYtelser(ytelsePerioderRequest(tom = LocalDate.now().plusDays(1)))

        verify(exactly = 4) { aapClient.hentPerioder(any(), any(), any()) }
        verify(exactly = 4) { ensligForsørgerClient.hentPerioder(any(), any(), any()) }
    }

    @Test
    fun `skal kun hente perioder fra aap hvis det er ønskelig`() {
        val dto = ytelseService.hentYtelser(ytelsePerioderRequest(typer = listOf(TypeYtelsePeriode.AAP)))

        assertThat(dto.perioder.map { it.type }).containsOnly(TypeYtelsePeriode.AAP)

        verify(exactly = 1) { aapClient.hentPerioder(any(), any(), any()) }
        verify { ensligForsørgerClient wasNot called }
    }

    @Test
    fun `skal kun hente perioder fra enslig forsørger hvis det er ønskelig`() {
        val dto = ytelseService.hentYtelser(ytelsePerioderRequest(typer = listOf(TypeYtelsePeriode.ENSLIG_FORSØRGER)))

        assertThat(dto.perioder.map { it.type }).containsOnly(TypeYtelsePeriode.ENSLIG_FORSØRGER)

        verify(exactly = 1) { ensligForsørgerClient.hentPerioder(any(), any(), any()) }
        verify { aapClient wasNot called }
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