package no.nav.tilleggsstonader.integrasjoner.ytelse

import io.mockk.verify
import no.nav.tilleggsstonader.integrasjoner.IntegrationTest
import no.nav.tilleggsstonader.integrasjoner.aap.AAPClient
import no.nav.tilleggsstonader.integrasjoner.ensligforsørger.EnsligForsørgerClient
import no.nav.tilleggsstonader.integrasjoner.mocks.AAPClientTestConfig.Companion.resetMock
import no.nav.tilleggsstonader.integrasjoner.mocks.EnsligForsørgerClientTestConfig.Companion.resetMock
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
        ytelseService.hentYtelser(HentYtelserData("ident", LocalDate.now(), LocalDate.now()))
        ytelseService.hentYtelser(HentYtelserData("ident", LocalDate.now(), LocalDate.now()))

        verify(exactly = 0) { aapClient.hentPerioder(any(), any(), any()) }
        verify(exactly = 1) { ensligForsørgerClient.hentPerioder(any(), any(), any()) }
    }

    @Test
    fun `skal gjøre nytt kal hvis et felt endrer seg `() {
        ytelseService.hentYtelser(HentYtelserData("ident", LocalDate.now(), LocalDate.now()))
        ytelseService.hentYtelser(HentYtelserData("ident2", LocalDate.now(), LocalDate.now()))
        ytelseService.hentYtelser(HentYtelserData("ident", LocalDate.now().plusDays(1), LocalDate.now()))
        ytelseService.hentYtelser(HentYtelserData("ident", LocalDate.now(), LocalDate.now().plusDays(1)))

        verify(exactly = 0) { aapClient.hentPerioder(any(), any(), any()) }
        verify(exactly = 4) { ensligForsørgerClient.hentPerioder(any(), any(), any()) }
    }
}
