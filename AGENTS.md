# AGENTS.md

Guía breve para trabajar en este repositorio. `README.md` documenta endpoints,
credenciales y uso de Docker; este archivo recoge lo que suele romperse.

## Comandos

- Usa siempre `./scripts/mvn.sh ...`, no `mvn`: detecta Java 17 (o descarga un
  JDK portátil) y delega en `./mvnw`. Un `mvn` global puede usar otra versión.
- Suite completa: `./scripts/mvn.sh test` (~2 min, sin PostgreSQL ni Eureka).
- Un módulo: `./scripts/mvn.sh -pl ms-orden test`.
- Una clase: `./scripts/mvn.sh -pl ms-orden -Dtest=SecurityIntegrationTest test`.
- Solo integración: `./scripts/mvn.sh -Dtest='*IntegrationTest' test`.
- Empaquetar sin pruebas: `./scripts/mvn.sh -DskipTests package`.
- Arrancar un servicio: `./scripts/mvn.sh -pl ms-auth spring-boot:run`.
- Stack completo: `docker compose up --build`.
- No hay linter, formatter ni CI configurados; la verificación es la suite Maven.

## Módulos

| Módulo | Puerto | Rol |
|---|---|---|
| `server` | 8761 | Eureka con HTTP Basic |
| `ms-auth` | 8084 | Emite JWT y administra usuarios |
| `ms-orden` | 8081 | Órdenes |
| `ms-driver` | 8082 | Conductores |
| `ms-asignacion` | 8083 | Asignaciones, subida de archivos y cliente Feign |

El `pom.xml` raíz solo agrega módulos. `spring-cloud.version` (2023.0.3) y
`springdoc.version` (2.6.0) se declaran dentro de cada módulo, no en el root; al
crear un módulo nuevo hay que replicarlas.

## Seguridad

- `ms-auth` guarda usuarios, roles y permisos en base de datos (`app_users`,
  `roles`, `permissions`, `user_roles`, `role_permissions`). `DataInitializer`
  los siembra de forma idempotente al arrancar; no hay usuarios en memoria.
  Usuarios por defecto: `user/user123` y `admin/admin123`.
- El JWT HS256 incluye los claims `roles` y `permissions`. Los resource servers
  convierten `roles` en `ROLE_*` y cada permiso en una authority directa, y
  autorizan con `hasAuthority("orden:read")`, etc. **No** autorizan por rol.
- Al agregar un endpoint o un permiso hay que actualizar el `SecurityConfig` del
  módulo, `DataInitializer` y las pruebas de seguridad.
- `JWT_SECRET` debe ser el mismo en los cinco servicios. Cambiarlo en uno solo
  invalida los tokens de los demás.
- Eureka usa `EUREKA_USER`/`EUREKA_PASSWORD` (HTTP Basic); los clientes incluyen
  las credenciales en `eureka.client.service-url.defaultZone`. No usa JWT.
- `ms-asignacion` llama por Feign a `ms-orden`/`ms-driver` por nombre de servicio
  y reenvía el header `Authorization` desde `FeignConfig`; no elimines ese
  interceptor.
- `/actuator/health` es público en todos los servicios y es el healthcheck de
  Compose.

## Pruebas

- Las pruebas usan H2 en memoria y no requieren servicios externos. Los
  `*IntegrationTest` desactivan Eureka con `eureka.client.enabled=false`,
  `register-with-eureka=false`, `fetch-registry=false` y
  `spring.cloud.discovery.enabled=false`; copia esas propiedades en cualquier
  `@SpringBootTest` nuevo.
- Mientras iteras, usa `-pl <módulo> -Dtest=<Clase>`; la suite completa levanta
  varios contextos de Spring y tarda varios minutos.
- Las pruebas de seguridad firman JWT con Nimbus usando el mismo secreto. Si
  cambias claims o authorities, actualiza `JwtService`, los
  `JwtAuthenticationConverter` y las pruebas.
- En `ms-auth` es normal ver `Table "..." already exists` durante los tests: H2
  usa `DB_CLOSE_DELAY=-1` y comparte la base entre contextos. No es un fallo.

## Docker y datos

- Los cinco servicios comparten una sola imagen
  (`three-microservices-eureka/app:local`) con los cinco JAR; el `command` de
  Compose elige cuál ejecutar. Reconstruir un servicio reconstruye esa imagen.
- En local cada servicio usa H2. Compose sobrescribe a PostgreSQL y **debe**
  definir `SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.postgresql.Driver`; sin eso
  el servicio sigue usando el driver H2 de `application.properties`.
- `docker/postgres/init/01-create-databases.sql` solo corre con el volumen
  vacío. Usa `docker compose down -v` para reinicializar `authdb`, `ordendb`,
  `driverdb` y `asignaciondb`.
- `EUREKA_INSTANCE_PREFER_IP_ADDRESS=true` permite que Feign resuelva los
  contenedores por IP dentro de la red Docker.
- `ms-asignacion` guarda archivos en `uploads.path` (`./uploads` en local,
  `/app/uploads` y volumen `asignacion-uploads` en Compose). `.env` y
  `uploads/` están ignorados por Git; parte de `.env.example`.
