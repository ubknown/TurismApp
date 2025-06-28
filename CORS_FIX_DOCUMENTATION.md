# CORS Configuration Fix for Spring Boot Application

## 🚨 **Problem Identified**
**Error:** "When allowCredentials is true, allowedOrigins cannot contain the special value '*'"

## 🔍 **Root Cause Analysis**
The error occurred because multiple controllers had conflicting CORS configurations:

### **Problematic Configurations Found:**
1. **AuthController**: `@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:8080", "*"})`
2. **ProfitController**: `@CrossOrigin(origins = "*")`
3. **DashboardController**: `@CrossOrigin(origins = "*")`

### **The Issue:**
- Some controllers used `origins = "*"` (wildcard)
- Other controllers used `allowCredentials = "true"`
- **Spring Security Rule**: When `allowCredentials = true`, you CANNOT use `origins = "*"`

## ✅ **Solutions Applied**

### **1. Fixed Controller-Level CORS Annotations**

**Before:**
```java
@CrossOrigin(origins = "*")  // ❌ Problematic
```

**After:**
```java
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:3000", "http://127.0.0.1:5173"}, allowCredentials = "true")  // ✅ Fixed
```

**Files Updated:**
- `AuthController.java` - Fixed wildcard in origins array
- `ProfitController.java` - Replaced `*` with specific origins
- `DashboardController.java` - Replaced `*` with specific origins

### **2. Enhanced Global CORS Configuration**

**File:** `src/main/java/com/licentarazu/turismapp/config/SecurityConfig.java`

**Key Improvements:**
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    
    // ✅ Specific origins only (NO wildcards)
    configuration.setAllowedOrigins(List.of(
        "http://localhost:5173",    // Your frontend
        "http://localhost:5174",    // Alternative port
        "http://localhost:3000",    // React alternative
        "http://127.0.0.1:5173",    // IP alternative
        "http://127.0.0.1:5174",
        "http://127.0.0.1:3000"
    ));
    
    // ✅ All necessary HTTP methods
    configuration.setAllowedMethods(Arrays.asList(
        "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"
    ));
    
    // ✅ Specific headers (NO "*" wildcard)
    configuration.setAllowedHeaders(Arrays.asList(
        "Authorization", "Content-Type", "Accept", "Origin",
        "Access-Control-Request-Method", "Access-Control-Request-Headers",
        "X-Requested-With", "Cache-Control"
    ));
    
    // ✅ Expose important headers
    configuration.setExposedHeaders(Arrays.asList(
        "Authorization", "Access-Control-Allow-Origin", "Access-Control-Allow-Credentials"
    ));
    
    // ✅ Enable credentials for JWT tokens
    configuration.setAllowCredentials(true);
    
    // ✅ Cache preflight for performance
    configuration.setMaxAge(3600L);
    
    return source;
}
```

## 🔧 **What Changed**

### **Before (Problematic):**
- Mixed use of `origins = "*"` and `allowCredentials = true`
- Some controllers had wildcard origins
- Headers configuration used `"*"` wildcard

### **After (Fixed):**
- ✅ **All origins explicitly listed** (no wildcards)
- ✅ **Consistent `allowCredentials = true`** across all controllers
- ✅ **Specific headers** instead of wildcard
- ✅ **Exposed headers** for frontend access
- ✅ **Comprehensive HTTP methods** support

## 🧪 **Testing the Fix**

### **1. Automatic Testing:**
```cmd
cd "d:\razu\Licenta\SCD\TurismApp"
.\test-cors-configuration.bat
```

### **2. Manual Testing:**
```cmd
# Test preflight request
curl -X OPTIONS http://localhost:8080/api/auth/login \
  -H "Origin: http://localhost:5173" \
  -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: Content-Type" \
  -v

# Test actual request
curl -X POST http://localhost:8080/api/auth/forgot-password \
  -H "Origin: http://localhost:5173" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"test@example.com\"}" \
  -v
```

### **3. Expected Response Headers:**
```
Access-Control-Allow-Origin: http://localhost:5173
Access-Control-Allow-Credentials: true
Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS, PATCH, HEAD
Access-Control-Allow-Headers: Authorization, Content-Type, Accept, Origin, ...
```

## 🚀 **Deployment Steps**

### **1. Restart Backend:**
```cmd
# Stop current backend (Ctrl+C)
# Then restart:
cd "d:\razu\Licenta\SCD\TurismApp"
.\mvnw.cmd spring-boot:run
```

### **2. Test Frontend:**
- Open your frontend at `http://localhost:5173`
- Try authentication operations (login, register, forgot password)
- Check browser console for CORS errors (should be gone)

### **3. Verify in Browser DevTools:**
- Open Network tab
- Look for requests to `localhost:8080`
- Check response headers for proper CORS headers

## 🔍 **Browser Testing Checklist**

Open your frontend and verify:
- [ ] Login form works without CORS errors
- [ ] Register form works without CORS errors  
- [ ] Forgot password works without CORS errors
- [ ] API calls show proper CORS headers in Network tab
- [ ] No "blocked by CORS policy" errors in console
- [ ] JWT tokens are properly sent with requests (credentials)

## 💡 **Key Security Rules Learned**

1. **Never use `origins = "*"` with `allowCredentials = true`**
2. **Always specify explicit origins** when credentials are needed
3. **Avoid header wildcards** when using credentials
4. **Controller-level CORS** can override global configuration
5. **Preflight requests** need proper OPTIONS handling

## 🛡️ **Security Benefits**

- ✅ **Explicit origin control** - Only your frontend can access the API
- ✅ **Credential security** - JWT tokens are properly handled
- ✅ **No wildcard vulnerabilities** - Specific origins prevent CSRF
- ✅ **Proper header control** - Only necessary headers are allowed

## 🔧 **Troubleshooting**

### **If CORS errors persist:**
1. **Clear browser cache** and restart browser
2. **Check controller annotations** for remaining wildcards
3. **Verify frontend URL** matches exactly (including port)
4. **Check browser console** for specific CORS error messages
5. **Test with different browsers** to isolate issues

### **Common Issues:**
- **Port mismatch**: Ensure frontend port matches CORS config
- **Protocol mismatch**: Use `http://` not `https://` for local dev
- **Cache issues**: Hard refresh browser (Ctrl+Shift+R)
- **Multiple CORS configs**: Check for conflicting configurations
