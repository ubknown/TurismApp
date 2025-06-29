# Authentication Debugging Guide - Spring Boot Login Issues

## 🚨 **Problem**: "Bad credentials" with 401 Unauthorized

### **Your Setup:**
- Email: `admin@tourism.com`
- Password: `admin123`
- BCrypt hash: 60 characters (correct length)
- Account enabled: `true`
- Role: `ADMIN`
- Password encoder: `BCryptPasswordEncoder`
- User loads correctly from database

## 🔍 **Step-by-Step Debugging Process**

### **Phase 1: Database Verification**

#### **1. Verify User Record in Database**
```sql
-- Run this in your MySQL database
SELECT 
    id, 
    email, 
    SUBSTRING(password, 1, 10) as password_prefix,
    LENGTH(password) as password_length,
    enabled, 
    role, 
    owner_status,
    created_at 
FROM users 
WHERE email = 'admin@tourism.com';
```

**Expected Results:**
- ✅ Record exists
- ✅ `password_length` = 60
- ✅ `password_prefix` starts with `$2a$` or `$2b$`
- ✅ `enabled` = 1 (true)
- ✅ `role` = 'ADMIN'

#### **2. Verify Password Hash Format**
BCrypt hashes should look like: `$2a$10$abcdefghijklmnopqrstuvwxyz...`

**Common Issues:**
- ❌ Wrong encoding algorithm
- ❌ Hash truncated or corrupted
- ❌ Wrong password stored initially

### **Phase 2: Password Verification Test**

#### **Manual Java Test (Create a test class):**
```java
@RestController
@RequestMapping("/api/debug")
public class AuthDebugController {
    
    @Autowired
    private UserRepository userRepository;
    
    @PostMapping("/test-password")
    public ResponseEntity<?> testPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");
        
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.ok("User not found");
        }
        
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        boolean matches = encoder.matches(password, user.getPassword());
        
        return ResponseEntity.ok(Map.of(
            "email", email,
            "passwordMatches", matches,
            "storedHash", user.getPassword().substring(0, 20) + "...",
            "inputPassword", password,
            "userEnabled", user.getEnabled()
        ));
    }
}
```

### **Phase 3: Authentication Chain Analysis**

#### **The Authentication Flow:**
1. **AuthController** receives login request
2. **AuthenticationManager** delegates to provider
3. **DaoAuthenticationProvider** calls UserDetailsService
4. **UserDetailsService** loads user from database
5. **PasswordEncoder** compares raw password with hash
6. If successful, creates Authentication object

#### **Where It Usually Fails:**
1. **UserDetailsService** - User not found or wrong username
2. **PasswordEncoder** - Password doesn't match hash
3. **Account Status** - User disabled, expired, locked

### **Phase 4: Common Issues & Solutions**

#### **Issue 1: Case Sensitivity**
```java
// Check if email case matters
User user1 = userRepository.findByEmail("admin@tourism.com");
User user2 = userRepository.findByEmail("ADMIN@TOURISM.COM");
```

#### **Issue 2: Whitespace Issues**
```java
// Check for hidden whitespace
String email = request.getEmail().trim();
String password = request.getPassword().trim();
```

#### **Issue 3: Password Encoding Mismatch**
```java
// Verify encoder configuration
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();  // Make sure this matches
}
```

#### **Issue 4: Multiple UserDetailsService Beans**
Check if you have multiple `UserDetailsService` implementations that might conflict.

#### **Issue 5: Security Configuration**
```java
// Ensure AuthenticationManager is properly configured
@Bean
public AuthenticationManager authenticationManager(
    AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
}
```

## 🧪 **Testing Methods**

### **Method 1: Postman/cURL Testing**
```bash
# Test login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"admin@tourism.com\",\"password\":\"admin123\"}"

# Expected: Either success (200) or detailed error
```

### **Method 2: Backend Logs Analysis**
Enable debug logging in `application.properties`:
```properties
logging.level.org.springframework.security=DEBUG
logging.level.com.licentarazu.turismapp=DEBUG
logging.level.org.springframework.security.authentication=TRACE
```

### **Method 3: Manual Password Test**
```java
// In any controller or test class
BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
String rawPassword = "admin123";
String storedHash = "$2a$10$your_actual_hash_here";
boolean matches = encoder.matches(rawPassword, storedHash);
System.out.println("Password matches: " + matches);
```

## 🔧 **Quick Fixes to Try**

### **Fix 1: Recreate Admin User**
```java
@PostMapping("/debug/recreate-admin")
public ResponseEntity<?> recreateAdmin() {
    // Delete existing admin
    userRepository.findByEmail("admin@tourism.com")
        .ifPresent(user -> userRepository.delete(user));
    
    // Create new admin with fresh password
    User admin = new User();
    admin.setEmail("admin@tourism.com");
    admin.setFirstName("Admin");
    admin.setLastName("User");
    admin.setRole(Role.ADMIN);
    admin.setEnabled(true);
    
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    admin.setPassword(encoder.encode("admin123"));
    
    userRepository.save(admin);
    
    return ResponseEntity.ok("Admin user recreated");
}
```

### **Fix 2: Enhanced Logging in UserDetailsService**
```java
@Override
public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    System.out.println("Loading user: " + email);
    
    User user = userRepository.findByEmail(email).orElse(null);
    if (user == null) {
        System.out.println("User not found: " + email);
        throw new UsernameNotFoundException("User not found: " + email);
    }
    
    System.out.println("User found: " + user.getEmail() + ", enabled: " + user.getEnabled());
    System.out.println("Password hash: " + user.getPassword());
    
    return org.springframework.security.core.userdetails.User
        .withUsername(user.getEmail())
        .password(user.getPassword())
        .authorities("ROLE_" + user.getRole().name())
        .disabled(!user.getEnabled())
        .build();
}
```

### **Fix 3: Direct Authentication Test**
```java
@PostMapping("/debug/direct-auth-test")
public ResponseEntity<?> directAuthTest(@RequestBody LoginRequest request) {
    try {
        // Step 1: Load user
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (user == null) return ResponseEntity.ok("Step 1 FAILED: User not found");
        
        // Step 2: Check enabled
        if (!user.getEnabled()) return ResponseEntity.ok("Step 2 FAILED: User disabled");
        
        // Step 3: Test password
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        boolean matches = encoder.matches(request.getPassword(), user.getPassword());
        if (!matches) return ResponseEntity.ok("Step 3 FAILED: Password mismatch");
        
        // Step 4: Test UserDetailsService
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        if (userDetails == null) return ResponseEntity.ok("Step 4 FAILED: UserDetailsService");
        
        // Step 5: Test AuthenticationManager
        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        
        return ResponseEntity.ok("ALL STEPS PASSED: Authentication successful");
        
    } catch (Exception e) {
        return ResponseEntity.ok("FAILED at: " + e.getMessage());
    }
}
```

## ⚡ **Most Likely Causes (in order)**

1. **Password hash corruption** - Hash was truncated or modified
2. **Wrong password encoder** - Using different encoder than when hash was created
3. **Case sensitivity** - Email case doesn't match database
4. **Whitespace issues** - Hidden spaces in email or password
5. **Multiple UserDetailsService** - Wrong implementation being used
6. **User account status** - Account disabled or expired
7. **Database connection** - User not actually being loaded

## 🎯 **Next Steps**

1. **Run the debug script** I created: `debug-admin-authentication.bat`
2. **Check backend logs** for detailed authentication errors
3. **Verify password hash** using manual BCrypt test
4. **Recreate admin user** if hash is corrupted
5. **Enable debug logging** to trace authentication flow

The most common issue is usually password hash corruption or using a different password encoder than when the hash was originally created.
