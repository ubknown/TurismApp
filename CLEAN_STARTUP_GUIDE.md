# 🚀 Clean Backend Startup Guide

## ✅ **Project Cleaned Up**

### Removed Conflicts:
- ❌ Root-level `node_modules/` (moved to frontend only)
- ❌ Root-level `package.json` & `package-lock.json`
- ❌ Duplicate frontend folders (`frontend/`, `frontend_backup/`)
- ❌ Nested `TurismApp/TurismApp/` folder
- ❌ Conflicting JavaScript files in root

### Current Clean Structure:
```
TurismApp/
├── src/main/java/          ✅ Spring Boot source
├── src/main/resources/     ✅ Application config
├── pom.xml                 ✅ Maven config
├── mvnw, mvnw.cmd         ✅ Maven wrapper
├── .mvn/                  ✅ Wrapper config (fixed)
├── New front/             ✅ React frontend
└── *.bat scripts          ✅ Startup scripts
```

## 🎯 **Startup Steps**

### Step 1: Check Prerequisites
```bash
# Run this first:
check-mysql.bat
```

### Step 2: Start Backend
```bash
# Clean startup (recommended):
fix-and-start-backend.bat
```

### Step 3: Test Backend
```bash
# In another terminal:
test-cors-fix.bat
```

### Step 4: Start Frontend
```bash
cd "New front"
npm run dev
```

## 🔧 **Expected Backend Output**

When backend starts successfully:
```
✅ Started TurismappApplication in X.XXX seconds (JVM running for X.XXX)
✅ Tomcat started on port(s): 8080 (http) with context path ''
✅ HikariPool-1 - Start completed.
```

## 🧪 **Expected Test Results**

When testing endpoints:
```bash
curl http://localhost:8080/actuator/health
→ {"status":"UP"} ✅

curl http://localhost:8080/api/units/public
→ [] ✅ (empty array if no data)
```

## 🚨 **Troubleshooting**

### Backend Won't Start:
1. **Port 8080 in use**: Script will try to kill existing process
2. **MySQL not running**: Run `check-mysql.bat` first
3. **Java/Maven issues**: Check versions with `java -version` & `mvn -version`

### Still Getting 403:
1. **Clear browser cache**
2. **Restart backend completely**
3. **Check that request goes to exactly**: `http://localhost:8080/api/units/public`

### Connection Refused:
1. **Backend not fully started**: Wait for "Started TurismappApplication" message
2. **Firewall blocking**: Check Windows Firewall
3. **Wrong URL**: Ensure using `localhost:8080`, not `127.0.0.1` or other

## 📱 **Frontend Connection**

Once backend is running on port 8080:
1. Frontend runs on: `http://localhost:5173`
2. Frontend calls: `http://localhost:8080/api/units/public`
3. CORS allows this cross-origin request
4. No authentication required for public endpoints

## 🎉 **Success Indicators**

✅ Backend: "Started TurismappApplication" message  
✅ Health: `curl localhost:8080/actuator/health` returns 200  
✅ API: `curl localhost:8080/api/units/public` returns 200 with `[]`  
✅ Frontend: Can access search page without CORS errors
