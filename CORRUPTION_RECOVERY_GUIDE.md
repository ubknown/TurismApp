# Corruption Recovery Guide - TurismApp Project

## Overview
This guide helps you identify and restore corrupted files by comparing your current "turismapp" folder with your backup "turismapp copy" folder using only local Windows tools.

## Method 1: Using Built-in Windows Tools (Free)

### Step 1: File Count and Size Comparison
```cmd
# Navigate to parent directory containing both folders
cd "d:\razu\Licenta\SCD"

# Compare file counts in both directories
dir "turismapp" /s /a-d | find /c ".>"
dir "turismapp copy" /s /a-d | find /c ".>"

# Compare total sizes
dir "turismapp" /s
dir "turismapp copy" /s
```

### Step 2: Generate File Lists for Comparison
```cmd
# Create file lists with sizes and dates
dir "turismapp" /s /b /a-d > turismapp_files.txt
dir "turismapp copy" /s /b /a-d > turismapp_copy_files.txt

# Create detailed lists with timestamps and sizes
dir "turismapp" /s /a-d > turismapp_detailed.txt
dir "turismapp copy" /s /a-d > turismapp_copy_detailed.txt
```

### Step 3: Compare Using FC Command
```cmd
# Compare the file lists
fc turismapp_files.txt turismapp_copy_files.txt > file_differences.txt

# Compare individual files (example)
fc "turismapp\src\main\java\com\licentarazu\turismapp\controller\AuthController.java" "turismapp copy\src\main\java\com\licentarazu\turismapp\controller\AuthController.java"
```

### Step 4: Use PowerShell for Advanced Comparison
```powershell
# Get file hashes for corruption detection
Get-ChildItem "turismapp" -Recurse -File | Get-FileHash | Export-Csv turismapp_hashes.csv
Get-ChildItem "turismapp copy" -Recurse -File | Get-FileHash | Export-Csv turismapp_copy_hashes.csv

# Compare directories
Compare-Object (Get-ChildItem "turismapp" -Recurse) (Get-ChildItem "turismapp copy" -Recurse) -Property Name, Length, LastWriteTime
```

## Method 2: Using Free Third-Party Tools

### Option A: WinMerge (Recommended - Free)
1. Download WinMerge from https://winmerge.org/
2. Install and run WinMerge
3. Click "File" → "Open" → Select "Folders"
4. Left folder: Select "turismapp"
5. Right folder: Select "turismapp copy"
6. Click "Compare"
7. WinMerge will show:
   - Green files: Identical
   - Yellow files: Different
   - Red files: Missing in one folder
   - Gray files: Binary differences

### Option B: Beyond Compare (Trial - Paid)
1. Download trial from https://www.scootersoftware.com/
2. Install and run Beyond Compare
3. Select "Folder Compare"
4. Left folder: "turismapp"
5. Right folder: "turismapp copy"
6. Click "Compare"

### Option C: FreeCommander (Free)
1. Download from https://freecommander.com/
2. Use built-in folder comparison feature
3. Navigate to both folders in dual-pane view
4. Use "Tools" → "Synchronize Folders"

## Method 3: Command-Line Automation (Recommended)

### Automated Comparison Script
Run the automated script below to generate a comprehensive comparison report.

## Recovery Strategy

### Phase 1: Identify Issues
1. **Missing Files**: Files that exist in backup but not in current
2. **Different Files**: Files that exist in both but have different content/size
3. **Extra Files**: Files that exist in current but not in backup
4. **Corrupted Files**: Files with same name but different hash/content

### Phase 2: Selective Restoration
1. **Critical Files First**: 
   - Java source files (.java)
   - Configuration files (.properties, .xml, .yml)
   - Build files (pom.xml, package.json)
2. **Secondary Files**:
   - Frontend source files (.jsx, .js, .css)
   - Documentation files (.md)
3. **Generated Files** (Usually safe to ignore):
   - target/ folder contents
   - node_modules/
   - .class files

### Phase 3: Verification
1. After restoring files, run project build
2. Test application functionality
3. Verify all endpoints work correctly

## File Priority for Recovery

### High Priority (Must Restore)
- `src/main/java/**/*.java` - All Java source files
- `src/main/resources/**` - Configuration files
- `pom.xml` - Maven build configuration
- `New front/package.json` - Frontend dependencies
- `New front/src/**` - Frontend source code

### Medium Priority
- Documentation files (*.md)
- SQL scripts (*.sql)
- Batch scripts (*.bat)

### Low Priority (Can Regenerate)
- `target/` folder contents
- `node_modules/` (can be restored with npm install)
- IDE configuration files

## Safety Tips
1. **Always backup** before making changes
2. **Test incrementally** after each restoration
3. **Verify functionality** after major file restorations
4. **Keep backup folder intact** until recovery is complete
