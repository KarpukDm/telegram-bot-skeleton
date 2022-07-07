FROM openjdk:13-jdk-alpine

VOLUME /tmp /mnt

ARG JAR_FILE

COPY ${JAR_FILE} tgbot_skeleton.jar

EXPOSE 8080

ENTRYPOINT [\
"java",\
"-Djava.security.egd=file:/dev/./urandom",\
"-jar",\
"/tgbot_skeleton.jar"\
]
