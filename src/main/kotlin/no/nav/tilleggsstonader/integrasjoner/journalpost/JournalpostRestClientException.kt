package no.nav.tilleggsstonader.integrasjoner.journalpost

import no.nav.tilleggsstonader.integrasjoner.journalpost.internal.SafJournalpostRequest

class JournalpostRestClientException(message: String?, cause: Throwable?, val journalpostId: String) :
    RuntimeException(message, cause)

class JournalpostRequestException(
    message: String?,
    cause: Throwable?,
    val safJournalpostRequest: SafJournalpostRequest,
) :
    RuntimeException(message, cause)

class JournalpostForbiddenException(message: String?, cause: Throwable? = null) : RuntimeException(message, cause)

class JournalpostIkkeFunnetException(val id: String) : RuntimeException()
