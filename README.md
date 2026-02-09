# AuthApp Backend

A robust and secure authentication and authorization backend application built with **Spring Boot 4.0.1**. This application provides comprehensive user authentication mechanisms including JWT-based token authentication, OAuth2 integration, and role-based access control.

## Features

- **JWT Authentication** - Secure token-based authentication with JWT (JSON Web Tokens)
- **Refresh Token** - Automatic token refresh mechanism for extended sessions
- **OAuth2 Support** - Integration with OAuth2 providers for seamless third-party authentication
- **User Management** - Complete user registration, login, and profile management
- **Role-Based Access Control** - Fine-grained authorization using roles and permissions
- **JPA/Hibernate ORM** - Database operations with Spring Data JPA
- **Spring Security** - Enterprise-level security configuration
- **Input Validation** - Request validation using Jakarta validation annotations
- **Error Handling** - Global exception handling with detailed error responses
- **Cookie Management** - Secure cookie handling for token storage

## Tech Stack

- **Java 25**
- **Spring Boot 4.0.1**
  - Spring Web MVC
  - Spring Data JPA
  - Spring Security
  - Spring Validation
- **Database**: MySQL
- **ORM**: Hibernate/JPA
- **Authentication**: JWT, OAuth2
- **Build Tool**: Maven
- **Additional Libraries**:
  - Lombok (Reduce boilerplate code)
  - JSpecify (Null safety annotations)
  - Jackson (JSON processing)
  - JWT (JWT token generation and validation)

## Project Structure

```
src/main/
├── java/com/authApp/AuthApp_Backend/
│   ├── config/                    # Spring configuration classes
│   │   ├── ProjectConfig.java
│   │   └── SecurityConfig.java
│   ├── controllers/               # REST API endpoints
│   │   ├── AuthController.java
│   │   └── UserController.java
│   ├── dtos/                      # Data Transfer Objects
│   │   ├── LoginRequest.java
│   │   ├── TokenResponse.java
│   │   ├── UserDto.java
│   │   └── ...
│   ├── entities/                  # JPA Entity classes
│   │   ├── User.java
│   │   ├── Role.java
│   │   ├── RefreshToken.java
│   │   └── Provider.java
│   ├── repository/                # Spring Data repositories
│   │   ├── UserRepository.java
│   │   └── RefreshTokenRepository.java
│   ├── services/                  # Business logic services
│   │   ├── AuthService.java
│   │   ├── UserService.java
│   │   └── Impl/
│   ├── security/                  # Security-related components
│   │   ├── JwtService.java
│   │   ├── JwtAuthenticationFilter.java
│   │   ├── CustomUserDetailsService.java
│   │   ├── CookieService.java
│   │   └── OAuth2SuccessHandler.java
│   ├── exceptions/                # Custom exception classes
│   │   ├── GlobalExceptionHandler.java
│   │   └── ResourceNotFoundException.java
│   ├── helper/                    # Utility helper classes
│   │   └── UserHelper.java
│   └── AuthAppBackendApplication.java
└── resources/
    ├── application.yaml           # Main configuration
    ├── application-dev.yaml       # Development profile
    └── ...
```

## API Endpoints

### Authentication Endpoints (`/api/v1/auth`)

#### User Registration
```
POST /api/v1/auth/register
Content-Type: application/json

{
  "username": "string",
  "email": "string",
  "password": "string"
}

Response: 200 OK
{
  "message": "User registered successfully"
}
```

#### User Login
```
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "string",
  "password": "string"
}

Response: 200 OK
{
  "accessToken": "jwt_token",
  "refreshToken": "refresh_token",
  "expiresIn": 3600
}
```

#### Refresh Token
```
POST /api/v1/auth/refresh
Content-Type: application/json

{
  "refreshToken": "string"
}

Response: 200 OK
{
  "accessToken": "new_jwt_token",
  "refreshToken": "new_refresh_token",
  "expiresIn": 3600
}
```

### User Endpoints (`/api/v1/users`)
Secured endpoints requiring valid JWT token in Authorization header.

## Getting Started

### Prerequisites

- **Java 25** or higher
- **Maven 3.8.0** or higher
- **MySQL 8.0** or higher
- **Git**

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/AuthApp-Backend.git
   cd AuthApp-Backend
   ```

2. **Configure Database**
   
   Create a MySQL database:
   ```sql
   CREATE DATABASE authapp_db;
   ```

   Update database configuration in `src/main/resources/application-dev.yaml`:
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/authapp_db
       username: root
       password: your_password
     jpa:
       hibernate:
         ddl-auto: update
   ```

3. **Build the project**
   ```bash
   mvn clean install
   ```

4. **Run the application**
   ```bash
   mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
   ```

   Or using the Maven wrapper:
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
   ```

5. **Verify the application**
   
   The application will start on `http://localhost:8082`
   
   Check health endpoint:
   ```bash
   curl http://localhost:8082/actuator/health
   ```

## Configuration

### Environment Profiles

- **dev** - Development environment with detailed logging
- **prod** - Production environment (configure as needed)

### JWT Configuration

Configure JWT settings in your properties/yaml file:
```yaml
jwt:
  secret: your_secret_key_here
  expiration: 3600000  # 1 hour in milliseconds
  refresh-expiration: 604800000  # 7 days in milliseconds
```

### OAuth2 Configuration

Enable OAuth2 authentication by configuring OAuth2 provider details:
```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: your_client_id
            client-secret: your_client_secret
```

## Security Features

- ✅ **CSRF Protection** - Configured appropriately for stateless REST API
- ✅ **Stateless Sessions** - Using JWT tokens for scalability
- ✅ **Password Encoding** - BCrypt password hashing
- ✅ **CORS Configuration** - Properly configured for frontend integration
- ✅ **Input Validation** - All user inputs validated using Jakarta validators
- ✅ **Exception Handling** - Centralized error handling with consistent response format

## Testing

Run unit and integration tests:
```bash
mvn test
```

## Build

Create a production-ready JAR file:
```bash
mvn clean package
```

This generates `target/AuthApp-Backend-0.0.1-SNAPSHOT.jar`

## Docker Support (Optional)

Create a `Dockerfile` in the project root:
```dockerfile
FROM openjdk:25-slim
COPY target/AuthApp-Backend-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

Build and run:
```bash
docker build -t authapp-backend:1.0 .
docker run -p 8082:8082 authapp-backend:1.0
```

## Database Schema

### User Table
- id (PRIMARY KEY)
- username (UNIQUE)
- email (UNIQUE)
- password
- enabled
- created_at
- updated_at

### Role Table
- id (PRIMARY KEY)
- role_name
- description

### RefreshToken Table
- id (PRIMARY KEY)
- token
- user_id (FOREIGN KEY)
- expiry_date

### Provider Table
- id (PRIMARY KEY)
- provider_name
- provider_id

## API Response Format

### Success Response
```json
{
  "statusCode": 200,
  "message": "Operation successful",
  "data": {}
}
```

### Error Response
```json
{
  "statusCode": 400,
  "message": "Error description",
  "errors": {
    "field": "Field error message"
  }
}
```

## Troubleshooting

### Database Connection Issues
- Ensure MySQL is running
- Verify database credentials in configuration
- Check database URL format

### JWT Token Issues
- Verify JWT secret key configuration
- Ensure token is included in Authorization header as `Bearer <token>`
- Check token expiration time

### CORS Issues
- Verify CORS configuration in `SecurityConfig`
- Enable frontend origin in allowed origins

## Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Contact & Support

For support, email: your-email@example.com

Project Link: [https://github.com/varunkushwah31/Auth_App]

## Roadmap

- [ ] Two-Factor Authentication (2FA)
- [ ] Rate Limiting
- [ ] API Documentation (Swagger/SpringDoc)
- [ ] Enhanced Audit Logging
- [ ] Redis Caching Integration
- [ ] Multi-Tenant Support
- [ ] Advanced Permission Management

---

**Version**: 0.0.1-SNAPSHOT  
**Created**: 2024  
**Last Updated**: February 2026
