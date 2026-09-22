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

- `POST /api/auth/register`: crea un usuario con rol `USER`; recibe `name`, `email` y `password` (mínimo 8 caracteres).
- `POST /api/auth/login`: recibe `email` y `password` y devuelve un token JWT.
- En rutas protegidas envía `Authorization: Bearer <token>`.
- La lectura de objetos, categorías y publicaciones es pública. Solo `ADMIN` puede crear o eliminar categorías y consultar usuarios.

## Publicaciones

Un objeto pertenece a un usuario. Una publicación ofrece ese objeto para intercambio y guarda la fecha, el estado y el tipo de objeto deseado. Para crearla se usa `POST /api/publications` con `itemId` y `wantedItem`. Solo el dueño del objeto puede hacerlo.
