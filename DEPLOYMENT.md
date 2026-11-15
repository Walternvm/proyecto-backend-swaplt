# Deployment de SwapIt Backend

## URLs publicas

- Frontend: https://issue-14-deployment.d1kgloxg3ds3s2.amplifyapp.com
- API: https://uld0g33r62.execute-api.us-east-1.amazonaws.com
- API base: https://uld0g33r62.execute-api.us-east-1.amazonaws.com/api/v1

## Infraestructura

- Contenedores: Amazon ECS Fargate
- Imagenes: Amazon ECR
- Base de datos: Amazon RDS
- Entrada HTTPS: Amazon API Gateway
- Registros: Amazon CloudWatch
- Region: us-east-1

## Variables de produccion

```env
CORS_ALLOWED_ORIGINS=https://issue-14-deployment.d1kgloxg3ds3s2.amplifyapp.com,http://localhost:3000,http://localhost:5173
AUTH_REFRESH_COOKIE_SECURE=true
AUTH_REFRESH_COOKIE_SAME_SITE=None
AUTH_REFRESH_COOKIE_PATH=/api/v1/auth/web
```

Las credenciales de base de datos, claves JWT y otros secretos deben configurarse solamente en AWS. No deben almacenarse en este documento ni en el repositorio.

## Verificacion

```text
GET https://uld0g33r62.execute-api.us-east-1.amazonaws.com/api/v1/categories
```

Tambien deben probarse desde el frontend:

- Registro
- Inicio de sesion
- Renovacion de sesion al recargar
- Cierre de sesion
- Rutas privadas
- Cookie `swaplt_refresh_token` con `HttpOnly`, `Secure` y `SameSite=None`
- Cookie `XSRF-TOKEN` con `Secure` y disponible para el cliente web

## Despliegue

1. Ejecutar las pruebas del backend.
2. Construir una nueva imagen con una etiqueta inmutable.
3. Subir la imagen a ECR.
4. Crear una nueva revision de `swaplt-backend-task`.
5. Actualizar `swaplt-backend-service` con la nueva revision.
6. Esperar hasta que la tarea nueva este en ejecucion.
7. Obtener la IP publica de la tarea.
8. Actualizar la integracion de API Gateway.
9. Verificar la API mediante HTTPS.
10. Revisar los registros de CloudWatch.

## Recuperacion

Si ECS reemplaza la tarea, su IP publica puede cambiar. Actualizar API Gateway con la nueva IP:

```powershell
$integration = aws apigatewayv2 get-integrations `
  --api-id uld0g33r62 `
  --region us-east-1 |
  ConvertFrom-Json

$integrationId = $integration.Items[0].IntegrationId

aws apigatewayv2 update-integration `
  --api-id uld0g33r62 `
  --integration-id $integrationId `
  --integration-uri "http://NUEVA-IP:8080" `
  --region us-east-1
```

Si una revision falla:

1. Revisar los registros de CloudWatch.
2. Revertir el servicio ECS a la revision anterior.
3. Confirmar la conexion con RDS.
4. Actualizar API Gateway con la IP de la tarea activa.
5. Probar nuevamente el endpoint de categorias.

## Almacenamiento de imagenes de articulos

Las imagenes se almacenan en un bucket privado de Amazon S3.

### Bucket

```text
swaplt-item-images-188264477355
```

El bloqueo de acceso publico debe permanecer completamente activado. El frontend y la aplicacion mobile no reciben credenciales de AWS ni acceden directamente al bucket.

### Variables de entorno

```env
ITEM_IMAGES_BUCKET=swaplt-item-images-188264477355
AWS_REGION=us-east-1
PUBLIC_API_URL=https://uld0g33r62.execute-api.us-east-1.amazonaws.com/api/v1
```

### Permisos de ECS

La tarea de ECS debe utilizar un task role con acceso al siguiente recurso:

```text
arn:aws:s3:::swaplt-item-images-188264477355/items/*
```

Permisos requeridos:

```text
s3:PutObject
s3:GetObject
s3:DeleteObject
```

En AWS Academy se utiliza el rol disponible `LabRole`. En un entorno definitivo se debe crear un rol dedicado con permisos limitados al bucket.

### Limites y formatos

- Una imagen por articulo.
- Tamano maximo: 5 MB.
- Formatos permitidos: JPEG, PNG y WebP.
- La firma real del archivo es validada por el backend.
- Las URLs de lectura de S3 son temporales.
- La imagen anterior se elimina al reemplazarla.
- La imagen se elimina cuando se borra el articulo.

### Verificacion

1. Crear un articulo.
2. Subir una imagen mediante `PUT /api/v1/items/{id}/image`.
3. Confirmar que `imageUrl` no sea nulo.
4. Descargar la imagen mediante `GET /api/v1/items/{id}/image`.
5. Reemplazar la imagen y comprobar que solo exista un objeto.
6. Eliminar el articulo y comprobar que su imagen desaparezca de S3.
