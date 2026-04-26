############# BUILD STAGE
#Create a build contianer with maven & java 17 
FROM maven:3.9.9-eclipse-temurin-17 AS build

#Set the working directory inside the build container
WORKDIR /app

#Copy Maven wrapper/config files first (for Docker caching)
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

#Make maven wrapper executable
RUN chmod +x mvnw

#Download dependencies
RUN ./mvnw dependency:go-offline -B

#Copy application source code
COPY src src

#Build the jar file
RUN ./mvnw clean package -DskipTests

############# RUNIME STAGE
#Create a smaller runtime image with only java 17 runtime w/o maven 
FROM eclipse-temurin:17-jre

# Set the working directory inside the runtime container
WORKDIR /app

#Copy the built jar from build stage into the final image
COPY --from=build /app/target/*.jar app.jar

#Expose default springboot port (used for local run only)
EXPOSE 8080

#Run the springboot app
ENTRYPOINT [ "java", "-jar", "app.jar" ]