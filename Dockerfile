FROM gradle:7.1-jdk16 AS build
ENV APP_HOME=/OnlineRoulette/
WORKDIR $APP_HOME
COPY . ./
RUN gradle build

FROM openjdk:16.0.2
ENV APP_HOME=/OnlineRoulette/
WORKDIR /OnlineRoulette
COPY --from=build $APP_HOME/build/OnlineRoulette-1.0-SNAPSHOT.jar ./
COPY src/main/resources ./resources

ENTRYPOINT ["java", "-jar", "OnlineRoulette-1.0-SNAPSHOT.jar"]