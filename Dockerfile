FROM mcr.microsoft.com/java/jdk:8u192-zulu-alpine
COPY build/libs/irc-music-downloader-1.0-SNAPSHOT-uber.jar /root/marvin.jar
COPY src/main/js/dist/marvin /root/webroot
CMD java -cp /root/marvin.jar marvin.Client

