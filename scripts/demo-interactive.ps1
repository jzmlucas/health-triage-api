$ErrorActionPreference = 'Stop'

Write-Host ''
Write-Host 'Health Triage - teste de sintomas' -ForegroundColor Cyan
Write-Host 'Descreva seus sintomas em uma frase. Evite informar nome ou outros dados pessoais.'
Write-Host 'Em uma emergência, procure atendimento imediato ou ligue 192.' -ForegroundColor Yellow
Write-Host ''

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
