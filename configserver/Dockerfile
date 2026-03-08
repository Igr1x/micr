FROM amazoncorretto:17-alpine3.21 as build
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

RUN mkdir -p target/dependency \
    && (cd target/dependency; jar -xf /app.jar)

FROM amazoncorretto:17-alpine3.21
VOLUME /tmp

ARG DEPENDENCY=/target/dependency

COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app

ENTRYPOINT ["java","-cp","app:app/lib/*","ru.varnavskii.configserver.ConfigurationServerApplication"]