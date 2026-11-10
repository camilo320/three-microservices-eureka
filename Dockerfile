# syntax=docker/dockerfile:1

# Compila todos los módulos una sola vez y reutiliza la caché de Maven.
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /workspace
COPY . .

RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -ntp -pl ms-orden,ms-driver,ms-asignacion,ms-auth,server -am package -Dmaven.test.skip=true

FROM eclipse-temurin:17-jre-alpine AS runtime

RUN addgroup -S spring && adduser -S spring -G spring
WORKDIR /app
RUN mkdir -p /app/uploads && chown -R spring:spring /app

COPY --from=build /workspace/ms-orden/target/ms-orden-1.0-SNAPSHOT.jar /app/ms-orden.jar
COPY --from=build /workspace/ms-driver/target/ms-driver-1.0-SNAPSHOT.jar /app/ms-driver.jar
COPY --from=build /workspace/ms-asignacion/target/ms-asignacion-1.0-SNAPSHOT.jar /app/ms-asignacion.jar
COPY --from=build /workspace/ms-auth/target/ms-auth-1.0-SNAPSHOT.jar /app/ms-auth.jar
COPY --from=build /workspace/server/target/server-1.0-SNAPSHOT.jar /app/server.jar

USER spring:spring
EXPOSE 8081 8082 8083 8084 8761

ENTRYPOINT ["java", "-jar"]
CMD ["/app/server.jar"]
