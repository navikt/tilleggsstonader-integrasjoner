import DatoFormat.NORSK_DATO_TEKSTLIG_MÅNED
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object DatoFormat {
    val LOCALE_NORGE = Locale.forLanguageTag("no-NO")
    val GOSYS_DATE_TIME = DateTimeFormatter.ofPattern("dd.MM.yyyy' 'HH:mm")
    val NORSK_DATO_TEKSTLIG_MÅNED = DateTimeFormatter.ofPattern("dd. MMMM yyyy", LOCALE_NORGE)
}

fun LocalDate.norskDatoTekstligMåned() = this.format(NORSK_DATO_TEKSTLIG_MÅNED)
