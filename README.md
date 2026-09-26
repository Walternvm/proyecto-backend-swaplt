# Proyecto Backend DBP(CS2031): Swaplt
Integrantes:
- Aquino Pachas, Walter Sebastian
- Kaufman Monzon, Corbin
- Lara Alvarez, Nicolas
- Gamboa Rodriguez, Adrian Valentino
- Lara Alvarez Rodolfo Antonio

## Ejecutar localmente

Se necesita Java 21 o superior y PostgreSQL. Inicia la base de datos con `docker compose up -d` y define las variables de `.env.example` en tu terminal. `JWT_SECRET` debe tener al menos 32 caracteres. `ADMIN_EMAIL` y `ADMIN_PASSWORD` son opcionales: si se definen al iniciar por primera vez, se crea un administrador.

Ejecuta la aplicación con `sh ./mvnw spring-boot:run`. Para correr las pruebas, usa `sh ./mvnw test`. Las pruebas usan H2 y no necesitan PostgreSQL.

## Autenticación básica

- `POST /api/v1/auth/register`: crea un usuario con rol `USER`; recibe `name`, `email` y `password` (mínimo 8 caracteres).
- `POST /api/v1/auth/login`: recibe `email` y `password` y devuelve un token JWT y un refresh token.
- `POST /api/v1/auth/refresh`: renueva la sesión usando el refresh token.
- En rutas protegidas envía `Authorization: Bearer <token>`.
- La lectura de objetos y categorías es pública. Solo `ADMIN` puede crear o eliminar categorías y consultar usuarios.

## Objetos publicados

Siguiendo la simplificación indicada en la propuesta, no existe una entidad separada llamada `Publicación`. Cada objeto funciona directamente como la publicación del usuario. Guarda su descripción, categoría, condición, ubicación, estado, fecha y el tipo de objeto que se desea recibir.

Para publicar un objeto se usa `POST /api/v1/items`. El usuario se obtiene del token. Los objetos se pueden buscar con filtros de categoría, estado, condición, ubicación y texto.

## Datos de la API

Las solicitudes usan IDs para indicar relaciones. Por ejemplo:

- `POST /api/v1/categories`: `{ "name": "Libros" }` (solo ADMIN).
- `POST /api/v1/items`: `{ "name": "Novela", "description": "Buen estado", "categoryId": 1, "location": "Lima", "wantedItem": "Juego de mesa", "condition": "GOOD" }`.
- `POST /api/v1/proposals`: `{ "offeredItemId": 2, "requestedItemId": 1, "message": "¿Intercambiamos?" }`.
- `POST /api/v1/reviews`: `{ "exchangeId": 1, "rating": 5, "comment": "Todo bien" }`.

El servidor obtiene el usuario desde el token y decide los estados y fechas. Las respuestas incluyen IDs y datos simples como `ownerId`, `categoryId` y `status`; no incluyen la contraseña ni entidades anidadas.
