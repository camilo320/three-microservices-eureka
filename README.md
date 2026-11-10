# three-microservices-eureka

Microservices for a transportation company.

## Requisitos

- Java 17+
- Maven 3.9+ (opcional: el proyecto incluye Maven Wrapper)

## Ejecutar las pruebas

La forma recomendada no requiere tener Java ni Maven instalados globalmente:

```bash
./scripts/mvn.sh test
```

El script:

1. Reutiliza `JAVA_HOME` o el `java` del sistema si es 17+.
2. Si no encuentra Java 17, descarga un JDK portátil en
   `~/.local/share/three-microservices-eureka/jdk-17` (no requiere `sudo`).
3. Ejecuta `./mvnw`, el Maven Wrapper incluido en el proyecto.

Si ya tienes Java 17 y Maven instalados, también puedes ejecutar:

```bash
mvn test
# o sin Maven global:
./mvnw test
```

Comandos útiles:

```bash
# Todas las pruebas
./scripts/mvn.sh test

# Solo pruebas de integración
./scripts/mvn.sh -Dtest='*IntegrationTest' test

# Solo pruebas de contexto
./scripts/mvn.sh -Dtest='*ApplicationContextTest' test

# Empaquetar sin ejecutar pruebas
./scripts/mvn.sh -DskipTests package
```

## Módulos

| Módulo | Puerto | Descripción |
|---|---|---|
| `server` | 8761 | Servidor Eureka (HTTP Basic) |
| `ms-auth` | 8084 | Emisión de tokens JWT (`/auth/login`) |
| `ms-orden` | 8081 | Gestión de órdenes |
| `ms-driver` | 8082 | Gestión de conductores |
| `ms-asignacion` | 8083 | Asignación de órdenes a conductores |

## Ejecución con Docker

El proyecto incluye un `Dockerfile` multi-módulo y un `docker-compose.yml` que
levantan todo el stack:

- Eureka (`server`) en el puerto `8761`, protegido con HTTP Basic.
- PostgreSQL 16 con una base de datos por microservicio.
- `ms-auth`, `ms-orden`, `ms-driver` y `ms-asignacion`.

### Requisitos

- Docker Engine 24+ con Docker Compose v2.
- Espacio libre para las imágenes y el volumen de PostgreSQL.

### Levantar el stack

```bash
docker compose up --build
```

La primera ejecución compila los cinco módulos Maven y descarga las imágenes
base, por lo que puede tardar varios minutos. El `Dockerfile` genera una sola
imagen con los cinco JAR y cada contenedor ejecuta el suyo; las siguientes
ejecuciones reutilizan la caché.

| Servicio | URL local |
|---|---|
| Eureka | http://localhost:8761 |
| `ms-auth` | http://localhost:8084 |
| `ms-orden` | http://localhost:8081/swagger-ui.html |
| `ms-driver` | http://localhost:8082/swagger-ui.html |
| `ms-asignacion` | http://localhost:8083/swagger-ui.html |
| PostgreSQL | `localhost:5432` |

Credenciales por defecto:

| Recurso | Usuario | Contraseña |
|---|---|---|
| Eureka | `eureka` | `eureka123` |
| Usuarios de la app | `user` / `admin` | `user123` / `admin123` |
| PostgreSQL | `transporte` | `transporte123` |

Los usuarios, roles y permisos se siembran automáticamente al arrancar
`ms-auth`, igual que en la ejecución local.

### Healthchecks

Todos los servicios exponen `GET /actuator/health` sin autenticación. Docker
Compose usa ese endpoint para marcar los contenedores como `healthy` y para
ordenar el arranque: `ms-asignacion` espera a que `ms-orden` y `ms-driver`
estén saludables antes de iniciar.

```bash
curl http://localhost:8084/actuator/health   # ms-auth
curl http://localhost:8081/actuator/health   # ms-orden
docker compose ps
```

### Variables de entorno

Copia el archivo de ejemplo y ajusta los valores:

```bash
cp .env.example .env
```

| Variable | Valor por defecto | Descripción |
|---|---|---|
| `JWT_SECRET` | Clave de desarrollo | Secreto compartido para firmar los JWT |
| `EUREKA_USER` | `eureka` | Usuario de Eureka |
| `EUREKA_PASSWORD` | `eureka123` | Contraseña de Eureka |
| `POSTGRES_USER` | `transporte` | Usuario de PostgreSQL |
| `POSTGRES_PASSWORD` | `transporte123` | Contraseña de PostgreSQL |
| `POSTGRES_PORT` | `5432` | Puerto de PostgreSQL en el host |

Si el puerto `5432` ya está ocupado por otro PostgreSQL local, cambia
`POSTGRES_PORT` en el `.env` (por ejemplo, `5433`) y usa ese puerto para
conectarte desde el host.

### Comandos útiles

```bash
# Ver el estado de los contenedores
docker compose ps

# Seguir los logs de un servicio
docker compose logs -f ms-auth

# Detener el stack conservando los datos
docker compose down

# Detener y borrar también los volúmenes (base de datos y archivos subidos)
docker compose down -v

# Reiniciar un servicio sin reconstruir la imagen compartida
docker compose restart ms-orden
```

Los datos de PostgreSQL y los archivos subidos por `ms-asignacion` se guardan en
los volúmenes `postgres-data` y `asignacion-uploads`. El script
`docker/postgres/init/01-create-databases.sql` crea las bases `authdb`,
`ordendb`, `driverdb` y `asignaciondb` la primera vez que se inicializa el
volumen.

## Seguridad con JWT

La seguridad está basada en **Spring Security + OAuth2 Resource Server** con tokens
JWT firmados con HS256.

- `ms-auth` emite los tokens.
- `ms-orden`, `ms-driver` y `ms-asignacion` validan el token en cada petición.
- `ms-asignacion` reenvía el token al llamar por Feign a `ms-orden` y `ms-driver`.
- Eureka (`server`) usa **HTTP Basic**, porque los clientes Eureka no envían JWT
  de forma nativa. Los microservicios incluyen las credenciales en la URL de
  registro.

### Usuarios, roles y permisos

Los usuarios de `ms-auth` se almacenan en base de datos con el modelo
`app_users → user_roles → roles → role_permissions → permissions`:

- Cada usuario tiene uno o más roles.
- Cada rol agrupa permisos.
- El JWT incluye los claims `roles` y `permissions`.
- `ms-orden`, `ms-driver` y `ms-asignacion` autorizan por **permiso**, no por rol.

Los datos iniciales se crean de forma idempotente al arrancar `ms-auth`:

| Rol | Permisos |
|---|---|
| `USER` | `orden:read`, `driver:read`, `asignacion:read` |
| `ADMIN` | `orden:read`, `orden:write`, `driver:read`, `driver:write`, `asignacion:read`, `asignacion:write`, `user:read`, `user:write` |

| Usuario | Contraseña | Roles | Permisos efectivos |
|---|---|---|---|
| `user` | `user123` | `USER` | Solo lectura de órdenes, conductores y asignaciones |
| `admin` | `admin123` | `ADMIN`, `USER` | Todos los permisos |

### Reglas de acceso

| Método | Endpoint | Permiso requerido |
|---|---|---|
| `POST` | `/auth/login` | Público |
| `GET` | Órdenes | `orden:read` |
| `POST` / `PUT` / `DELETE` | Órdenes | `orden:write` |
| `GET` | Conductores | `driver:read` |
| `POST` / `PUT` / `DELETE` | Conductores | `driver:write` |
| `GET` | Asignaciones | `asignacion:read` |
| `POST` / `PUT` / `DELETE` | Asignaciones | `asignacion:write` |
| `GET` | `/auth/users`, `/auth/roles` | `user:read` |
| `POST` | `/auth/users` | `user:write` |
| `GET` | Swagger UI, OpenAPI, H2 console, `/actuator/health` | Público |

### Obtener un token

```bash
curl -s -X POST http://localhost:8084/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}'
```

Respuesta:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "expiresIn": 3600,
  "username": "admin",
  "roles": ["ADMIN", "USER"],
  "permissions": [
    "asignacion:read", "asignacion:write",
    "driver:read", "driver:write",
    "orden:read", "orden:write",
    "user:read", "user:write"
  ]
}
```

### Administración de usuarios

Los siguientes endpoints requieren un token con los permisos indicados:

| Método | Endpoint | Permiso |
|---|---|---|
| `GET` | `/auth/users` | `user:read` |
| `POST` | `/auth/users` | `user:write` |
| `GET` | `/auth/roles` | `user:read` |

```bash
# Listar usuarios y roles
curl -H "Authorization: Bearer $TOKEN" http://localhost:8084/auth/users
curl -H "Authorization: Bearer $TOKEN" http://localhost:8084/auth/roles

# Crear un usuario con rol USER
curl -X POST http://localhost:8084/auth/users \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"username":"operador","password":"operador123","roles":["USER"]}'
```

### Usar el token

```bash
TOKEN=$(curl -s -X POST http://localhost:8084/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}' | jq -r .token)

curl -H "Authorization: Bearer $TOKEN" http://localhost:8081/search
curl -H "Authorization: Bearer $TOKEN" http://localhost:8082/search
```

### Configuración

El secreto se comparte entre todos los módulos mediante la variable de entorno
`JWT_SECRET`. Si no se define, se usa un valor por defecto apto solo para
desarrollo:

```bash
export JWT_SECRET='una-clave-secreta-de-al-menos-32-bytes'
./scripts/mvn.sh -pl ms-auth spring-boot:run
```

En producción se debe definir `JWT_SECRET` en todos los servicios con el mismo
valor y no usar el valor por defecto.

### Base de datos de `ms-auth`

Por defecto `ms-auth` usa H2 en memoria:

```properties
spring.datasource.url=jdbc:h2:mem:authdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false
spring.jpa.hibernate.ddl-auto=update
```

Para usar PostgreSQL basta con configurar el datasource y el driver ya incluido
en el `pom.xml`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/authdb
spring.datasource.username=auth
spring.datasource.password=una-contraseña-segura
spring.datasource.driver-class-name=org.postgresql.Driver
```

`DataInitializer` crea permisos, roles y usuarios solo si no existen, por lo que
se puede ejecutar varias veces sin duplicar datos. No sobreescribe usuarios ya
creados.

### Protección de Eureka

El dashboard y la API de Eureka requieren HTTP Basic:

| Usuario | Contraseña |
|---|---|
| `eureka` | `eureka123` |

```bash
# Dashboard/API con credenciales
curl -u eureka:eureka123 http://localhost:8761/eureka/apps
```

Los microservicios envían las credenciales a Eureka mediante la URL configurada
en `eureka.client.service-url.defaultZone`. Se pueden cambiar con:

```bash
export EUREKA_USER='eureka'
export EUREKA_PASSWORD='una-contraseña-segura'
```

Estas variables deben tener el mismo valor en `server`, `ms-auth`, `ms-orden`,
`ms-driver` y `ms-asignacion`. En producción no se debe usar la contraseña por
defecto.

## Pruebas

El proyecto incluye:

- Pruebas unitarias con JUnit 5, Mockito y AssertJ.
- Pruebas de validación de DTOs.
- Pruebas de integración de búsquedas con `@DataJpaTest` y H2.
- Pruebas del modelo de usuarios, roles y permisos en base de datos.
- Pruebas de arranque de contexto con `@SpringBootTest`.
- Pruebas de seguridad JWT: login, token válido/expirado, `401`, `403` por
  permiso, administración de usuarios y propagación del token en Feign.
- Pruebas de seguridad de Eureka: dashboard y API protegidos con HTTP Basic.

Estado actual: **159 pruebas**, todas en verde.

| Módulo | Pruebas |
|---|---|
| `ms-orden` | 44 |
| `ms-driver` | 43 |
| `ms-asignacion` | 46 |
| `ms-auth` | 19 |
| `server` | 7 |
