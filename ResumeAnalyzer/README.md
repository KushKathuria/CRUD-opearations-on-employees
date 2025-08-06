# Resume Analyzer Application

This is a Spring Boot application that allows users to upload resumes, parse their content, and store the information in a database. The application includes user authentication, resume parsing, and a web interface for easy interaction.

## Features

- User registration and authentication with JWT
- Resume upload and parsing
- Storage of parsed resume data in a MySQL database
- File storage in AWS S3
- RESTful API for all operations
- Web interface for easy interaction

## Prerequisites

Before running the application, ensure you have the following installed:

- Java 21
- Maven
- MySQL database
- AWS account with S3 access

## Setup

### 1. Database Configuration

Create a MySQL database for the application:

```sql
CREATE DATABASE resumeanalyzer;
```

Update the database configuration in `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/resumeanalyzer
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 2. AWS S3 Configuration

Update the AWS S3 configuration in `src/main/resources/application.properties`:

```properties
aws.s3.bucketName=your_bucket_name
spring.cloud.aws.credentials.access-key=your_access_key
spring.cloud.aws.credentials.secret-key=your_secret_key
spring.cloud.aws.region.static=your_region
```

### 3. JWT Secret

For production, update the JWT secret in `src/main/resources/application.properties`:

```properties
jwt.secret=your_strong_secret_key
```

## Running the Application

### Using Maven

```bash
mvn spring-boot:run
```

### Using Java

First, build the project:

```bash
mvn clean package
```

Then, run the JAR file:

```bash
java -jar target/resume-analyzer-0.0.1-SNAPSHOT.jar
```

## API Endpoints

### Authentication

- `POST /api/auth/register` - Register a new user
- `POST /api/auth/login` - Login and obtain JWT token

### Resume Management

- `POST /api/resume/upload` - Upload and parse a resume
- `GET /api/resume/my` - Get all resumes for the current user
- `GET /api/resume/all` - Get all resumes (admin only)
- `POST /api/resume/parse` - Parse a resume file without saving
- `GET /api/resume/search?query={query}` - Search resumes by name or skills

### User Management

- `GET /api/user/my-resumes` - Get current user's resumes

### Admin Management

- `GET /api/admin/resumes` - Get all resumes (admin only)
- `DELETE /api/admin/resume/{id}` - Delete a resume (admin only)
- `GET /api/admin/users` - Get all users (admin only)
- `POST /api/admin/user/{username}/role` - Update user role (admin only)
- `DELETE /api/admin/user/{username}` - Delete a user (admin only)

## Web Interface

The application includes a web interface accessible at `http://localhost:8080`. The interface provides:

1. User registration and login
2. Resume upload and parsing
3. Display of parsed resume data

## Testing with Postman

1. Register a new user:
   - POST `http://localhost:8080/api/auth/register`
   - Body: `{ "username": "testuser", "password": "password" }`

2. Login to get JWT token:
   - POST `http://localhost:8080/api/auth/login`
   - Body: `{ "username": "testuser", "password": "password" }`

3. Upload a resume:
   - POST `http://localhost:8080/api/resume/upload`
   - Headers: `Authorization: Bearer {your_token}`
   - Body: form-data with key "file" and your resume file

## Security

The application uses JWT for authentication and Spring Security for authorization. Different endpoints require different roles:

- User endpoints: Require ROLE_USER
- Admin endpoints: Require ROLE_ADMIN

## Troubleshooting

### All endpoints return 200 OK

If you're experiencing issues where all endpoints return 200 OK even for invalid requests, ensure:

1. The JWT filter is properly configured
2. Security configuration is correctly set up
3. Authentication is required for protected endpoints

### Database Connection Issues

Ensure:
1. MySQL is running
2. Database credentials are correct
3. Database exists and is accessible

### AWS S3 Issues

Ensure:
1. AWS credentials are valid
2. S3 bucket exists and is accessible
3. Proper permissions are set for the AWS user

## Development

### Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── resumeanalyzer/
│   │           ├── config/       # Configuration classes
│   │           ├── controller/    # REST controllers
│   │           ├── dto/           # Data transfer objects
│   │           ├── entity/        # JPA entities
│   │           ├── exception/      # Exception handlers
│   │           ├── model/         # Model classes
│   │           ├── repository/     # JPA repositories
│   │           ├── service/        # Business logic services
│   │           └── util/          # Utility classes
│   └── resources/
│       ├── static/               # Static web resources
│       └── templates/            # Thymeleaf templates
└── test/                         # Unit and integration tests
```

### Technologies Used

- Spring Boot 3.5.3
- Spring Security
- Spring Data JPA
- MySQL
- AWS S3 SDK
- JWT for authentication
- Apache Tika for text extraction
- Apache PDFBox for PDF processing
- Apache POI for DOC/DOCX processing

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a pull request

## License

This project is licensed under the MIT License.
