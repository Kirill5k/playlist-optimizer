FROM openjdk:11.0.4-slim

WORKDIR /
RUN mkdir -p /static
COPY frontend/dist/ /static/
COPY core/target/scala-2.13/playlist-optimizer.jar po.jar
EXPOSE $SERVER_PORT

CMD ["java","-jar","/po.jar"]
