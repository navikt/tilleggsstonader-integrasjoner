package no.nav.tilleggsstonader.integrasjoner

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import no.nav.tilleggsstonader.integrasjoner.util.TokenUtil
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.cache.CacheManager
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.client.RestTemplate

@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [App::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(
    "integrasjonstest",
    "mock-oauth",
    "mock-aap",
    "mock-enslig",
)
@EnableMockOAuth2Server
abstract class IntegrationTest {

    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    @Qualifier("utenAuth")
    protected lateinit var restTemplate: RestTemplate
    protected val headers = HttpHeaders()

    @LocalServerPort
    private var port: Int? = 0

    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private lateinit var mockOAuth2Server: MockOAuth2Server

    @Autowired
    private lateinit var cacheManagers: List<CacheManager>

    protected val listAppender = initLoggingEventListAppender()
    protected val loggingEvents: MutableList<ILoggingEvent> = listAppender.list

    @AfterEach
    fun tearDown() {
        loggingEvents.clear()
        headers.clear()
        clearClientMocks()
        clearCache()
    }

    private fun clearCache() {
        cacheManagers.forEach { cacheManager ->
            cacheManager.cacheNames.mapNotNull { cacheName -> cacheManager.getCache(cacheName) }
                .forEach { cache -> cache.clear() }
        }
    }

    private fun clearClientMocks() {
    }

    protected fun localhost(path: String): String {
        return "$LOCALHOST$port/$path"
    }

    protected fun onBehalfOfToken(
        saksbehandler: String = "julenissen",
    ): String {
        return TokenUtil.onBehalfOfToken(mockOAuth2Server, "role1", saksbehandler)
    }

    companion object {
        private const val LOCALHOST = "http://localhost:"

        protected fun initLoggingEventListAppender(): ListAppender<ILoggingEvent> {
            val listAppender = ListAppender<ILoggingEvent>()
            listAppender.start()
            return listAppender
        }
    }
}
