# ====================================
# ÉTAPE 1 : BUILD avec Maven
# ====================================
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copie des fichiers de configuration Maven
COPY pom.xml .
COPY src ./src

# Build de l'application (création du JAR)
RUN mvn clean package -DskipTests

# ====================================
# ÉTAPE 2 : RUNTIME avec JRE
# ====================================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copie du JAR depuis l'étape de build
COPY --from=build /app/target/*.jar app.jar

# Variables d'environnement (surchargées par Render au démarrage)
ENV SERVER_PORT=8080

# Exposition du port
EXPOSE 8080

# Healthcheck simplifié (vérifie juste que le port 8080 répond)
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/auth/login || exit 1

# Démarrage de l'application
# Le profil Spring est défini par la variable SPRING_PROFILES_ACTIVE sur Render
CMD ["java", "-jar", "app.jar"]