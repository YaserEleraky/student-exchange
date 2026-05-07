FROM openjdk:27-ea-20-jdk
ADD target/studentexchange.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]