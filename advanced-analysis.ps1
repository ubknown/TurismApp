# Advanced File Comparison Script for TurismApp Recovery
# PowerShell script to perform detailed comparison between current and backup folders

param(
    [string]$CurrentPath = "d:\razu\Licenta\SCD\turismapp",
    [string]$BackupPath = "d:\razu\Licenta\SCD\turismapp copy",
    [string]$ReportPath = "d:\razu\Licenta\SCD\TurismApp\corruption_analysis"
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "ADVANCED TURISMAPP CORRUPTION ANALYSIS" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Create report directory
if (!(Test-Path $ReportPath)) {
    New-Item -ItemType Directory -Path $ReportPath -Force | Out-Null
}

Write-Host "Step 1: Scanning directories..." -ForegroundColor Yellow

# Get all files from both directories
$currentFiles = Get-ChildItem -Path $CurrentPath -Recurse -File | Select-Object FullName, Name, Length, LastWriteTime, @{Name="RelativePath"; Expression={$_.FullName.Replace($CurrentPath, "")}}
$backupFiles = Get-ChildItem -Path $BackupPath -Recurse -File | Select-Object FullName, Name, Length, LastWriteTime, @{Name="RelativePath"; Expression={$_.FullName.Replace($BackupPath, "")}}

Write-Host "Current directory: $($currentFiles.Count) files" -ForegroundColor Green
Write-Host "Backup directory: $($backupFiles.Count) files" -ForegroundColor Green

Write-Host ""
Write-Host "Step 2: Computing file hashes..." -ForegroundColor Yellow

# Get file hashes for corruption detection
$currentHashes = @{}
$backupHashes = @{}

$progressCount = 0
foreach ($file in $currentFiles) {
    $progressCount++
    Write-Progress -Activity "Computing current file hashes" -Status "Processing $($file.Name)" -PercentComplete (($progressCount / $currentFiles.Count) * 100)
    try {
        $hash = Get-FileHash -Path $file.FullName -Algorithm SHA256
        $currentHashes[$file.RelativePath] = @{
            Hash = $hash.Hash
            Size = $file.Length
            LastModified = $file.LastWriteTime
            FullPath = $file.FullName
        }
    }
    catch {
        Write-Warning "Could not hash file: $($file.FullName)"
    }
}

$progressCount = 0
foreach ($file in $backupFiles) {
    $progressCount++
    Write-Progress -Activity "Computing backup file hashes" -Status "Processing $($file.Name)" -PercentComplete (($progressCount / $backupFiles.Count) * 100)
    try {
        $hash = Get-FileHash -Path $file.FullName -Algorithm SHA256
        $backupHashes[$file.RelativePath] = @{
            Hash = $hash.Hash
            Size = $file.Length
            LastModified = $file.LastWriteTime
            FullPath = $file.FullName
        }
    }
    catch {
        Write-Warning "Could not hash file: $($file.FullName)"
    }
}

Write-Host ""
Write-Host "Step 3: Analyzing differences..." -ForegroundColor Yellow

# Analysis results
$identicalFiles = @()
$differentFiles = @()
$missingInCurrent = @()
$missingInBackup = @()
$corruptedFiles = @()

# Get all unique file paths
$allPaths = ($currentHashes.Keys + $backupHashes.Keys) | Sort-Object | Get-Unique

foreach ($path in $allPaths) {
    $inCurrent = $currentHashes.ContainsKey($path)
    $inBackup = $backupHashes.ContainsKey($path)
    
    if ($inCurrent -and $inBackup) {
        # File exists in both
        $currentInfo = $currentHashes[$path]
        $backupInfo = $backupHashes[$path]
        
        if ($currentInfo.Hash -eq $backupInfo.Hash) {
            $identicalFiles += [PSCustomObject]@{
                Path = $path
                Status = "Identical"
                CurrentSize = $currentInfo.Size
                BackupSize = $backupInfo.Size
                CurrentModified = $currentInfo.LastModified
                BackupModified = $backupInfo.LastModified
            }
        }
        else {
            # Same file, different content - potential corruption
            $status = if ($currentInfo.Size -eq 0) { "Corrupted (Zero bytes)" } 
                     elseif ($currentInfo.Size -ne $backupInfo.Size) { "Different Size" }
                     else { "Different Content" }
            
            $differentFiles += [PSCustomObject]@{
                Path = $path
                Status = $status
                CurrentSize = $currentInfo.Size
                BackupSize = $backupInfo.Size
                CurrentModified = $currentInfo.LastModified
                BackupModified = $backupInfo.LastModified
                CurrentHash = $currentInfo.Hash
                BackupHash = $backupInfo.Hash
            }
            
            if ($currentInfo.Size -eq 0 -or $currentInfo.Hash -eq "") {
                $corruptedFiles += $path
            }
        }
    }
    elseif ($inBackup -and !$inCurrent) {
        # Missing in current (needs restoration)
        $missingInCurrent += [PSCustomObject]@{
            Path = $path
            Status = "Missing in Current"
            BackupSize = $backupHashes[$path].Size
            BackupModified = $backupHashes[$path].LastModified
        }
    }
    elseif ($inCurrent -and !$inBackup) {
        # Extra file in current (new or backup incomplete)
        $missingInBackup += [PSCustomObject]@{
            Path = $path
            Status = "Missing in Backup"
            CurrentSize = $currentHashes[$path].Size
            CurrentModified = $currentHashes[$path].LastModified
        }
    }
}

Write-Host ""
Write-Host "Step 4: Generating reports..." -ForegroundColor Yellow

# Generate comprehensive report
$reportFile = Join-Path $ReportPath "detailed_comparison_report.html"
$htmlContent = @"
<!DOCTYPE html>
<html>
<head>
    <title>TurismApp Corruption Analysis Report</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .summary { background-color: #f0f0f0; padding: 15px; margin-bottom: 20px; border-radius: 5px; }
        .status-identical { color: green; }
        .status-different { color: orange; }
        .status-missing { color: red; }
        .status-corrupted { color: darkred; font-weight: bold; }
        table { border-collapse: collapse; width: 100%; margin-bottom: 20px; }
        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        th { background-color: #f2f2f2; }
        .priority-high { background-color: #ffebee; }
        .priority-medium { background-color: #fff3e0; }
        .priority-low { background-color: #e8f5e8; }
    </style>
</head>
<body>
    <h1>TurismApp Corruption Analysis Report</h1>
    <p>Generated: $(Get-Date)</p>
    
    <div class="summary">
        <h2>Summary</h2>
        <ul>
            <li><span class="status-identical">Identical files: $($identicalFiles.Count)</span></li>
            <li><span class="status-different">Different files: $($differentFiles.Count)</span></li>
            <li><span class="status-missing">Missing in current: $($missingInCurrent.Count)</span></li>
            <li><span class="status-missing">Missing in backup: $($missingInBackup.Count)</span></li>
            <li><span class="status-corrupted">Potentially corrupted: $($corruptedFiles.Count)</span></li>
        </ul>
    </div>
"@

# Add different files section
if ($differentFiles.Count -gt 0) {
    $htmlContent += @"
    <h2>Files Requiring Restoration (Different)</h2>
    <table>
        <tr>
            <th>File Path</th>
            <th>Status</th>
            <th>Current Size</th>
            <th>Backup Size</th>
            <th>Priority</th>
        </tr>
"@
    
    foreach ($file in $differentFiles | Sort-Object Path) {
        $priority = if ($file.Path -match "\.(java|properties|xml|yml)$") { "HIGH" }
                   elseif ($file.Path -match "\.(jsx|js|css|json)$") { "MEDIUM" }
                   else { "LOW" }
        
        $priorityClass = switch ($priority) {
            "HIGH" { "priority-high" }
            "MEDIUM" { "priority-medium" }
            "LOW" { "priority-low" }
        }
        
        $htmlContent += "<tr class='$priorityClass'><td>$($file.Path)</td><td>$($file.Status)</td><td>$($file.CurrentSize)</td><td>$($file.BackupSize)</td><td>$priority</td></tr>"
    }
    
    $htmlContent += "</table>"
}

# Add missing files section
if ($missingInCurrent.Count -gt 0) {
    $htmlContent += @"
    <h2>Files Missing in Current (Need Restoration)</h2>
    <table>
        <tr>
            <th>File Path</th>
            <th>Backup Size</th>
            <th>Priority</th>
        </tr>
"@
    
    foreach ($file in $missingInCurrent | Sort-Object Path) {
        $priority = if ($file.Path -match "\.(java|properties|xml|yml)$") { "HIGH" }
                   elseif ($file.Path -match "\.(jsx|js|css|json)$") { "MEDIUM" }
                   else { "LOW" }
        
        $priorityClass = switch ($priority) {
            "HIGH" { "priority-high" }
            "MEDIUM" { "priority-medium" }
            "LOW" { "priority-low" }
        }
        
        $htmlContent += "<tr class='$priorityClass'><td>$($file.Path)</td><td>$($file.BackupSize)</td><td>$priority</td></tr>"
    }
    
    $htmlContent += "</table>"
}

$htmlContent += @"
    <h2>Recovery Recommendations</h2>
    <ol>
        <li><strong>High Priority Files</strong> - Restore immediately (Java, config files)</li>
        <li><strong>Medium Priority Files</strong> - Restore next (Frontend source files)</li>
        <li><strong>Low Priority Files</strong> - Can be restored later or regenerated</li>
    </ol>
    
    <h3>Suggested Recovery Commands</h3>
    <pre>
# Run the selective restoration script:
.\restore-from-backup.bat

# Or manually copy high priority files:
$(($differentFiles + $missingInCurrent | Where-Object { $_.Path -match "\.(java|properties|xml|yml)$" } | ForEach-Object { "copy `"$BackupPath$($_.Path)`" `"$CurrentPath$($_.Path)`"" }) -join "`n")
    </pre>
</body>
</html>
"@

$htmlContent | Out-File -FilePath $reportFile -Encoding UTF8

# Generate CSV reports for easy processing
$differentFiles | Export-Csv -Path (Join-Path $ReportPath "different_files.csv") -NoTypeInformation
$missingInCurrent | Export-Csv -Path (Join-Path $ReportPath "missing_in_current.csv") -NoTypeInformation
$missingInBackup | Export-Csv -Path (Join-Path $ReportPath "missing_in_backup.csv") -NoTypeInformation
$identicalFiles | Export-Csv -Path (Join-Path $ReportPath "identical_files.csv") -NoTypeInformation

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "ANALYSIS COMPLETE!" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Results:" -ForegroundColor Yellow
Write-Host "  Identical files: $($identicalFiles.Count)" -ForegroundColor Green
Write-Host "  Different files: $($differentFiles.Count)" -ForegroundColor $(if($differentFiles.Count -gt 0){"Red"}else{"Green"})
Write-Host "  Missing in current: $($missingInCurrent.Count)" -ForegroundColor $(if($missingInCurrent.Count -gt 0){"Red"}else{"Green"})
Write-Host "  Missing in backup: $($missingInBackup.Count)" -ForegroundColor $(if($missingInBackup.Count -gt 0){"Yellow"}else{"Green"})
Write-Host "  Potentially corrupted: $($corruptedFiles.Count)" -ForegroundColor $(if($corruptedFiles.Count -gt 0){"Red"}else{"Green"})
Write-Host ""
Write-Host "Reports generated in: $ReportPath" -ForegroundColor Cyan
Write-Host "  - detailed_comparison_report.html (Visual report)" -ForegroundColor White
Write-Host "  - different_files.csv (Files needing restoration)" -ForegroundColor White
Write-Host "  - missing_in_current.csv (Missing files)" -ForegroundColor White
Write-Host ""

# Open the HTML report
if ($differentFiles.Count -gt 0 -or $missingInCurrent.Count -gt 0) {
    Write-Host "ATTENTION: Issues found that require restoration!" -ForegroundColor Red
    Write-Host "Opening detailed report..." -ForegroundColor Yellow
    Start-Process $reportFile
}

Write-Host "Press any key to continue..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
