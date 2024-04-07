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
| pageable | query |  | Yes | [Pageable](#Pageable) |

#### POST

### /api/v1/groups

#### GET
##### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ---- |
| pageable | query |  | Yes | [Pageable](#Pageable) |

#### POST

### /api/v1/apps

#### GET
##### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ---- |
| pageable | query |  | Yes | [Pageable](#Pageable) |

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
| pageable | query |  | Yes | [Pageable](#Pageable) |

### /api/v1/groups/users/{userId}

#### GET
##### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ---- |
| userId | path |  | Yes | string |
| pageable | query |  | Yes | [Pageable](#Pageable) |

### /api/v1/groups/apps/{appId}

#### GET
##### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ---- |
| appId | path |  | Yes | string |
| pageable | query |  | Yes | [Pageable](#Pageable) |

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
