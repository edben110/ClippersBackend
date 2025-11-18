# 🔧 Clipers Backend

API REST desarrollada con Spring Boot para la plataforma Clipers - Red social de video-CVs.

## 🚀 Inicio Rápido

### Requisitos Previos

- Java 17 o superior
- Maven 3.6+
- MongoDB 7.0+ (local o Atlas)

### Configuración Local

1. **Copia el archivo de variables de entorno:**
   ```bash
   cp .env.example .env
   ```

2. **Edita `.env` con tu configuración:**
   ```bash
   # MongoDB
   MONGODB_URI=mongodb://localhost:27017/clipers_db
   
   # AI Services - IMPORTANTE: Configura estas URLs
   AI_MATCHING_SERVICE_URL=https://selector.clipers.pro
   VIDEO_PROCESSING_SERVICE_URL=https://video.clipers.pro
   ```

3. **Inicia MongoDB:**
   ```bash
   # Windows
   start-mongodb.bat
   
   # Linux/Mac
   docker run -d -p 27017:27017 --name clipers-mongodb mongo:7.0
   ```

4. **Ejecuta la aplicación:**
   ```bash
   # Con Maven
   ./mvnw spring-boot:run
   
   # O con el JAR compilado
   ./mvnw clean package
   java -jar target/clipers-0.0.1-SNAPSHOT.jar
   ```

5. **Verifica que funcione:**
   - Health: http://localhost:8080/actuator/health
   - Test: http://localhost:8080/api/test/health

## 📋 Variables de Entorno

### Obligatorias

Estas variables **DEBEN** estar configuradas en `.env`:

```bash
# MongoDB
MONGODB_URI=mongodb://localhost:27017/clipers_db

# AI Services
AI_MATCHING_SERVICE_URL=https://selector.clipers.pro
VIDEO_PROCESSING_SERVICE_URL=https://video.clipers.pro
```

### Opcionales (tienen valores por defecto)

```bash
SPRING_PROFILE=dev
SERVER_PORT=8080
JWT_SECRET=mySecretKey...
FRONTEND_URL=http://localhost:3000
```

Ver `.env.example` para la lista completa.

## 🏗️ Estructura del Proyecto

```
src/main/java/com/clipers/clipers/
├── config/          # Configuraciones (Security, CORS, etc.)
├── controller/      # Endpoints REST
├── dto/            # Data Transfer Objects
├── entity/         # Entidades MongoDB
├── repository/     # Repositorios MongoDB
├── security/       # JWT, autenticación
└── service/        # Lógica de negocio
```

## 🔐 Seguridad

### Perfiles de Spring

- **`dev`**: Desarrollo local (endpoints de test públicos)
- **`prod`**: Producción (endpoints de test requieren ADMIN)

Configura con: `SPRING_PROFILE=dev` o `SPRING_PROFILE=prod`

### Roles de Usuario

- **CANDIDATE**: Puede subir clipers, aplicar a empleos
- **COMPANY**: Puede publicar empleos, ver aplicantes
- **ADMIN**: Acceso completo (solo en producción)

## 🧪 Testing

```bash
# Ejecutar tests
./mvnw test

# Ejecutar con cobertura
./mvnw test jacoco:report
```

## 📦 Build para Producción

```bash
# Compilar JAR
./mvnw clean package -DskipTests

# El JAR estará en: target/clipers-0.0.1-SNAPSHOT.jar
```

## 🐳 Docker

```bash
# Build
docker build -t clipers-backend .

# Run
docker run -p 8080:8080 \
  -e MONGODB_URI=mongodb://host.docker.internal:27017/clipers_db \
  -e AI_MATCHING_SERVICE_URL=https://selector.clipers.pro \
  -e VIDEO_PROCESSING_SERVICE_URL=https://video.clipers.pro \
  clipers-backend
```

## 🚀 Despliegue en Coolify

Ver [COOLIFY_SETUP.md](./COOLIFY_SETUP.md) para instrucciones detalladas.

**Resumen rápido:**
1. Configura MongoDB (Atlas recomendado)
2. Copia variables de `.env.production`
3. Configura volumen `/app/uploads`
4. Despliega

## 📚 API Endpoints

### Públicos
- `POST /api/auth/login` - Login
- `POST /api/auth/register` - Registro
- `GET /api/clipers/public` - Ver clipers públicos
- `GET /api/jobs/public` - Ver empleos públicos

### Autenticados
- `GET /api/auth/me` - Perfil actual
- `POST /api/clipers/upload` - Subir cliper (CANDIDATE)
- `POST /api/jobs/create` - Crear empleo (COMPANY)

### Admin (solo en producción)
- `DELETE /api/clipers/admin/clear-all` - Limpiar clipers
- `GET /api/test/**` - Endpoints de prueba

## 🔧 Troubleshooting

### Error: "AI_MATCHING_SERVICE_URL is required"

Asegúrate de tener `.env` con:
```bash
AI_MATCHING_SERVICE_URL=https://selector.clipers.pro
VIDEO_PROCESSING_SERVICE_URL=https://video.clipers.pro
```

### Error: "Failed to connect to MongoDB"

Verifica que MongoDB esté corriendo:
```bash
# Windows
start-mongodb.bat

# Linux/Mac
docker ps | grep mongo
```

### Puerto 8080 en uso

Cambia el puerto en `.env`:
```bash
SERVER_PORT=8081
```

## 📞 Soporte

- Documentación completa: [DEPLOYMENT.md](../DEPLOYMENT.md)
- Setup Coolify: [COOLIFY_SETUP.md](./COOLIFY_SETUP.md)
- Issues: GitHub Issues

## 📄 Licencia

Privado - Todos los derechos reservados
