FROM openjdk:11
VOLUME /tmp
COPY target/*.jar proxy-layer-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","/proxy-layer-0.0.1-SNAPSHOT.jar"]