$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot

$javaHome = $env:JAVA_HOME
if (-not $javaHome) {
    $candidate = Get-ChildItem "$env:USERPROFILE\.jdks" -Directory -ErrorAction SilentlyContinue |
        Where-Object { $_.Name -match 'openjdk-(17|21|25)' } |
        Sort-Object Name -Descending |
        Select-Object -First 1
    if ($candidate) { $javaHome = $candidate.FullName }
}
if ($javaHome) { $env:JAVA_HOME = $javaHome; $env:Path = "$javaHome\bin;$env:Path" }

$maven = 'mvn'
$repo = Join-Path $root '.m2repo'
Write-Host "== Backend tests ==" -ForegroundColor Cyan
Push-Location "$root\backend"
try {
    & $maven "-Dmaven.repo.local=$repo" -B --no-transfer-progress test
    if ($LASTEXITCODE -ne 0) { throw 'Backend tests failed' }
} finally {
    Pop-Location
}

Write-Host "== Frontend build ==" -ForegroundColor Cyan
$env:npm_config_cache = Join-Path $root '.npm-cache'
Push-Location "$root\frontend"
try {
    npm ci
    if ($LASTEXITCODE -ne 0) { throw 'npm ci failed' }
    npm run build
    if ($LASTEXITCODE -ne 0) { throw 'Frontend build failed' }
} finally {
    Pop-Location
}

Write-Host "CI equivalent finished successfully." -ForegroundColor Green
