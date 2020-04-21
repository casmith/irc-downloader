#/bin/bash
gradle uberJar
docker build -t casmith/marvinbot:latest .
docker push casmith/marvinbot:latest