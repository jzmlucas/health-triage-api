param(
    [string[]]$Symptoms = @('dor de cabeça leve há duas horas'),
    [int]$UserPort = 8083
)

$ErrorActionPreference = 'Stop'
if ($Symptoms.Count -eq 0 -or @($Symptoms | Where-Object { -not [string]::IsNullOrWhiteSpace($_) }).Count -ne $Symptoms.Count) {
    throw 'Informe pelo menos um sintoma não vazio.'
}

$userBase = "http://localhost:$UserPort"
$email = 'health-triage-demo-' + [guid]::NewGuid().ToString('N').Substring(0, 12) + '@example.test'
$password = 'Test-' + [guid]::NewGuid().ToString('N') + '!'
$userId = $null
$headers = $null

try {
    $account = Invoke-RestMethod -Uri "$userBase/auth/register" -Method Post -ContentType 'application/json' -Body (@{
        name = 'Teste Local'
        email = $email
        password = $password
    } | ConvertTo-Json) -TimeoutSec 20
    $userId = $account.userId
    $headers = @{ Authorization = 'Bearer ' + $account.accessToken }
    if (-not $userId -or -not $account.accessToken) { throw 'O cadastro não retornou usuário e token.' }

    Write-Host ''
    Write-Host 'Sintomas informados:' -ForegroundColor Cyan
    $Symptoms | ForEach-Object { Write-Host "  - $_" }
    Write-Host ''
    Write-Host 'Solicitando orientação preliminar ao Health Triage...' -ForegroundColor Cyan

    $request = @{ userId = $userId; symptoms = $Symptoms } | ConvertTo-Json -Depth 4
    $httpResponse = Invoke-WebRequest -Uri 'http://localhost:8081/symptoms' -Method Post -Headers $headers -ContentType 'application/json; charset=utf-8' -Body ([Text.Encoding]::UTF8.GetBytes($request)) -TimeoutSec 180 -UseBasicParsing
    $response = [Text.Encoding]::UTF8.GetString($httpResponse.RawContentStream.ToArray()) | ConvertFrom-Json
    $analysis = $response.diagnosis | ConvertFrom-Json
    if (-not $analysis.condition -or -not $analysis.risk -or -not $analysis.recommendation) {
        throw 'A resposta da IA está incompleta.'
    }

    Write-Host ''
    Write-Host "Modelo: $($response.model)" -ForegroundColor Green
    Write-Host "Risco: $($analysis.risk)" -ForegroundColor Green
    Write-Host "Possibilidades: $($analysis.condition)"
    Write-Host "Recomendação: $($analysis.recommendation)"
    if ($analysis.red_flags) {
        Write-Host 'Sinais de alerta:'
        $analysis.red_flags | ForEach-Object { Write-Host "  - $_" }
    }
    if ($analysis.questions) {
        Write-Host 'Perguntas para orientar a procura de atendimento:'
        $analysis.questions | ForEach-Object { Write-Host "  - $_" }
    }
    Write-Host ''
    Write-Host $response.disclaimer -ForegroundColor Yellow
} finally {
    if ($headers -and $userId) {
        try {
            Invoke-RestMethod -Uri "$userBase/users/$userId" -Method Delete -Headers $headers -TimeoutSec 20 | Out-Null
        } catch {
            Write-Warning 'A conta temporária não pôde ser removida.'
        }
    }
}
