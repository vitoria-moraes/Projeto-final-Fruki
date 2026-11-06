# ============================================================================
# Pede Fruki - variaveis de ambiente para rodar LOCALMENTE
# ----------------------------------------------------------------------------
# COMO USAR
#   1. Copie este arquivo para: scripts\env.local.ps1
#   2. Preencha os valores entre <...> (o env.local.ps1 NAO vai para o Git)
#   3. No PowerShell, dentro de projeto-final-fruki:
#        . .\scripts\env.local.ps1
#        .\mvnw.cmd -B spring-boot:run
#
# NUNCA coloque token/senha no application.yml nem commite o env.local.ps1.
# Cada pessoa do grupo usa o seu proprio arquivo (workspace/token sao pessoais).
# ============================================================================

# --- Supabase (PostgreSQL transacional) -------------------------------------
# Conexao direta: jdbc:postgresql://db.<ref>.supabase.co:5432/postgres?sslmode=require
# Pooler (IPv4):  jdbc:postgresql://aws-0-<regiao>.pooler.supabase.com:5432/postgres?sslmode=require
$env:SUPABASE_JDBC_URL = "jdbc:postgresql://<host>:5432/postgres?sslmode=require"
$env:SUPABASE_USER     = "<usuario>"      # no pooler: postgres.<ref>
$env:SUPABASE_PASSWORD = "<senha>"        # cuidado com espaco no fim

# --- JWT do catalogo (minimo 32 caracteres) ---------------------------------
# Gere uma vez e mantenha:
#   [guid]::NewGuid().ToString('N') + [guid]::NewGuid().ToString('N')
$env:JWT_SECRET = "<32+ caracteres>"
$env:JWT_EXPIRATION_MINUTES = "7200"

# --- Databricks (camada analitica) ------------------------------------------
$env:DATABRICKS_HOST      = "<workspace>.cloud.databricks.com"   # sem https:// e sem :443
$env:DATABRICKS_HTTP_PATH = "/sql/1.0/warehouses/<warehouse-id>"
$env:DATABRICKS_TOKEN     = "<token>"
$env:DATABRICKS_RECOMENDACAO_TABLE = "<catalog>.<schema>.<tabela>"

# --- Opcionais ---------------------------------------------------------------
# $env:OPENWEATHER_KEY = "<chave>"
# $env:EVOLUTION_URL = "http://localhost:8081"
# $env:EVOLUTION_INSTANCE = "<instancia>"
# $env:EVOLUTION_API_KEY = "<chave>"
# $env:EVOLUTION_WEBHOOK_TOKEN = "<token>"

# --- Conferencia rapida ------------------------------------------------------
$nomes = 'SUPABASE_JDBC_URL','SUPABASE_USER','SUPABASE_PASSWORD','JWT_SECRET',
         'DATABRICKS_HOST','DATABRICKS_HTTP_PATH','DATABRICKS_TOKEN','DATABRICKS_RECOMENDACAO_TABLE'
$faltando = @()
foreach ($n in $nomes) {
    $v = [Environment]::GetEnvironmentVariable($n)
    if ([string]::IsNullOrWhiteSpace($v) -or $v -match '^<.*>$') { $faltando += $n }
}
if ($env:JWT_SECRET -and $env:JWT_SECRET.Length -lt 32) { $faltando += "JWT_SECRET (curto)" }
if ($faltando) { Write-Host "PREENCHER: $($faltando -join ', ')" -ForegroundColor Yellow }
else { Write-Host "Variaveis OK" -ForegroundColor Green }
