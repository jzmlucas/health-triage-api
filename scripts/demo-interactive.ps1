$ErrorActionPreference = 'Stop'

Write-Host ''
Write-Host 'Health Triage - teste de sintomas' -ForegroundColor Cyan
Write-Host 'Descreva seus sintomas em uma frase. Evite informar nome ou outros dados pessoais.'
Write-Host 'Em uma emergência, procure atendimento imediato ou ligue 192.' -ForegroundColor Yellow
Write-Host ''

function Get-ActiveServicePorts {
    @(8081, 8082, 8083) | Where-Object {
        Get-NetTCPConnection -State Listen -LocalPort $_ -ErrorAction SilentlyContinue
    }
}

$activePorts = @(Get-ActiveServicePorts)
if ($activePorts.Count -eq 0) {
    Write-Host 'Os três serviços estão parados. Iniciando o Health Triage...' -ForegroundColor Cyan
    try {
        & (Join-Path $PSScriptRoot 'start-local.ps1')
        if ($LASTEXITCODE -ne 0 -and $LASTEXITCODE -ne $null) { throw 'A inicialização falhou.' }
        $ready = $false
        for ($attempt = 0; $attempt -lt 60; $attempt++) {
            if (@(Get-ActiveServicePorts).Count -eq 3) { $ready = $true; break }
            Start-Sleep -Seconds 2
        }
        if (-not $ready) { throw 'Os serviços não ficaram prontos em dois minutos.' }
        Write-Host 'Serviços prontos.' -ForegroundColor Green
        Write-Host ''
    } catch {
        Write-Host "Não foi possível iniciar os serviços: $($_.Exception.Message)" -ForegroundColor Red
        Write-Host 'Abra o Docker Desktop e o Ollama e tente novamente.'
        return
    }
} elseif ($activePorts.Count -ne 3) {
    Write-Host "Apenas parte dos serviços está ativa (portas: $($activePorts -join ', '))." -ForegroundColor Yellow
    Write-Host 'Feche as janelas dos serviços e execute .\scripts\start-local.ps1 na raiz do projeto.'
    return
}

$description = Read-Host 'O que você está sentindo?'
if ([string]::IsNullOrWhiteSpace($description)) {
    Write-Host 'Nenhum sintoma informado. Execute o atalho novamente para tentar.' -ForegroundColor Yellow
    return
}

try {
    & (Join-Path $PSScriptRoot 'demo-symptoms.ps1') -Symptoms @($description.Trim())
} catch {
    Write-Host ''
    Write-Host "Não foi possível concluir o teste: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host 'Confirme que Docker Desktop, Ollama e os três serviços estão em execução.'
    Write-Host 'Na raiz do projeto, inicie os serviços com .\scripts\start-local.ps1.'
}

Write-Host ''
Write-Host 'Você pode fechar esta janela quando terminar.'
