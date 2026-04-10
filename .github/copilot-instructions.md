# Copilot Instructions – tilleggsstonader-integrasjoner

## Bygge, teste og lint

```bash
./gradlew build            # Bygg og kjør alle tester
./gradlew test             # Kjør tester
./gradlew spotlessApply    # Fiks ktlint-feil automatisk
./gradlew spotlessCheck    # Sjekk linting uten å endre

# Kjør én enkelt test
./gradlew test --tests "no.nav.tilleggsstonader.integrasjoner.ytelse.YtelseServiceTest"

# Bygg uten lint
./gradlew build -PskipLint
```

Appen kjøres med `./gradlew bootRun`. Port `8080` i prod, `9193` i integrasjonstester.

## Arkitektur

Appen er en Spring Boot-integrasjonstjeneste i Kotlin (Java 21). Den fungerer som en proxy/aggregator mellom tilleggsstønader-appene og eksterne NAV-tjenester.

**Pakkestruktur per domene** – hvert domene har:
- `*Client` – kaller ekstern tjeneste via `RestTemplate`
- `*Service` – forretningslogikk og aggregering
- `*Controller` – REST-endepunkt

Domenene er: `aap`, `arena`, `aktiviteter`, `azure`, `dagpenger`, `dokarkiv`, `dokdist`, `ensligforsørger`, `etterlatte`, `fullmakt`, `journalpost`, `oppgave`, `saksbehandler`, `tiltakspenger`, `ytelse`.

**To typer endepunkter:**
- `/api/...` – interne endepunkter, kun for tilleggsstønader-apper (Azure AD)
- `/api/ekstern/...` – eksterne endepunkter, aksepterer TokenX (f.eks. søknadsflyt)

**Tilgangskontroll:**
- `ValiderKallErFraTilleggsstønaderFilter` blokkerer alle ikke-eksterne kall der `azp_name`/`client_id` ikke matcher `gcp:tilleggsstonader:tilleggsstonader-*`
- Ekstern tilgang kontrolleres manuelt med `SikkerhetsContext.kallKommerFra(EksternApplikasjon.*)` og `EksternBrukerUtils`
- Alle kontrollere annoteres med `@ProtectedWithClaims(issuer = "azuread")` eller `"tokenx"`

**YtelseService** er et sentralt eksempel: den aggregerer ytelser fra 5+ klienter parallelt vha. `VirtualThreadUtil.parallelt()` (virtual threads), og returnerer delvis resultat selv om enkeltkilder feiler.

## Nøkkelkonvensjoner

### RestTemplate og auth
Alle klienter bruker `@Qualifier("azure") RestTemplate` for machine-to-machine (client_credentials). Konfigurasjon er i `application.yaml` under `no.nav.security.jwt.client.registration` og `clients.*`.

### Parallell kjøring
Bruk `Collection<() -> T>.parallelt()` fra `VirtualThreadUtil` for parallelle kall med MDC-propagering:
```kotlin
listOf(fn1, fn2, fn3).parallelt()
```

### Caching
Fire `CacheManager`-bønner via `@Qualifier`:
- (default) – 60 minutter
- `"shortCache"` – 10 minutter (brukes for ytelser)
- `"kodeverkCache"` – 24 timer
- `"longCache"` – 7 dager

Bruk `cacheManager.getValue("cache-navn", cacheKey) { ... }` fra `tilleggsstonader-libs`.

### Datakontakter
Domeneobjekter/kontrakter (f.eks. `YtelsePerioderDto`, `Oppgave`, `OpprettOppgaveRequest`) importeres fra `no.nav.tilleggsstonader.kontrakter`-biblioteket – ikke definer disse lokalt.

### Logging av sensitiv data
Bruk `secureLogger` (fra `tilleggsstonader-libs`) for å logge PII (fnr, ident osv.):
```kotlin
secureLogger.error("Feil for ident=$ident", e)
logger.error("Feil, se secure logs")
```

### Ekstern tilgang og `EksternApplikasjon`
Applikasjoner med ekstern tilgang registreres i `EksternApplikasjon`-enumen med `namespaceAppNavn`.

## Integrasjonstester

Alle integrasjonstester arver `IntegrationTest`. Testen starter hele Spring-konteksten med WireMock (port 28085) og MockOAuth2Server.

**Test-profiler** aktiveres i `IntegrationTest`: `mock-aap`, `mock-dagpenger`, `mock-enslig`, `mock-etterlatte`, `mock-tiltakspenger`, `mock-az-ad`. Disse erstatter klient-bønner med MockK-mocks via `@Profile("mock-*")` konfigurasjon under `src/test/kotlin/.../mocks/`.

For å sette opp token i test:
```kotlin
headers.setBearerAuth(onBehalfOfToken(saksbehandler = "Z123456"))
headers.setBearerAuth(tokenX(ident = "12345678901"))
```

Cache tømmes automatisk mellom hver test.
