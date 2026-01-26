FROM maven:4.0.0-rc-5-eclipse-temurin-21 AS source

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY ./ ./

RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /newunimol

COPY --from=source /app/target/newunimol-1.0.2-SNAPSHOT.jar newunimol.jar

EXPOSE 23109

ENTRYPOINT [ "java", "-jar", "newunimol.jar" ]