# ==========================
# 🏗️ Etapa de construcción (Build)
# ==========================
FROM eclipse-temurin:21-jdk AS build

# Instalar Maven
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

# Directorio de trabajo
WORKDIR /app

# Copiar configuración de Maven
COPY pom.xml .

# Descargar dependencias (esta capa se cachea si no cambia pom.xml)
RUN mvn dependency:go-offline -B

# Copiar el código fuente
COPY src ./src

# Compilar el proyecto y generar el .jar (sin ejecutar tests)
RUN mvn clean package -DskipTests


# ==========================
# 🚀 Etapa de producción (Runtime)
# ==========================
FROM eclipse-temurin:21-jdk

# Instalar curl para health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Crear usuario no root por seguridad
RUN addgroup --system spring && adduser --system spring --ingroup spring

# Directorio de la aplicación
WORKDIR /app

# Crear carpetas necesarias para uploads
RUN mkdir -p /app/uploads/images \
    /app/uploads/videos \
    /app/uploads/thumbnails && \
    chown -R spring:spring /app

# Copiar el jar desde la etapa de build
COPY --from=build /app/target/*.jar app.jar

# Cambiar al usuario no root
USER spring:spring

# Exponer el puerto
EXPOSE 8080

# Variables de entorno optimizadas para VPS pequeño
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication -Djava.security.egd=file:/dev/./urandom"

# Comando de inicio
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
