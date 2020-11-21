FROM adoptopenjdk/openjdk15-openj9:debianslim-jre

WORKDIR /
RUN mkdir -p /static
COPY built-frontend/ /static/
COPY built-backend/playlist-optimizer.jar po.jar
EXPOSE $PORT

CMD ["java","-jar","/po.jar"]
