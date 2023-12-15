FROM ghcr.io/navikt/baseimages/temurin:21

ENV APPLICATION_NAME=tilleggsstonader-integrasjoner

EXPOSE 8080
COPY build/libs/*.jar ./

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"
