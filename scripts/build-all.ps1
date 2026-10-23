$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot

Write-Host '== 1/3 Build frontend ==' -ForegroundColor Cyan
Push-Location "$root\frontend"
try {
    npm install
    if ($LASTEXITCODE -ne 0) { throw 'npm install failed' }
    npm run build
    if ($LASTEXITCODE -ne 0) { throw 'npm run build failed' }
} finally {
    Pop-Location
}

Write-Host '== 2/3 Copy dist to backend static ==' -ForegroundColor Cyan
$static = "$root\backend\src\main\resources\static"
New-Item -ItemType Directory -Force -Path $static | Out-Null
if (Test-Path "$static\assets") { Remove-Item "$static\assets" -Recurse -Force }
Copy-Item "$root\frontend\dist\*" $static -Recurse -Force

Write-Host '== 3/3 Package single jar ==' -ForegroundColor Cyan
$javaHome = $env:JAVA_HOME
if (-not $javaHome) {
    $candidate = Get-ChildItem "$env:USERPROFILE\.jdks" -Directory -ErrorAction SilentlyContinue |
        Where-Object { $_.Name -match 'openjdk-(17|21|25)' } |
        Sort-Object Name -Descending | Select-Object -First 1
    if ($candidate) { $javaHome = $candidate.FullName }
}
if ($javaHome) {
    $env:JAVA_HOME = $javaHome
    $env:Path = "$javaHome\bin;$env:Path"
}

Push-Location "$root\backend"
try {
    mvn clean package
    if ($LASTEXITCODE -ne 0) { throw 'mvn package failed' }
} finally {
    Pop-Location
}

Write-Host 'Build finished: backend\target\learnmate.jar' -ForegroundColor Green
