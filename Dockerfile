FROM openjdk:11-jdk-slim
COPY build/libs/irc-music-downloader-1.0-SNAPSHOT-uber.jar /root/marvin.jar
#COPY dist/marvin /root/webroot
CMD java -cp /root/marvin.jar marvin.Client

