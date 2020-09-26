FROM openjdk:11.0.4-slim

RUN mkdir -p /static
COPY frontend/dist/ /static/
COPY core/target/scala-2.13/playlist-optimizer.jar po.jar

ENTRYPOINT ["java","-jar","/po.jar"]