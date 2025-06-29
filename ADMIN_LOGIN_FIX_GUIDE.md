# ADMIN LOGIN TROUBLESHOOTING GUIDE

## 🔍 Problem Analysis
You're getting "Invalid email or password" when trying to log in with:
- Email: admin@tourism.com  
- Password: admin123

## 🐛 Root Cause Found
**DATABASE NAME MISMATCH**: Your application.properties was pointing to `turismdb` but your schema creates `turismdb`.

## ✅ Steps to Fix

### Step 1: Database Configuration Fix
I've already updated your `application.properties` to use the correct database name:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/turismdb
```

### Step 2: Verify Database Setup
1. **Connect to MySQL** and run:
```sql
USE turismdb;
SELECT * FROM users WHERE email = 'admin@turismapp.com';
```

2. **If no admin user found**, run the `create-admin-plaintext.sql` script:
```bash
mysql -u root -p turismdb < create-admin-plaintext.sql
```

### Step 3: Restart Backend
1. Stop your Spring Boot application
2. Start it again with:
```bash
mvn spring-boot:run
```

### Step 4: Test Login
Run the debug script I created:
```bash
debug-admin-login.bat
```

## 🔧 Alternative Solutions

### Option A: Manual Database Fix
```sql
USE turismdb;

-- Delete existing admin if any
DELETE FROM users WHERE email = 'admin@turismapp.com';

-- Create admin user
INSERT INTO users (first_name, last_name, email, password, enabled, role, owner_status, created_at) 
VALUES ('Admin', 'User', 'admin@turismapp.com', 'admin123', TRUE, 'ADMIN', 'NONE', NOW()); 
        '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 
        TRUE, 'ADMIN', NOW());
```

### Option B: Test Other Users
If admin still doesn't work, try these credentials:

**Owner Account:**
- Email: owner@example.com
- Password: owner123

**Guest Account:**
- Email: guest@example.com
- Password: guest123

### Option C: Generate New Password Hash
Run the `PasswordHashGenerator.java` I created to generate a fresh password hash:
```bash
javac -cp target/classes src/main/java/com/licentarazu/turismapp/util/PasswordHashGenerator.java
java -cp target/classes:. com.licentarazu.turismapp.util.PasswordHashGenerator
```

## 🧪 Verification Steps

1. **Check backend logs** for authentication errors
2. **Verify database connection** - should show `turismdb` in logs
3. **Test API endpoint**:
```bash
curl -X POST "http://localhost:8080/api/admin/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@turismapp.com","password":"admin123"}'
```

## 🔍 Common Issues & Solutions

| Issue | Solution |
|-------|----------|
| Database not found | Create `turismdb` database |
| Admin user not found | Run `create-admin-plaintext.sql` |
| Wrong password format | Use plain text password with NoOpPasswordEncoder |
| User not enabled | Set `enabled = TRUE` in database |
| Wrong role | Set `role = 'ADMIN'` in database |

## 📋 Checklist

- [ ] Updated application.properties database name
- [ ] Restarted Spring Boot backend  
- [ ] Verified admin user exists in turismdb
- [ ] Admin user has enabled = TRUE
- [ ] Admin user has role = 'ADMIN'
- [ ] Password hash is BCrypt format
- [ ] Tested login via curl/Postman
- [ ] Checked backend logs for errors

## 🎯 Expected Result

After fixing, you should be able to:
1. Login with admin@tourism.com / admin123
2. Be redirected to `/dashboard` 
3. See admin navigation options
4. Access all admin functionality

Let me know if you need help with any of these steps!
