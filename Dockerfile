FROM openjdk:11-jdk-slim
COPY build/libs/irc-music-downloader-1.0-SNAPSHOT-uber.jar /root/marvin.jar
#COPY dist/marvin /root/webroot
CMD java \
    -Dcom.sun.management.jmxremote.port=9010 \
    -Dcom.sun.management.jmxremote.local.only=false \
    -Dcom.sun.management.jmxremote.authenticate=false \
    -Dcom.sun.management.jmxremote.ssl=false \
    -cp /root/marvin.jar marvin.Client

EXPOSE 8081 9010
