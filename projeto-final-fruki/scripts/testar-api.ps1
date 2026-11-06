# Testa os endpoints principais da API Pede Fruki.
# Pré-requisito: aplicação rodando em http://localhost:8080.
# Uso: .\scripts\testar-api.ps1
#      .\scripts\testar-api.ps1 -Base "http://localhost:8081"

param(
    [string]$Base = "http://localhost:8080"
)

$ErrorActionPreference = "Continue"
$resultados = @()

function Obter-Status($url) {
    try {
        $r = Invoke-WebRequest -Uri $url -Method Get -UseBasicParsing -TimeoutSec 60
        return [int]$r.StatusCode
    } catch {
        if ($_.Exception.Response) {
            return [int]$_.Exception.Response.StatusCode
        }
        return 0
    }
}

function Verificar($nome, $caminho, $esperado) {
    $url = "$Base$caminho"
    $status = Obter-Status $url
    $ok = $status -eq $esperado
    $script:resultados += [pscustomobject]@{
        Teste    = $nome
        Esperado = $esperado
        Obtido   = $status
        Situacao = if ($ok) { "PASS" } else { "FAIL" }
    }
}

Write-Host "Testando $Base ..." -ForegroundColor Cyan

Verificar "Contexto"          "/api/v1/contexto?cidade=Porto%20Alegre&uf=RS" 200
Verificar "Estacao"           "/api/v1/contexto/estacao?data=2026-12-01"     200
Verificar "Feriados"          "/api/v1/contexto/feriados?ano=2026&cidade=Gramado" 200
Verificar "Recomendacao 41"   "/api/v1/recomendacoes/41?limite=5"             200
Verificar "Recomendacao 404"  "/api/v1/recomendacoes/999999"                  404
Verificar "Recomendacao 400"  "/api/v1/recomendacoes/abc"                     400
Verificar "Indicadores"       "/api/v1/indicadores"                           200
Verificar "Produtos"          "/api/v1/produtos?limite=10"                    200
Verificar "Catalogo sem JWT"  "/catalogo"                                     401

$resultados | Format-Table -AutoSize

$falhas = @($resultados | Where-Object { $_.Situacao -eq "FAIL" }).Count
if ($falhas -eq 0) {
    Write-Host "Todos os testes de status passaram." -ForegroundColor Green
    Write-Host ""
    Write-Host "Amostra da recomendacao:" -ForegroundColor Cyan
    try {
        $rec = Invoke-RestMethod -Uri "$Base/api/v1/recomendacoes/41?limite=3" -TimeoutSec 60
        $rec.itens | Select-Object posicao, codItem, nome, categoria, preco, score | Format-Table -AutoSize
        $rec.itens | ForEach-Object {
            Write-Host ("#{0} {1} - score {2} - {3}" -f $_.posicao, $_.nome, $_.score, ($_.motivos -join "; "))
        }
    } catch {
        Write-Host "Nao foi possivel ler a recomendacao: $($_.Exception.Message)" -ForegroundColor Yellow
    }
} else {
    Write-Host "$falhas teste(s) falharam. Veja a tabela acima." -ForegroundColor Red
    Write-Host "Dica: confira se a aplicacao esta rodando e se as variaveis de ambiente foram carregadas." -ForegroundColor Yellow
    exit 1
}
