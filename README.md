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

---

## 🧩 Patrones de Diseño de Software Utilizados (Análisis Detallado)

### 1. Service Layer
- **Dónde:** Todas las clases en `service/` (ej: `UserService`, `AuthService`, `CliperService`, `TechnicalTestService`, `NotificationService`).
- **Cómo:** Centralizan la lógica de negocio, desacoplando los controladores de la persistencia y facilitando el testeo.
- **Para qué:** Permiten reutilizar lógica, mantener el código organizado y aplicar reglas de negocio en un solo lugar.

### 2. Template Method (Implícito)
- **Dónde:** Métodos como `registerUser` en `UserService`, `login` en `AuthService`.
- **Cómo:** Definen el esqueleto de un proceso (registro, login) y delegan pasos concretos a métodos auxiliares.
- **Para qué:** Permiten estandarizar flujos complejos y facilitar la extensión o personalización de pasos.

### 3. Factory Method (Implícito)
- **Dónde:** Métodos constructores y estáticos en entidades como `User`.
- **Cómo:** Permiten crear instancias de entidades según el contexto (ej: usuario candidato o empresa).
- **Para qué:** Facilitan la creación flexible y controlada de objetos.

### 4. Observer Pattern (Implícito)
- **Dónde:** `NotificationService` y su lista de `NotificationHandler`.
- **Cómo:** Notifica a todos los observadores registrados ante eventos relevantes (registro, like, comentario).
- **Para qué:** Desacopla la lógica de notificación del flujo principal, permitiendo múltiples canales de notificación.

### 5. Strategy Pattern (Implícito)
- **Dónde:** En servicios como `CliperService` y `NotificationService`.
- **Cómo:** Permite cambiar la estrategia de procesamiento de video o notificación según configuración o tipo de evento.
- **Para qué:** Facilita la extensión y personalización de comportamientos sin modificar el flujo principal.

### 6. Repository Pattern
- **Dónde:** Todas las interfaces en `repository/` (ej: `UserRepository`, `JobRepository`, `TechnicalTestRepository`).
- **Cómo:** Encapsulan el acceso a datos, permitiendo consultas declarativas y desacoplando la lógica de persistencia.
- **Para qué:** Facilitan el mantenimiento, el testeo y la evolución de la capa de datos.

### 7. Query Method Pattern
- **Dónde:** Métodos como `findByEmail`, `findByJobIdAndCandidateId`, `findByIsActiveTrue` en los repositorios.
- **Cómo:** Permiten definir consultas complejas usando el nombre del método o anotaciones `@Query`.
- **Para qué:** Simplifican la consulta de datos y evitan escribir queries manuales.

### 8. Entity Pattern
- **Dónde:** Todas las clases en `entity/` (ej: `User`, `Cliper`, `Job`).
- **Cómo:** Modelan los documentos de MongoDB y encapsulan la lógica de dominio.
- **Para qué:** Representan los datos persistentes y sus relaciones.

### 9. Value Object (Implícito)
- **Dónde:** Campos como dirección, skills, comentarios en entidades.
- **Cómo:** Encapsulan datos inmutables y sin identidad propia.
- **Para qué:** Mejoran la claridad y robustez del modelo de dominio.

### 10. DTO (Data Transfer Object)
- **Dónde:** Clases en `dto/` (ej: `UserDTO`, `RegisterRequest`, `AuthResponse`).
- **Cómo:** Transportan datos entre capas y hacia el frontend, evitando exponer entidades completas.
- **Para qué:** Mejoran la seguridad, el control de la información y la eficiencia de la comunicación.

### 11. Builder Pattern (Implícito en DTOs)
- **Dónde:** Constructores sobrecargados en DTOs.
- **Cómo:** Permiten crear instancias con diferentes combinaciones de datos.
- **Para qué:** Facilitan la creación flexible de objetos de transferencia.

### 12. Controller Pattern
- **Dónde:** Todas las clases en `controller/` (ej: `UserController`, `AuthController`).
- **Cómo:** Gestionan las rutas y peticiones HTTP, delegando la lógica a los servicios.
- **Para qué:** Separan la lógica de presentación de la lógica de negocio.

### 13. Singleton y Configuration Pattern
- **Dónde:** Clases en `config/` (ej: `SecurityConfig`, `WebConfig`).
- **Cómo:** Beans singleton gestionados por Spring, centralizando la configuración global.
- **Para qué:** Permiten modificar el comportamiento global de la app de forma centralizada y escalable.

---

## Ejemplo de Uso de Patrones

- **Registro de usuario:**
  - `UserController` recibe la petición y delega a `UserService`.
  - `UserService` usa Template Method para el flujo de registro y Factory Method para crear el usuario.
  - Se guarda usando `UserRepository` (Repository Pattern).
  - Se notifica usando `NotificationService` (Observer/Strategy).
  - Se responde con un `UserDTO` (DTO).

- **Autenticación:**
  - `AuthController` delega a `AuthService`, que usa Template Method y responde con `AuthResponse` (DTO).

- **Procesamiento de video:**
  - `CliperService` decide la estrategia de procesamiento (Strategy) y notifica al usuario (Observer).

---

## Características Clave
- Arquitectura limpia y desacoplada.
- Uso extensivo de patrones de diseño estándar.
- Seguridad y configuración centralizadas.
- DTOs para comunicación eficiente y segura.
- Repositorios declarativos y consultas personalizadas.

---

## Referencias
- [Spring Boot Docs](https://spring.io/projects/spring-boot)
- [Patrones de Diseño GoF](https://refactoring.guru/es/design-patterns)
