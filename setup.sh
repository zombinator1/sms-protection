#!/bin/sh

docker compose down
docker compose up -d
# generate JOOQ sources
mvn generate-sources
