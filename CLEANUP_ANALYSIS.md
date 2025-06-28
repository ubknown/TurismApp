# 🧹 Project Cleanup Analysis

## ❌ Files/Folders Causing Conflicts:

### Unnecessary Node.js Files (Root Level):
- `node_modules/` - Should only be in frontend
- `package.json` - Should only be in frontend  
- `package-lock.json` - Should only be in frontend
- `resize-logos.js` - Frontend script in wrong location
- `resize-logos-improved.js` - Frontend script in wrong location

### Duplicate/Unused Frontend Folders:
- `frontend/` - Old frontend version
- `frontend_backup/` - Backup folder
- `TurismApp/src/App.jsx` - Nested React component in wrong location

### Excessive Documentation/Scripts:
- Multiple `.md` files (keeping only essential ones)
- Duplicate test scripts
- Old batch files

### Nested Project Structure:
- `TurismApp/TurismApp/` - Unnecessary nesting

## ✅ Files to Keep:
- `src/` - Main Spring Boot source
- `pom.xml` - Maven configuration
- `mvnw`, `mvnw.cmd` - Maven wrapper
- `.mvn/` - Maven wrapper config (fixed)
- `target/` - Maven build output
- `New front/` - Working frontend
- Essential `.bat` scripts

## 🎯 Cleanup Actions:
1. Remove conflicting Node.js files from root
2. Remove duplicate frontend folders
3. Remove nested TurismApp folder
4. Clean up excessive scripts and docs
5. Fix backend startup script
