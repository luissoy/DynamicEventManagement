# Database layer: Project Information

## Project Structure
The app is a Spring Boot application that provides CRUD operations for managing the database layer. It uses MongoDB as the database.

### Project Dependencies
- *Spring Boot Version:* 3.2.3
- *Java Version:* 17

### Swagger Documentation
- The application includes Swagger documentation for the API. It is accessible at [/swagger](/swaggerl).
- The API will be accessible at http://localhost:8080/api/v1/.
- With docker the API will be accessible at http://localhost:10001/api/v1/.

### Modules
- *Controller Module:* Handles HTTP requests and manages API endpoints.
- *Service Module:* Implements business logic and interacts with the database.
- *Model Module:* Defines the data models.
- *Dto Module:* Defines the dtos of the models.
- *Bean Module:* Implements beans for the whole app.
- *Repository Module:* Defines the JPA implementation for mongo database.
- *Response Module:* Implements models for the API answers.
- *Exception Module:* Implements custom exceptions.
- *Util Module:* Implements utils for the whole app.
- *Dockerfile:* Defines the Docker image for the application.
- *Docker-compose:* Sets up the MongoDB, MongoDB Express and App containers for development.

## OpenAPI definition

### /api/v1/users/{id}

#### GET
##### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ---- |
| id | path |  | Yes | string |

#### PUT
##### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ---- |
| id | path |  | Yes | string |

#### DELETE
##### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ---- |
| id | path |  | Yes | string |

### /api/v1/groups/{id}

#### GET
##### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ---- |
| id | path |  | Yes | string |

#### PUT
##### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ---- |
| id | path |  | Yes | string |

#### DELETE
##### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ---- |
| id | path |  | Yes | string |

### /api/v1/apps/{id}

#### GET
##### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ---- |
| id | path |  | Yes | string |

#### PUT
##### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ---- |
| id | path |  | Yes | string |

#### DELETE
##### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ---- |
| id | path |  | Yes | string |

### /api/v1/users

#### GET
##### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ---- |
| pageable | query |  | Yes | Pageable |

#### POST

### /api/v1/groups

#### GET
##### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ---- |
| pageable | query |  | Yes | Pageable |

#### POST

### /api/v1/apps

#### GET
##### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ---- |
| pageable | query |  | Yes | Pageable |

#### POST

### /api/v1/users/usernames/{username}

#### GET
##### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ---- |
| username | path |  | Yes | string |

### /api/v1/users/apps/{appId}

#### GET
##### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ---- |
| appId | path |  | Yes | string |
| pageable | query |  | Yes | Pageable |

### /api/v1/groups/users/{userId}

#### GET
##### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ---- |
| userId | path |  | Yes | string |
| pageable | query |  | Yes | Pageable |

### /api/v1/groups/apps/{appId}

#### GET
##### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ---- |
| appId | path |  | Yes | string |
| pageable | query |  | Yes | Pageable |

### /api/v1/apps/names/{name}

#### GET
##### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ---- |
| name | path |  | Yes | string |

## Docker-compose Configuration

### MongoDB Configuration
The application uses MongoDB as the database. It is configured with the following parameters:
- *Host:* `mongo`
- *Port:* `27017`
- *Database:* `dynamic_event_management`
- *Username:* `luis`
- *Password:* `luissoy`


The Docker-compose file sets up three containers: MongoDB, MongoDB Express, and the application.
- *MongoDB:*
    - Container Name: `mongodb`
    - Port: `27017`
    - Username: `luis`
    - Password: `luissoy`
- *MongoDB Express:*
    - Container Name: `mongo-express`
    - Port: `10000` ([http://localhost:10000](http://localhost:10000))
    - Username: `luis`
    - Password: `luissoy`
- *Application:*
    - Container Name: `app`
    - Port: `10001` ([http://localhost:10001](http://localhost:10001))
    - MongoDB Connection: `mongo:27017`
    - Database: `dynamic_event_management`
    - Username: `luis`
    - Password: `luissoy`

## Clasificador de gravedad

Microservicio que estima la gravedad de un evento a partir de ocho constantes
vitales y devuelve una categoría junto con sus tres probabilidades.

Es un módulo de la capa externa. Las dos capas internas no lo conocen y no se
modifican para integrarlo.

### Qué contiene

| Fichero | Para qué sirve |
|---|---|
| `Entrenamiento.py` | Entrena el modelo con datos públicos de NHAMCS y genera el fichero del modelo |
| `app.py` | El microservicio |
| `Dockerfile` | Imagen del microservicio |
| `docker-compose.yml` | Servicio `clasificador-gravedad`, puerto 10004 |
| `requirements.txt` | Dependencias, con las versiones del entrenamiento |

El fichero del modelo **no está en el repositorio**. Pesa más de 100 MB, que es
el límite de GitHub, y se regenera desde el script.

### Cómo se levanta

Hace falta el modelo antes de construir la imagen, porque el `Dockerfile` lo
copia dentro.

```bash
# 1. Generar el modelo. Descarga los datos del CDC y tarda unos minutos.
python Entrenamiento.py
 
# 2. Dejarlo donde el microservicio lo espera.
cp salida_modelo/modelo_gravedad_cuatro_anos.joblib modelo_gravedad.joblib
 
# 3. Levantar el servicio. La red tfg-network la crea la capa de datos,
#    así que esa tiene que estar arriba antes.
docker-compose -p external-classification-layer -f docker-compose.yml up -d
```

Para comprobar que ha arrancado:

```bash
curl http://localhost:10004/api/v1/gravedad/salud
```

### El endpoint

`POST /api/v1/gravedad`

Desde dentro de la red de Docker la dirección es
`http://clasificador-gravedad:8000/api/v1/gravedad`. Desde fuera,
`http://localhost:10004/api/v1/gravedad`.

**Ninguno de los ocho campos es obligatorio.** El modelo lleva dentro un
imputador que rellena los que falten con la mediana del entrenamiento, de modo
que una petición incompleta también obtiene respuesta.

| Campo | Qué es |
|---|---|
| `pulse` | Frecuencia cardiaca, en pulsaciones por minuto |
| `popct` | Saturación de oxígeno, en porcentaje |
| `respr` | Frecuencia respiratoria, en respiraciones por minuto |
| `tempf` | Temperatura en grados Fahrenheit, con el decimal implícito. 101,3 se envía como `1013` |
| `age` | Edad en años |
| `sex` | 1 mujer, 2 hombre |
| `hora` | Hora del día, de 0 a 23 |
| `vdayr` | Día de la semana, 1 domingo a 7 sábado |

Ejemplo:

```bash
curl -X POST http://localhost:10004/api/v1/gravedad \
  -H "Content-Type: application/json" \
  -d '{"pulse":130,"popct":91,"respr":28,"tempf":1013,"age":74,"sex":2,"hora":3,"vdayr":6}'
```

```json
{
  "gravedad": "ALTA",
  "probabilidades": {"ALTA": 0.7417, "BAJA": 0.0149, "MEDIA": 0.2434}
}
```

Un tipo que no sea numérico devuelve 422 y no llega al modelo.

### El modelo

Bosque aleatorio entrenado sobre NHAMCS, cuatro años y 48.047 urgencias
reales. Las ocho variables son las que puede aportar un dispositivo de muñeca
más el perfil y la hora, sin tensión arterial.

A igual sensibilidad, gana 5,5 puntos de precisión a una regla construida
sobre los umbrales por franja de edad del algoritmo ESI v5. Las métricas
completas quedan en `salida_modelo/metricas_cuatro_anos.json` al entrenar.

Las versiones de `requirements.txt` están clavadas a las del entrenamiento.
Si `joblib.load` avisa de una versión inconsistente al levantar el contenedor,
eso es lo primero que hay que mirar.