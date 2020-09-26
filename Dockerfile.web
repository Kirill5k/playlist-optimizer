FROM openjdk:11.0.4-slim

WORKDIR /
RUN mkdir -p /static
COPY built-frontend/ /static/
COPY built-backend/playlist-optimizer.jar po.jar
EXPOSE $SERVER_PORT

CMD ["java","-jar","/po.jar"]
