# Configuración de PostgreSQL 17 en Coolify

## Imagen Docker Correcta

Para PostgreSQL 17 en Coolify, utiliza la siguiente imagen Docker:

```yaml
image: postgres:17-alpine
```

Esta imagen está configurada correctamente en el archivo [`docker-compose.yml`](backendClipers/docker-compose.yml:1):

```yaml
services:
  postgres:
    image: postgres:17-alpine
    container_name: clipers-postgres
    environment:
      POSTGRES_DB: clipers_db
      POSTGRES_USER: clipers_user
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD:-clipers_password}
    ports:
      - "5434:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U clipers_user -d clipers_db"]
      interval: 10s
      timeout: 5s
      retries: 5
    restart: unless-stopped
    networks:
      - clipers-network
```

## Configuración en Coolify

### 1. Crear Servicio PostgreSQL

1. En Coolify, haz clic en "New Service" → "Database"
2. Selecciona "PostgreSQL"
3. Configura los siguientes parámetros:
   - **Name**: `clipers-postgres`
   - **Image**: `postgres:17-alpine`
   - **Database Name**: `clipers_db`
   - **Username**: `clipers_user`
   - **Password**: Genera una contraseña segura

### 2. Variables de Entorno para el Backend

Una vez creado el servicio PostgreSQL en Coolify, configura estas variables en el servicio del backend:

```bash
# Base de Datos
DATABASE_URL=postgresql://clipers_user:PASSWORD@HOST_COOLIFY:5432/clipers_db
DATABASE_USERNAME=clipers_user
DATABASE_PASSWORD=PASSWORD

# PostgreSQL 17
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

### 3. Verificación de la Versión

Para verificar que estás usando PostgreSQL 17, puedes:

1. **Revisar logs del backend**:
   Busca este mensaje: `PostgreSQLDialect does not need to be specified explicitly`
   Si ves este mensaje, significa que Hibernate detectó automáticamente PostgreSQL 17

2. **Conectarse directamente a la base de datos**:
   ```bash
   docker exec -it clipers-postgres psql -U clipers_user -d clipers_db -c "SELECT version();"
   ```

3. **Verificar health check**:
   ```bash
   curl https://backend.clipers.pro/api/test/health
   ```

## Troubleshooting

### Error: "Database version: 12.0"

Si ves este error en los logs, significa que Coolify está usando PostgreSQL 12 en lugar de 17.

**Solución**:
1. Verifica que la imagen en Coolify sea `postgres:17-alpine`
2. Elimina el servicio actual y créalo nuevamente
3. Asegúrate de que las variables de entorno del backend estén configuradas correctamente

### Error: "No suitable driver found for"

Si ves este error, significa que el driver PostgreSQL no es compatible.

**Solución**:
1. Verifica que la dependencia en [`pom.xml`](backendClipers/pom.xml:1) sea:
   ```xml
   <dependency>
       <groupId>org.postgresql</groupId>
       <artifactId>postgresql</artifactId>
       <scope>runtime</scope>
   </dependency>
   ```
2. El driver PostgreSQL incluido en Spring Boot 3.5.6 es compatible con PostgreSQL 17

## Configuración de Hibernate para PostgreSQL 17

En [`application.properties`](backendClipers/src/main/resources/application.properties:1), asegúrate de tener:

```properties
# JPA/Hibernate Configuration
spring.jpa.hibernate.ddl-auto=${JPA_DDL_AUTO:update}
spring.jpa.show-sql=${JPA_SHOW_SQL:true}
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

## Comandos Útiles

### Verificar la versión de PostgreSQL
```bash
# En Coolify (a través del backend)
curl https://backend.clipers.pro/api/test/health

# Conectarse directamente (si tienes acceso)
docker exec -it clipers-postgres psql -U clipers_user -d clipers_db -c "SELECT version();"
```

### Verificar el dialecto de Hibernate
```bash
# Revisa los logs del backend
grep "PostgreSQLDialect" logs/backend.log
```

## Resumen

- ✅ **Imagen Docker**: `postgres:17-alpine` configurada en docker-compose.yml
- ✅ **Driver PostgreSQL**: Compatible con PostgreSQL 17
- ✅ **Dialecto Hibernate**: Configurado para PostgreSQL 17
- ✅ **Variables de entorno**: Preparadas para Coolify

Con esta configuración, el backend Clipers funcionará correctamente con PostgreSQL 17 en Coolify.