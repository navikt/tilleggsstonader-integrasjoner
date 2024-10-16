import no.nav.tilleggsstonader.integrasjoner.fullmakt.FullmaktIdentPdlRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PdlFullmaktClientTest {

    @Test
    fun `ident skal være encodet som base64`() {
        val ident = "12345678910"
        val identSomBase64 = "MTIzNDU2Nzg5MTA="

        val request = FullmaktIdentPdlRequest.create(ident)

        assertEquals(identSomBase64, request.ident)
    }
}