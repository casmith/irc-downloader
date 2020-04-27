#/bin/bash
gradle dist \
  && docker build -t casmith/marvinbot:latest . \
  && docker push casmith/marvinbot:latest
