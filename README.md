# SwapIt Backend

## CS 2031 Desarrollo Basado en Plataforma

### Integrantes

- Walter Sebastian Aquino Pachas
- Adrian Valentino Gamboa Rodriguez
- Corbin Kaufman Monzon
- Nicolas Lara Alvarez
- Rodolfo Antonio Lara Alvarez

## Índice

1. [Introducción](#1-introducción)
2. [Problema identificado](#2-problema-identificado)
3. [Solución propuesta](#3-solución-propuesta)
4. [Funcionalidades implementadas](#4-funcionalidades-implementadas)
5. [Tecnologías utilizadas](#5-tecnologías-utilizadas)
6. [Modelo de entidades](#6-modelo-de-entidades)
7. [Arquitectura](#7-arquitectura)
8. [Manejo de errores](#8-manejo-de-errores)
9. [Seguridad](#9-seguridad)
10. [Eventos y asincronía](#10-eventos-y-asincronía)
11. [API REST](#11-api-rest)
12. [Pruebas](#12-pruebas)
13. [GitHub y gestión](#13-github-y-gestión)
14. [Despliegue](#14-despliegue)
15. [Ejecución local](#15-ejecución-local)
16. [Conclusión](#16-conclusión)
17. [Referencias](#17-referencias)

## 1. Introducción

Muchas personas tienen objetos que dejaron de utilizar, aunque estos todavía se encuentran en buenas condiciones. Al mismo tiempo, otras personas podrían necesitar esos objetos y ofrecer algo diferente a cambio.

El problema es que estos intercambios normalmente se coordinan mediante redes sociales o grupos de mensajes. La información queda desordenada y aveces resulta difícil encontrar una persona que tenga lo que buscamos.

SwapIt busca organizar este proceso mediante una plataforma donde los usuarios publican sus objetos, buscan otros productos y envían propuestas de intercambio.

### Objetivos

- Permitir el registro e inicio de sesión de usuarios.
- Publicar objetos disponibles para intercambio.
- Buscar objetos usando diferentes filtros.
- Enviar, aceptar, rechazar y cancelar propuestas.
- Confirmar intercambios entre dos usuarios.
- Guardar objetos favoritos y categorías de interés.
- Calificar a otro usuario después de un intercambio.

## 2. Problema identificado

Las plataformas de venta están pensadas principalmente para operaciones con dinero. En un intercambio directo se necesita conocer qué ofrece cada usuario, qué busca y si ambos objetos continúan disponibles.

Usar publicaciones dispersas también puede ocasionar propuestas duplicadas, confusión sobre el estado de los objetos y poca seguridad sobre la identidad de las personas.

Resolver este problema permite promover la reutilización de objetos y reducir compras que quizá no sean necesarias. También ayuda a que los intercambios tengan estados y reglas mas claras.

## 3. Solución propuesta

SwapIt es una API REST para administrar intercambios de objetos entre usuarios.

Cada objeto funciona directamente como una publicación. Esta decisión simplificó el modelo porque ya no era necesario mantener una entidad `Publication` separada. El objeto contiene su descripción, categoría, condición, ubicación, estado y lo que el propietario desea recibir.

El flujo principal funciona de esta forma:

```mermaid
flowchart LR
    A[Usuario publica objeto] --> B[Otro usuario lo encuentra]
    B --> C[Envía una propuesta]
    C --> D{El dueño responde}
    D -->|Rechaza| E[Propuesta rechazada]
    D -->|Acepta| F[Se crea el intercambio]
    F --> G[Ambos usuarios confirman]
    G --> H[Intercambio completado]
    H --> I[Los usuarios dejan reseñas]
```

## 4. Funcionalidades implementadas

- Autenticación: registro, login, refresh token y cierre de sesión.
- Objetos: publicación, consulta, eliminación, filtros y paginación.
- Preferencias: favoritos, categorías de interés y recomendaciones.
- Propuestas: crear, aceptar, rechazar y cancelar propuestas.
- Intercambios: confirmar, completar o cancelar un intercambio.
- Reseñas: calificar al otro participante.
- Notificaciones: correos para operaciones importantes.

El sistema valida que los objetos sigan disponibles antes de aceptar una propuesta. Cuando comienza el intercambio, los dos objetos quedan reservados. Si el intercambio se cancela, vuelven a estar disponibles.

Las reseñas solo pueden ser creadas por los participantes. Además, el intercambio debe encontrarse completado y cada persona puede escribir solo una reseña.

## 5. Tecnologías utilizadas

- Java 21 y Spring Boot 4.
- Spring Web MVC y Spring Data JPA.
- Spring Security y JWT.
- PostgreSQL y H2 para pruebas.
- JavaMailSender para correos.
- Maven y Lombok.
- Docker y GitHub Actions.
- Postman para documentar y probar la API.

No se utilizaron APIs externas para mapas, pagos o inteligencia artificial porque no eran necesarias para completar el MVP.

## 6. Modelo de entidades

El proyecto utiliza siete entidades principales:

- `User`: usuario registrado, contraseña cifrada y rol.
- `Category`: clasificación de los objetos.
- `Item`: objeto publicado para intercambio.
- `Proposal`: oferta de un objeto por otro.
- `Exchange`: confirmación y estado del intercambio.
- `Review`: calificación entre los participantes.
- `RefreshToken`: renovación y cierre de sesiones.

```mermaid
erDiagram
    USER ||--o{ ITEM : posee
    CATEGORY ||--o{ ITEM : clasifica
    USER }o--o{ ITEM : favoritos
    USER }o--o{ CATEGORY : intereses
    USER ||--o{ PROPOSAL : envia
    ITEM ||--o{ PROPOSAL : ofrecido
    ITEM ||--o{ PROPOSAL : solicitado
    PROPOSAL ||--o| EXCHANGE : genera
    USER ||--o{ EXCHANGE : participa
    EXCHANGE ||--o{ REVIEW : recibe
    USER ||--o{ REVIEW : escribe
    USER ||--o{ REFRESH_TOKEN : mantiene
```

Las relaciones se implementaron con anotaciones como `@ManyToOne`, `@OneToMany`, `@ManyToMany` y `@OneToOne`. Se utiliza carga `LAZY` donde no es necesario obtener los datos relacionados inmediatamente.

También se agregaron validaciones como `@NotNull`, `@NotBlank`, `@Size`, `@Email`, `@Min` y `@Max`.

## 7. Arquitectura

La aplicación separa la recepción de solicitudes, las reglas del negocio y el acceso a los datos.

```mermaid
flowchart TD
    A[Cliente o Postman] --> B[Controller]
    B --> C[Service]
    C --> D[Repository]
    D --> E[(PostgreSQL)]
    C --> F[Event Publisher]
    F --> G[Listener asíncrono]
    G --> H[Servicio de correo]
```

Los controllers reciben los datos y llaman a los services. Las validaciones del intercambio están en los services y los repositories se encargan de consultar PostgreSQL.

Los DTOs separan los datos recibidos de las respuestas. Esto evita devolver las entidades completas o mostrar información sensible como las contraseñas.

## 8. Manejo de errores

El proyecto tiene excepciones para recursos inexistentes, conflictos, permisos insuficientes, credenciales incorrectas y tokens inválidos.

`GlobalExceptionHandler` utiliza `@RestControllerAdvice` para producir respuestas con un mismo formato:

```json
{
  "timestamp": "2026-09-25T20:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "El recurso no fue encontrado",
  "path": "/api/v1/items/100"
}
```

- `400`: solicitud o validación incorrecta.
- `401`: usuario sin autenticación válida.
- `403`: usuario sin permiso para la operación.
- `404`: recurso no encontrado.
- `409`: conflicto con el estado de los datos.
- `500`: error inesperado del servidor.

## 9. Seguridad

Las contraseñas se cifran con BCrypt y nunca forman parte de las respuestas. La autenticación utiliza JWT y un filtro que revisa el encabezado `Authorization`.

Existen los roles `USER` y `ADMIN`. Los administradores pueden gestionar categorías y consultar usuarios. Un usuario común solo puede modificar sus objetos y participar en propuestas o intercambios relacionados con su cuenta.

- BCrypt protege las contraseñas almacenadas.
- JWT autentica las solicitudes.
- Los refresh tokens permiten renovar y revocar sesiones.
- Las variables de entorno protegen claves y credenciales.
- Spring Data JPA reduce el riesgo de inyección SQL.
- CORS controla los orígenes permitidos.
- Las validaciones rechazan datos incorrectos.

CSRF se encuentra desactivado debido a que la API es stateless y utiliza tokens en el encabezado. La autorización también se valida dentro de los servicios para comprobar la propiedad de objetos y la participación en intercambios.

## 10. Eventos y asincronía

Se implementaron eventos en tres operaciones importantes:

1. Registro de un usuario.
2. Aceptación de una propuesta.
3. Finalización de un intercambio.

```mermaid
sequenceDiagram
    participant S as Service
    participant E as Event Publisher
    participant L as Listener
    participant M as MailService
    S->>E: Publica evento
    E-->>L: Entrega evento
    L-->>M: Ejecuta correo con Async
    S-->>S: Continua sin esperar el correo
```

`NotificationEventListener` procesa estos eventos usando `@Async`. Se configuró un `ThreadPoolTaskExecutor` para que el usuario no tenga que esperar mientras se conecta con el servidor de correo.

Si el correo falla, se registra el error y la operación principal no se pierde.

## 11. API REST

Todos los endpoints utilizan el prefijo `/api/v1`.

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/v1/auth/register` | Registrar usuario |
| POST | `/api/v1/auth/login` | Iniciar sesión |
| POST | `/api/v1/auth/refresh` | Renovar tokens |
| GET | `/api/v1/items` | Buscar objetos |
| POST | `/api/v1/items` | Publicar objeto |
| DELETE | `/api/v1/items/{id}` | Eliminar objeto propio |
| POST | `/api/v1/proposals` | Crear propuesta |
| PATCH | `/api/v1/proposals/{id}/accept` | Aceptar propuesta |
| PATCH | `/api/v1/proposals/{id}/reject` | Rechazar propuesta |
| GET | `/api/v1/exchanges` | Consultar intercambios |
| PATCH | `/api/v1/exchanges/{id}/confirm` | Confirmar intercambio |
| POST | `/api/v1/reviews` | Crear reseña |

La colección `postman_collection.json` se encuentra en la raíz del repositorio. Contiene ejemplos, variables, autorización y solicitudes para probar los recursos principales.

## 12. Pruebas

Se implementaron pruebas de integración para autenticación, permisos, objetos, propuestas, intercambios y reseñas.

Se ejecutaron 39 pruebas. El resultado fue cero fallos, cero errores y ninguna prueba omitida.

Las pruebas usan H2 y no necesitan una instalación local de PostgreSQL. Se verifican casos correctos y también errores como accesos sin permiso, datos inválidos y operaciones repetidas.

## 13. GitHub y gestión

El equipo utilizó ramas para desarrollar funcionalidades y pull requests para integrar los cambios a `main`. Los issues se organizaron en un orden según sus dependencias.

GitHub Actions ejecuta Maven automáticamente en los pushes y pull requests dirigidos a `main`. De esta forma se comprueba que el proyecto compile y que las pruebas continuen funcionando.

```mermaid
flowchart LR
    A[Crear issue] --> B[Crear rama]
    B --> C[Realizar cambios]
    C --> D[Crear pull request]
    D --> E[GitHub Actions]
    E --> F{Pruebas correctas}
    F -->|Sí| G[Merge a main]
    F -->|No| C
```

## 14. Despliegue

El backend fue preparado para ejecutarse con Docker mediante un `Dockerfile` de varias etapas.

```mermaid
flowchart LR
    A[Cliente o Postman] --> B[Backend en Docker]
    B --> C[(PostgreSQL)]
    B --> D[Servidor SMTP]
```

Las credenciales de PostgreSQL, el secreto JWT y la configuración del correo se colocan como variables de entorno. Esto evita guardar datos sensibles dentro del repositorio.

## 15. Ejecución local

Se necesita IntelliJ IDEA, Java 21 y Docker.

1. Clonar o descargar el repositorio.
2. Abrir el proyecto en IntelliJ y esperar que cargue Maven.
3. Revisar en Project Structure que el SDK sea Java 21.
4. Iniciar Docker Desktop.
5. Levantar PostgreSQL desde la terminal de IntelliJ:

```bash
docker compose up -d
```

6. Crear `.env` usando `.env.example` como referencia.
7. Configurar PostgreSQL y una clave JWT de 32 caracteres.
8. Abrir `ProyectoBackendSwapltApplication` y presionar Run.

Cuando aparezca el mensaje `Started ProyectoBackendSwapltApplication`, la API estará disponible en:

```text
http://localhost:8080/api/v1
```

Para las pruebas, se hace clic derecho sobre `src/test/java` y se selecciona Run Tests.

El archivo `.env` contiene datos privados y no se debe subir a GitHub.

## 16. Conclusión

SwapIt logró implementar el flujo principal para intercambiar objetos. El backend permite registrar usuarios, publicar objetos, enviar propuestas, confirmar intercambios y dejar reseñas.

Durante el proyecto aprendimos a separar responsabilidades entre controllers, services y repositories. También trabajamos con JWT, validaciones, relaciones JPA, pruebas, Docker y el manejo de errores.

Como trabajo futuro se podría agregar carga de imágenes en S3, recuperación de contraseña, ubicación mediante mapas y un sistema de recomendaciones mas completo.

## 17. Referencias

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/)
- [Spring Security Documentation](https://docs.spring.io/spring-security/reference/)
- [Spring Data JPA Documentation](https://docs.spring.io/spring-data/jpa/reference/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Docker Documentation](https://docs.docker.com/)
- [Postman Learning Center](https://learning.postman.com/)
- Material del curso CS 2031 Desarrollo Basado en Plataforma.
