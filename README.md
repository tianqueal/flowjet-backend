# FlowJet Backend

A robust backend application for managing personal and small team projects, built with **Kotlin** and **Spring Boot**. This application provides comprehensive project management capabilities including user authentication, real-time collaboration, and task management features.

## 🚀 Features

- **Project Management**: Create, update, and manage projects with detailed tracking
- **Task Management**: Complete task lifecycle management with assignments, comments, and status tracking
- **User Authentication**: Secure OAuth2 JWT-based authentication system
- **Real-time Collaboration**: WebSocket integration for live updates and notifications
- **Team Management**: Role-based access control with project member management
- **Email Integration**: Automated email notifications and verification system
- **Database Migration**: Liquibase-powered database versioning and migration
- **API Documentation**: Comprehensive OpenAPI/Swagger documentation
- **Internationalization**: Multi-language support with i18n
- **Caching**: Caffeine-based caching for improved performance

## 🛠️ Technology Stack

- **Language**: Kotlin 1.9.25
- **Framework**: Spring Boot 3.5.0
- **Database**: PostgreSQL (with H2 for testing)
- **Security**: Spring Security with OAuth2 Resource Server
- **Real-time**: WebSocket support
- **Database Migration**: Liquibase
- **Build Tool**: Gradle with Kotlin DSL
- **Testing**: JUnit 5, Spring Boot Test, GreenMail
- **Documentation**: SpringDoc OpenAPI
- **Caching**: Caffeine Cache
- **Template Engine**: FreeMarker

## 📋 Prerequisites

Before running this application, ensure you have the following installed:

- **Java 21** or higher
- **PostgreSQL 12** or higher
- **Gradle 8.0** or higher (optional, can use wrapper)

## 🚦 Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/tianqueal/flowjet-backend.git
cd flowjet-backend
```

### 2. Environment Configuration

Create a `.env` file in the root directory or set the following environment variables:

```dotenv
# Database Configuration
DB_HOST=localhost
DB_PORT=5432
DB_NAME=flowjet_db
DB_USER=flowjet_user
DB_PASSWORD=your_password

# Application Configuration
SPRING_PROFILES_ACTIVE=dev
SPRING_APPLICATION_NAME=flowjet-backend
APP_VERSION=1.0.0

# Mail Configuration
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
MAIL_FROM=noreply@yourdomain.com
MAIL_FROM_NAME="FlowJet Team"

# Frontend Configuration
FRONTEND_BASE_URL=http://localhost:3000

# Application Info
APP_NAME="FlowJet Backend"
APP_DESCRIPTION="Backend application for managing personal and small team projects"
APP_AUTHOR=tianqueal
APP_CONTACT_EMAIL=tianqueal@protonmail.com
APP_CONTACT_URL=https://yourdomain.com
```

### 3. Database Setup

#### Using Docker Compose (Recommended)

```bash
# Start PostgreSQL database (in any case, Spring Boot will detect the compose file and configure the datasource automatically)
docker-compose up -d postgres
```

#### Manual PostgreSQL Setup

1. Install PostgreSQL
2. Create database and user:

```sql
CREATE DATABASE flowjet_db;
CREATE USER flowjet_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE flowjet_db TO flowjet_user;
```

### 4. JWT Configuration

Generate RSA key pair for JWT signing:

```bash
# Create certs directory
mkdir -p src/main/resources/certs

# Generate private key
openssl genrsa -out src/main/resources/certs/private-key.pem 2048

# Generate public key
openssl rsa -in src/main/resources/certs/private-key.pem -pubout -out src/main/resources/certs/public-key.pem
```

### 5. Running the Application

#### Development Mode

```bash
# Using Gradle wrapper (recommended)
./gradlew bootRun

# Or using installed Gradle
gradle bootRun
```

#### Production Mode

```bash
# Build the application
./gradlew build

# Run the JAR file
java -jar build/libs/flowjet.backend-*.jar
```

The application will start on `http://localhost:8080`

## 📚 API Documentation

Once the application is running, you can access the interactive API documentation at:

- **Swagger UI**: `http://localhost:8080/swagger-ui/index.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

## 🧪 Testing

Run the test suite:

```bash
# Run all tests
./gradlew test

# Run tests with coverage
./gradlew test jacocoTestReport

# Run specific test class
./gradlew test --tests "com.tianqueal.flowjet.backend.controllers.v1.ProjectControllerTest"
```

## 🏗️ Project Structure

```
src/
├── main/
│   ├── kotlin/com/tianqueal/flowjet/backend/
│   │   ├── annotations/           # Custom validation annotations
│   │   ├── config/               # Configuration classes
│   │   ├── controllers/v1/       # REST controllers
│   │   ├── domain/
│   │   │   ├── dto/v1/          # Data Transfer Objects
│   │   │   └── entities/        # JPA entities
│   │   ├── exceptions/          # Custom exceptions
│   │   ├── mappers/v1/          # Entity-DTO mappers
│   │   ├── repositories/        # Data access layer
│   │   ├── security/            # Security configurations
│   │   ├── services/            # Business logic layer
│   │   ├── specifications/      # JPA specifications
│   │   └── utils/               # Utility classes
│   └── resources/
│       ├── db/changelog/        # Liquibase migrations
│       ├── i18n/               # Internationalization files
│       ├── certs/              # JWT certificates
│       └── application.yaml    # Application configuration
└── test/                       # Test files
```

## 🔧 Configuration

### Database Configuration

The application uses PostgreSQL in production and H2 for testing. Database migrations are managed through Liquibase.

### Security Configuration

- JWT-based authentication with RSA key pair
- Role-based access control
- CORS configuration for frontend integration
- OAuth2 Resource Server setup

### Caching Configuration

Caffeine cache is configured for:
- Project permissions (10 minutes TTL, 1000 max entries)

## 🚀 Deployment

### Docker Deployment

```bash
# Build and run with Docker Compose
docker-compose up --build
```

### Traditional Deployment

1. Build the application:
    ```bash
    ./gradlew build
    ```

2. Deploy the generated JAR file:
    ```bash
    java -jar build/libs/flowjet.backend-*.jar
    ```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Commit your changes: `git commit -m 'Add some amazing feature'`
4. Push to the branch: `git push origin feature/amazing-feature`
5. Open a Pull Request

### Code Style

This project follows Kotlin coding conventions and uses ktlint for code formatting.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Author

**Christian Alvarado** ([@tianqueal](https://github.com/tianqueal))

## 📞 Support

For support and questions:
- Create an issue in the [GitHub repository](https://github.com/tianqueal/flowjet-backend/issues)
- Contact: [tianqueal@protonmail.com](mailto:tianqueal@protonmail.com)

**FlowJet Backend** - Streamlining project management for teams of all sizes.
