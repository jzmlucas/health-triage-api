param(
    [int]$UserPort = 8083,
    [int]$DatabasePort = 5433
)

$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
$envFile = Join-Path $root '.env.local'
$databaseContainer = 'health-triage-postgres'

foreach ($command in @('docker', 'ollama', 'java')) {
    if (-not (Get-Command $command -ErrorAction SilentlyContinue)) {
        throw "Comando '$command' não encontrado. Instale-o e abra um novo PowerShell."
    }
}

$javaVersion = (& java --version | Select-Object -First 1).ToString()
if ($javaVersion -notmatch '^(?:openjdk|java) 25\.') {
    throw "Este projeto exige JDK 25. Versão ativa: $javaVersion"
}

if (-not (Test-Path $envFile)) {
    $jwtBytes = New-Object byte[] 48
    $passwordBytes = New-Object byte[] 24
    $rng = [Security.Cryptography.RandomNumberGenerator]::Create()
    try {
        $rng.GetBytes($jwtBytes)
        $rng.GetBytes($passwordBytes)
    } finally {
        $rng.Dispose()
    }
    $jwt = [BitConverter]::ToString($jwtBytes).Replace('-', '')
    $password = [BitConverter]::ToString($passwordBytes).Replace('-', '')
    @(
        "JWT_SECRET=$jwt"
        "DB_PASSWORD=$password"
        "POSTGRES_PASSWORD=$password"
        'POSTGRES_DB=health_ai_userservice'
        'POSTGRES_USER=healthtriage'
        "DATABASE_URL=jdbc:postgresql://localhost:$DatabasePort/health_ai_userservice"
    ) | Set-Content -LiteralPath $envFile -Encoding ASCII
    Write-Host 'Credenciais locais geradas em .env.local (ignorado pelo Git).'
}

$config = Get-Content -LiteralPath $envFile -Raw | ConvertFrom-StringData
foreach ($key in @('JWT_SECRET', 'DB_PASSWORD', 'DATABASE_URL', 'POSTGRES_PASSWORD', 'POSTGRES_DB', 'POSTGRES_USER')) {
    if (-not $config[$key]) { throw "Variável $key ausente de .env.local" }
}
if ($config['JWT_SECRET'].Length -lt 32) { throw 'JWT_SECRET deve ter pelo menos 32 caracteres.' }
if ($config['DATABASE_URL'] -notmatch ":$DatabasePort/health_ai_userservice$") {
    throw "DATABASE_URL em .env.local deve apontar para localhost:$DatabasePort/health_ai_userservice."
}

if (-not ((& ollama list) -match 'qwen3\.5:4b')) {
    throw 'Modelo qwen3.5:4b ausente. Execute: ollama pull qwen3.5:4b'
}

$existingContainer = & docker ps -a --filter "name=^${databaseContainer}$" --format '{{.Names}}'
if (-not $existingContainer) {
    & docker run -d --name $databaseContainer --env-file $envFile -p "127.0.0.1:${DatabasePort}:5432" -v health-triage-postgres-data:/var/lib/postgresql/data postgres:16-alpine | Out-Null
} elseif (-not (& docker ps --filter "name=^${databaseContainer}$" --format '{{.Names}}')) {
    & docker start $databaseContainer | Out-Null
}
if ($LASTEXITCODE -ne 0) { throw 'Não foi possível iniciar o PostgreSQL.' }

$databaseReady = $false
for ($attempt = 0; $attempt -lt 20; $attempt++) {
    & docker exec $databaseContainer pg_isready -U $config['POSTGRES_USER'] -d $config['POSTGRES_DB'] *> $null
    if ($LASTEXITCODE -eq 0) { $databaseReady = $true; break }
    Start-Sleep -Seconds 1
}
if (-not $databaseReady) { throw 'PostgreSQL não ficou pronto.' }

foreach ($port in @(8081, 8082, $UserPort)) {
    if (Get-NetTCPConnection -State Listen -LocalPort $port -ErrorAction SilentlyContinue) {
        throw "Porta $port já está em uso. Feche o serviço existente antes de iniciar outra instância."
    }
}

$env:JWT_SECRET = $config['JWT_SECRET']
$env:DB_PASSWORD = $config['DB_PASSWORD']
$env:DATABASE_URL = $config['DATABASE_URL']
$env:DB_USERNAME = $config['POSTGRES_USER']

$services = @(
    @{ Name = 'user-service'; Command = "`$env:SERVER_PORT='$UserPort'; .\mvnw.cmd spring-boot:run" }
    @{ Name = 'ai-service'; Command = "`$env:OLLAMA_MODEL='qwen3.5:4b'; .\mvnw.cmd spring-boot:run" }
    @{ Name = 'symptomservice'; Command = "`$env:USER_SERVICE_URL='http://localhost:$UserPort'; .\mvnw.cmd spring-boot:run" }
)

foreach ($service in $services) {
    Start-Process powershell.exe -WorkingDirectory (Join-Path $root $service.Name) -ArgumentList @('-NoExit', '-Command', $service.Command) | Out-Null
    Write-Host "Janela aberta: $($service.Name)"
}

Write-Host "Aguarde a inicialização. user-service: $UserPort; symptomservice: 8081; ai-service: 8082."
