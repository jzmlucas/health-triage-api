param(
    [int]$UserPort = 8083
)

$ErrorActionPreference = 'Stop'
$email = 'health-triage-smoke-' + [guid]::NewGuid().ToString('N').Substring(0, 12) + '@example.test'
$password = 'Test-' + [guid]::NewGuid().ToString('N') + '!'
$userBase = "http://localhost:$UserPort"
$headers = $null
$userId = $null

try {
    $account = Invoke-RestMethod -Uri "$userBase/auth/register" -Method Post -ContentType 'application/json' -Body (@{ name = 'Teste Local'; email = $email; password = $password } | ConvertTo-Json)
    $userId = $account.userId
    if (-not $userId) { throw 'Cadastro não retornou userId.' }
    Write-Host 'Cadastro: OK'

    $login = Invoke-RestMethod -Uri "$userBase/auth/login" -Method Post -ContentType 'application/json' -Body (@{ email = $email; password = $password } | ConvertTo-Json)
    if (-not $login.accessToken) { throw 'Login não retornou accessToken.' }
    $headers = @{ Authorization = 'Bearer ' + $login.accessToken }
    Write-Host 'Login: OK'

    $profile = Invoke-RestMethod -Uri "$userBase/users/$userId" -Headers $headers
    if ($profile.email -ne $email) { throw 'Consulta de perfil retornou outra conta.' }
    Write-Host 'Perfil: OK'

    $request = @{ userId = $userId; symptoms = @('dor de cabeça leve há duas horas') } | ConvertTo-Json
    $result = Invoke-RestMethod -Uri 'http://localhost:8081/symptoms' -Method Post -Headers $headers -ContentType 'application/json; charset=utf-8' -Body ([Text.Encoding]::UTF8.GetBytes($request)) -TimeoutSec 180
    $analysis = $result.diagnosis | ConvertFrom-Json
    if ($result.model -ne 'qwen3.5:4b' -or -not $analysis.condition -or $analysis.risk -notin @('BAIXO', 'MEDIO', 'ALTO') -or -not $analysis.recommendation) {
        throw 'Resposta de triagem incompleta ou modelo incorreto.'
    }
    Write-Host 'Triagem com qwen3.5:4b: OK'

    $cid = Invoke-RestMethod -Uri 'http://localhost:8082/ai/cid/E11' -Headers $headers -TimeoutSec 30
    if ($cid.code -ne 'E11' -or -not $cid.description) { throw 'Consulta CID-10 inválida.' }
    Write-Host 'Consulta CID-10: OK'

    try {
        $wrong = @{ userId = $userId + 1; symptoms = @('tosse') } | ConvertTo-Json
        Invoke-RestMethod -Uri 'http://localhost:8081/symptoms' -Method Post -Headers $headers -ContentType 'application/json' -Body $wrong | Out-Null
        throw 'Acesso a outra conta foi aceito.'
    } catch [System.Net.WebException] {
        if ([int]$_.Exception.Response.StatusCode -ne 403) { throw }
    }
    Write-Host 'Proteção entre contas: OK'

    Write-Host 'Fluxo principal: OK'
} finally {
    if ($headers -and $userId) {
        try {
            Invoke-RestMethod -Uri "$userBase/users/$userId" -Method Delete -Headers $headers | Out-Null
            Write-Host 'Conta de teste removida.'
        } catch {
            Write-Warning 'Não foi possível remover a conta de teste.'
        }
    }
}
