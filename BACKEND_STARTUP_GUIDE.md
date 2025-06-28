# Spring Boot Backend Startup Guide

## 🚀 Starting the Backend

### Option 1: Use the Startup Script (Recommended)
```bash
start-backend.bat
```

### Option 2: Manual Start
```bash
cd c:\Users\razvi\Desktop\SCD\TurismApp

# Using Maven wrapper (preferred)
mvnw.cmd spring-boot:run

# Or using system Maven
mvn spring-boot:run
```

## ✅ Success Indicators

Look for these messages in the console:

```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.5.0)

✅ Starting TurismappApplication using Java 17
✅ The following 1 profile is active: "default"
✅ Tomcat initialized with port(s): 8080 (http)
✅ Tomcat started on port(s): 8080 (http)
✅ Started TurismappApplication in X.XXX seconds
```

## 🔍 Health Check

After startup, run:
```bash
check-backend-health.bat
```

Or manually test:
```bash
curl http://localhost:8080/api/accommodation-units
```

## ❌ Common Issues & Solutions

### 1. Port 8080 Already in Use
**Error**: `Port 8080 was already in use`

**Solution**: Add to `application.properties`:
```properties
server.port=8081
```

Or find and stop the process using port 8080:
```bash
netstat -ano | findstr :8080
taskkill /PID <PID_NUMBER> /F
```

### 2. MySQL Connection Failed
**Error**: `Communications link failure` or `Access denied for user`

**Solutions**:
- Ensure MySQL server is running
- Verify database `turismdb` exists:
  ```sql
  CREATE DATABASE IF NOT EXISTS turismdb;
  ```
- Check credentials in `application.properties`
- Test connection manually:
  ```bash
  mysql -u root -p -h localhost -P 3306
  ```

### 3. Java Version Issues  
**Error**: `UnsupportedClassVersionError`

**Solution**: Ensure Java 17+ is installed:
```bash
java -version
```

### 4. Bean Creation Failed
**Error**: `Error creating bean with name...`

**Common causes**:
- Missing `@Repository`, `@Service`, `@Controller` annotations
- Circular dependencies
- Missing database driver

**Solution**: Check all components are properly annotated and dependencies are correct.

### 5. Maven Build Issues
**Error**: Build failures during startup

**Solution**:
```bash
# Clean and rebuild
mvn clean compile

# Skip tests if needed
mvn spring-boot:run -DskipTests
```

## 📊 Database Setup

### MySQL Database Requirements
1. **MySQL Server**: Running on localhost:3306
2. **Database**: `turismdb` 
3. **User**: `root` with password `Rzvtare112`
4. **Tables**: Auto-created by Hibernate (ddl-auto=update)

### Manual Database Creation
```sql
-- Connect to MySQL as root
mysql -u root -p

-- Create database
CREATE DATABASE IF NOT EXISTS turismdb 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- Grant permissions (if needed)
GRANT ALL PRIVILEGES ON turismdb.* TO 'root'@'localhost';
FLUSH PRIVILEGES;

-- Verify
USE turismdb;
SHOW TABLES;
```

## 🔧 Configuration Verification

### application.properties
```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/turismdb
spring.datasource.username=root
spring.datasource.password=Rzvtare112
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Server (optional port change)
# server.port=8081

# Logging (for troubleshooting)
# logging.level.com.licentarazu.turismapp=DEBUG
# logging.level.org.springframework.web=DEBUG
```

### pom.xml Dependencies
Key dependencies should include:
- `spring-boot-starter-web`
- `spring-boot-starter-data-jpa` 
- `mysql-connector-j`
- `spring-boot-starter-security` (if using auth)

## 🧪 Testing Endpoints

### Public Endpoints (No Auth Required)
```bash
# Get all accommodation units
GET http://localhost:8080/api/accommodation-units

# User registration  
POST http://localhost:8080/api/auth/register
Content-Type: application/json
{
  "name": "Test User",
  "email": "test@example.com", 
  "password": "password123",
  "role": "OWNER"
}
```

### Protected Endpoints (Auth Required)
```bash
# Login first to get JWT token
POST http://localhost:8080/api/auth/login
{
  "email": "test@example.com",
  "password": "password123" 
}

# Use returned token for protected endpoints
GET http://localhost:8080/api/dashboard/stats
Authorization: Bearer <JWT_TOKEN>
```

## 📈 Performance Tuning

For faster startup, add to `application.properties`:
```properties
# Faster startup
spring.jpa.defer-datasource-initialization=true
spring.sql.init.mode=never

# Connection pooling
spring.datasource.hikari.maximum-pool-size=5
spring.datasource.hikari.minimum-idle=2
```

## 🎯 Next Steps After Successful Startup

1. **Verify Health**: Run `check-backend-health.bat`
2. **Test API**: Use Postman collection or curl commands  
3. **Start Frontend**: `npm run dev` in `New front` folder
4. **Integration Test**: Full user flow from frontend to backend
5. **Data Creation**: Create test accommodation units and bookings

## 🔗 Useful Links

- **Backend**: http://localhost:8080
- **API Base**: http://localhost:8080/api  
- **Frontend**: http://localhost:5173 (when running)
- **Database**: localhost:3306/turismdb
