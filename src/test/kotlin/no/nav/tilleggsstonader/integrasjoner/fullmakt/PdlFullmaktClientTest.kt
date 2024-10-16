import no.nav.tilleggsstonader.integrasjoner.fullmakt.FullmaktIdentPdlRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Base64

class PdlFullmaktClientTest {

    @Test
    fun `ident skal være encodet som base64`() {
        val ident = "12345678910"

        val request = FullmaktIdentPdlRequest.create(ident)

        val forventetIdent = Base64.getEncoder().encodeToString(ident.toByteArray())
        assertEquals(forventetIdent, request.ident)
    }
}