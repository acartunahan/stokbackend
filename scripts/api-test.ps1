param(
    [string]$BaseUrl = 'http://localhost:8080',
    [string]$Container = 'stoktakip-oracle',
    [string]$OracleConnect = 'stoktakip/stoktakip123@//localhost:1521/FREEPDB1',
    [string]$ResultsPath = 'target/api-test-results.json'
)

$ErrorActionPreference = 'Stop'
$results = [System.Collections.Generic.List[object]]::new()
$ids = @{}
$runId = Get-Date -Format 'yyyyMMddHHmmss'
$prefix = "CODEX_TEST_$runId"

function Add-Result {
    param($Controller, $Method, $Endpoint, $Test, $Status, $Expected, $Db, $Pass, $Body)
    $results.Add([pscustomobject]@{
        Controller = $Controller; Method = $Method; Endpoint = $Endpoint; Test = $Test
        HttpStatus = $Status; Expected = $Expected; DbVerified = $Db
        Result = $(if ($Pass) { 'PASS' } else { 'FAIL' }); Response = $Body
    })
}

function Invoke-Api {
    param(
        [string]$Controller, [string]$Method, [string]$Path, [string]$Test,
        [int[]]$ExpectedStatus, $Body = $null, [string]$Db = '-'
    )
    try {
        $parameters = @{
            Uri = "$BaseUrl$Path"; Method = $Method; SkipHttpErrorCheck = $true
            UseBasicParsing = $true; TimeoutSec = 30
        }
        if ($null -ne $Body) {
            $parameters.ContentType = 'application/json'
            $parameters.Body = $Body | ConvertTo-Json -Depth 10 -Compress
        }
        $response = Invoke-WebRequest @parameters
        $raw = [string]$response.Content
        $json = if ([string]::IsNullOrWhiteSpace($raw)) { $null } else {
            try { $raw | ConvertFrom-Json -Depth 20 } catch { $null }
        }
        $pass = $ExpectedStatus -contains [int]$response.StatusCode
        Add-Result $Controller $Method $Path $Test ([int]$response.StatusCode) ($ExpectedStatus -join '/') $Db $pass $raw
        return [pscustomobject]@{ Status = [int]$response.StatusCode; Raw = $raw; Json = $json; Pass = $pass }
    } catch {
        Add-Result $Controller $Method $Path $Test 0 ($ExpectedStatus -join '/') $Db $false $_.Exception.Message
        return [pscustomobject]@{ Status = 0; Raw = $_.Exception.Message; Json = $null; Pass = $false }
    }
}

function Invoke-Sql {
    param([string]$Sql)
    $script = @"
WHENEVER SQLERROR EXIT SQL.SQLCODE
SET HEADING OFF FEEDBACK OFF PAGES 0 VERIFY OFF ECHO OFF
$Sql
EXIT;
"@
    $output = $script | docker exec -i $Container sqlplus -s $OracleConnect
    if ($LASTEXITCODE -ne 0) { throw "Oracle SQL failed: $output" }
    return (($output | Where-Object { $_ -and $_.Trim() }) -join "`n").Trim()
}

function Assert-Db {
    param([string]$Controller, [string]$Method, [string]$Endpoint, [string]$Test,
        [string]$Sql, [string]$Expected)
    try {
        $actual = Invoke-Sql $Sql
        $pass = $actual -eq $Expected
        Add-Result $Controller $Method $Endpoint $Test '-' '-' 'YES' $pass "expected=$Expected actual=$actual"
        return $pass
    } catch {
        Add-Result $Controller $Method $Endpoint $Test '-' '-' 'NO' $false $_.Exception.Message
        return $false
    }
}

function Assert-Value {
    param([string]$Controller, [string]$Method, [string]$Endpoint, [string]$Test,
        [bool]$Condition, [string]$Details)
    Add-Result $Controller $Method $Endpoint $Test '-' '-' '-' $Condition $Details
    return $Condition
}

function Contains-Guid($Json, [string]$Guid) {
    return @($Json | Where-Object { $_.guid -eq $Guid }).Count -gt 0
}

function Cleanup-TestData {
    if (-not $ids.Envanter1 -and -not $ids.Envanter2) { return }
    $envIds = @($ids.Envanter1, $ids.Envanter2, $ids.EnvanterDelete) | Where-Object { $_ }
    foreach ($envId in $envIds) {
        Invoke-Sql @"
DELETE FROM PERSONEL_ZIMMET_HAREKET WHERE ENVANTER_GUID = '$envId';
DELETE FROM DEPO_ZIMMET_HAREKET WHERE ENVANTER_GUID = '$envId';
DELETE FROM APARAT WHERE ENVANTER_GUID = '$envId';
DELETE FROM ENVANTER_DETAY WHERE ENVANTER_GUID = '$envId';
DELETE FROM ENVANTER WHERE GUID = '$envId';
COMMIT;
"@ | Out-Null
    }
    Invoke-Sql @"
DELETE FROM IHALE WHERE AD LIKE '$prefix%';
DELETE FROM MODEL WHERE MODEL_ADI LIKE '$prefix%';
DELETE FROM MARKA WHERE MARKA_ADI LIKE '$prefix%';
DELETE FROM KATEGORI WHERE BASLIK LIKE '$prefix%';
DELETE FROM FIRMA WHERE AD LIKE '$prefix%';
DELETE FROM DEPO WHERE AD LIKE '$prefix%';
COMMIT;
"@ | Out-Null
}

try {
    $identity = Invoke-Sql "SELECT SYS_CONTEXT('USERENV','CON_NAME') || '|' || SYS_CONTEXT('USERENV','SERVER_HOST') FROM DUAL;"
    if (-not $identity.StartsWith('FREEPDB1|')) { throw "Unsafe Oracle target: $identity" }

    # KategoriController: parent/child create, every read route, update, validation and delete.
    $r = Invoke-Api KategoriController POST '/api/kategoriler' Create @(201) @{ baslik="$prefix`_KATEGORI" } 'PENDING'
    $ids.Kategori = $r.Json.guid
    Assert-Db KategoriController POST '/api/kategoriler' 'Create DB' "SELECT BASLIK FROM KATEGORI WHERE GUID='$($ids.Kategori)';" "$prefix`_KATEGORI" | Out-Null
    Invoke-Api KategoriController GET "/api/kategoriler/$($ids.Kategori)" 'Read by id' @(200) $null 'YES' | Out-Null
    Invoke-Api KategoriController GET '/api/kategoriler' List @(200) $null 'YES' | Out-Null
    Invoke-Api KategoriController GET '/api/kategoriler/tree' Tree @(200) $null 'YES' | Out-Null
    Invoke-Api KategoriController PUT "/api/kategoriler/$($ids.Kategori)" Update @(200) @{ baslik="$prefix`_KATEGORI_GUNCEL" } 'PENDING' | Out-Null
    Assert-Db KategoriController PUT '/api/kategoriler/{guid}' 'Update DB' "SELECT BASLIK FROM KATEGORI WHERE GUID='$($ids.Kategori)';" "$prefix`_KATEGORI_GUNCEL" | Out-Null
    Invoke-Api KategoriController POST '/api/kategoriler' 'Validation empty body' @(400) @{} | Out-Null
    Invoke-Api KategoriController GET '/api/kategoriler/CODEX_MISSING' 'Not found' @(404) | Out-Null
    Invoke-Api KategoriController POST '/api/kategoriler' 'Foreign parent not found' @(404) @{baslik="$prefix`_BAD";parentId='CODEX_MISSING'} | Out-Null
    $candidate = Invoke-Api KategoriController POST '/api/kategoriler' 'Delete candidate create' @(201) @{baslik="$prefix`_KATEGORI_DELETE"}
    Invoke-Api KategoriController DELETE "/api/kategoriler/$($candidate.Json.guid)" Delete @(204) $null 'PENDING' | Out-Null
    Assert-Db KategoriController DELETE '/api/kategoriler/{guid}' 'Soft delete DB' "SELECT COUNT(*) FROM KATEGORI WHERE GUID='$($candidate.Json.guid)' AND SILINDI=TRUE;" '1' | Out-Null
    Invoke-Api KategoriController GET "/api/kategoriler/$($candidate.Json.guid)" 'Read after delete' @(404) | Out-Null
    $afterDeleteList = Invoke-Api KategoriController GET '/api/kategoriler' 'List after delete' @(200) $null 'YES'
    Assert-Value KategoriController GET '/api/kategoriler' 'Deleted record excluded from list' (@($afterDeleteList.Json | Where-Object guid -eq $candidate.Json.guid).Count -eq 0) $afterDeleteList.Raw | Out-Null
    Invoke-Api KategoriController DELETE "/api/kategoriler/$($candidate.Json.guid)" 'Second delete' @(404) | Out-Null

    # MarkaController.
    $r = Invoke-Api MarkaController POST '/api/markalar' Create @(201) @{markaAdi="$prefix`_MARKA";aciklama='Codex E2E'} 'PENDING'
    $ids.Marka = $r.Json.guid
    Assert-Db MarkaController POST '/api/markalar' 'Create DB' "SELECT MARKA_ADI FROM MARKA WHERE GUID='$($ids.Marka)';" "$prefix`_MARKA" | Out-Null
    Invoke-Api MarkaController GET '/api/markalar' List @(200) $null 'YES' | Out-Null
    Invoke-Api MarkaController GET "/api/markalar/search?adi=$prefix" Search @(200) $null 'YES' | Out-Null
    Invoke-Api MarkaController PUT "/api/markalar/$($ids.Marka)" Update @(200) @{markaAdi="$prefix`_MARKA_GUNCEL";aciklama='Guncel'} 'PENDING' | Out-Null
    Assert-Db MarkaController PUT '/api/markalar/{guid}' 'Update DB' "SELECT MARKA_ADI FROM MARKA WHERE GUID='$($ids.Marka)';" "$prefix`_MARKA_GUNCEL" | Out-Null
    Invoke-Api MarkaController POST '/api/markalar' 'Validation empty body' @(400) @{} | Out-Null
    Invoke-Api MarkaController PUT '/api/markalar/CODEX_MISSING' 'Update not found' @(404) @{markaAdi='X'} | Out-Null
    $candidate = Invoke-Api MarkaController POST '/api/markalar' 'Delete candidate create' @(201) @{markaAdi="$prefix`_MARKA_DELETE"}
    Invoke-Api MarkaController DELETE "/api/markalar/$($candidate.Json.guid)" Delete @(204) $null 'PENDING' | Out-Null
    Assert-Db MarkaController DELETE '/api/markalar/{guid}' 'Soft delete DB' "SELECT COUNT(*) FROM MARKA WHERE GUID='$($candidate.Json.guid)' AND SILINDI=TRUE;" '1' | Out-Null
    $afterDeleteList = Invoke-Api MarkaController GET '/api/markalar' 'List after delete' @(200) $null 'YES'
    Assert-Value MarkaController GET '/api/markalar' 'Deleted record excluded from list' (@($afterDeleteList.Json | Where-Object guid -eq $candidate.Json.guid).Count -eq 0) $afterDeleteList.Raw | Out-Null
    Invoke-Api MarkaController DELETE "/api/markalar/$($candidate.Json.guid)" 'Second delete' @(404) | Out-Null
    $recreated = Invoke-Api MarkaController POST '/api/markalar' 'Reuse business name after soft delete' @(201) @{markaAdi="$prefix`_MARKA_DELETE"}
    Assert-Db MarkaController POST '/api/markalar' 'Reused name has one active and one deleted row' "SELECT COUNT(*) FROM MARKA WHERE MARKA_ADI='$prefix`_MARKA_DELETE' AND ((SILINDI=TRUE AND GUID='$($candidate.Json.guid)') OR (SILINDI=FALSE AND GUID='$($recreated.Json.guid)'));" '2' | Out-Null

    # ModelController.
    $r = Invoke-Api ModelController POST '/api/modeller' Create @(201) @{modelAdi="$prefix`_MODEL";aciklama='Codex E2E';markaGuid=$ids.Marka;kategoriGuid=$ids.Kategori} 'PENDING'
    $ids.Model = $r.Json.guid
    Assert-Db ModelController POST '/api/modeller' 'Create DB' "SELECT MODEL_ADI FROM MODEL WHERE GUID='$($ids.Model)';" "$prefix`_MODEL" | Out-Null
    Invoke-Api ModelController GET '/api/modeller' List @(200) $null 'YES' | Out-Null
    Invoke-Api ModelController GET "/api/modeller?markaGuid=$($ids.Marka)" 'Filter by marka' @(200) $null 'YES' | Out-Null
    Invoke-Api ModelController GET "/api/modeller/search?markaGuid=$($ids.Marka)&adi=$prefix" Search @(200) $null 'YES' | Out-Null
    Invoke-Api ModelController PUT "/api/modeller/$($ids.Model)" Update @(200) @{modelAdi="$prefix`_MODEL_GUNCEL";aciklama='Guncel';markaGuid=$ids.Marka} 'PENDING' | Out-Null
    Assert-Db ModelController PUT '/api/modeller/{guid}' 'Update DB' "SELECT MODEL_ADI FROM MODEL WHERE GUID='$($ids.Model)';" "$prefix`_MODEL_GUNCEL" | Out-Null
    Invoke-Api ModelController POST '/api/modeller' 'Validation empty body' @(400) @{} | Out-Null
    Invoke-Api ModelController POST '/api/modeller' 'Foreign marka not found' @(404) @{modelAdi='BAD';markaGuid='CODEX_MISSING'} | Out-Null
    $candidate = Invoke-Api ModelController POST '/api/modeller' 'Delete candidate create' @(201) @{modelAdi="$prefix`_MODEL_DELETE";markaGuid=$ids.Marka}
    Invoke-Api ModelController DELETE "/api/modeller/$($candidate.Json.guid)" Delete @(204) $null 'PENDING' | Out-Null
    Assert-Db ModelController DELETE '/api/modeller/{guid}' 'Soft delete DB' "SELECT COUNT(*) FROM MODEL WHERE GUID='$($candidate.Json.guid)' AND SILINDI=TRUE;" '1' | Out-Null
    $afterDeleteList = Invoke-Api ModelController GET '/api/modeller' 'List after delete' @(200) $null 'YES'
    Assert-Value ModelController GET '/api/modeller' 'Deleted record excluded from list' (@($afterDeleteList.Json | Where-Object guid -eq $candidate.Json.guid).Count -eq 0) $afterDeleteList.Raw | Out-Null
    Invoke-Api ModelController DELETE "/api/modeller/$($candidate.Json.guid)" 'Second delete' @(404) | Out-Null

    # FirmaController.
    $firmaBody = @{ad="$prefix`_FIRMA";yetkiliKisi='Codex';durum='AKTIF';kaynakAlani='DEMIRBAS'}
    $r = Invoke-Api FirmaController POST '/api/firmalar' Create @(201) $firmaBody 'PENDING'
    $ids.Firma = $r.Json.guid
    Assert-Db FirmaController POST '/api/firmalar' 'Create DB' "SELECT AD FROM FIRMA WHERE GUID='$($ids.Firma)';" "$prefix`_FIRMA" | Out-Null
    Invoke-Api FirmaController GET "/api/firmalar/$($ids.Firma)" 'Read by id' @(200) $null 'YES' | Out-Null
    Invoke-Api FirmaController GET '/api/firmalar' List @(200) $null 'YES' | Out-Null
    $firmaBody.ad = "$prefix`_FIRMA_GUNCEL"
    Invoke-Api FirmaController PUT "/api/firmalar/$($ids.Firma)" Update @(200) $firmaBody 'PENDING' | Out-Null
    Assert-Db FirmaController PUT '/api/firmalar/{guid}' 'Update DB' "SELECT AD FROM FIRMA WHERE GUID='$($ids.Firma)';" "$prefix`_FIRMA_GUNCEL" | Out-Null
    Invoke-Api FirmaController POST '/api/firmalar' 'Validation empty body' @(400) @{} | Out-Null
    Invoke-Api FirmaController GET '/api/firmalar/CODEX_MISSING' 'Not found' @(404) | Out-Null
    $candidate = Invoke-Api FirmaController POST '/api/firmalar' 'Delete candidate create' @(201) @{ad="$prefix`_FIRMA_DELETE";kaynakAlani='DEMIRBAS'}
    Invoke-Api FirmaController DELETE "/api/firmalar/$($candidate.Json.guid)" Delete @(204) $null 'PENDING' | Out-Null
    Assert-Db FirmaController DELETE '/api/firmalar/{guid}' 'Soft delete DB' "SELECT COUNT(*) FROM FIRMA WHERE GUID='$($candidate.Json.guid)' AND SILINDI=TRUE;" '1' | Out-Null
    Invoke-Api FirmaController GET "/api/firmalar/$($candidate.Json.guid)" 'Read after delete' @(404) | Out-Null
    $afterDeleteList = Invoke-Api FirmaController GET '/api/firmalar' 'List after delete' @(200) $null 'YES'
    Assert-Value FirmaController GET '/api/firmalar' 'Deleted record excluded from list' (@($afterDeleteList.Json | Where-Object guid -eq $candidate.Json.guid).Count -eq 0) $afterDeleteList.Raw | Out-Null
    Invoke-Api FirmaController DELETE "/api/firmalar/$($candidate.Json.guid)" 'Second delete' @(404) | Out-Null

    # IhaleController.
    $ihaleBody = @{firmaGuid=$ids.Firma;ad="$prefix`_IHALE";ihaleTarihi='2026-01-01T10:00:00';durum='AKTIF'}
    $r = Invoke-Api IhaleController POST '/api/ihaleler' Create @(201) $ihaleBody 'PENDING'
    $ids.Ihale = $r.Json.guid
    Assert-Db IhaleController POST '/api/ihaleler' 'Create DB' "SELECT AD FROM IHALE WHERE GUID='$($ids.Ihale)';" "$prefix`_IHALE" | Out-Null
    Invoke-Api IhaleController GET "/api/ihaleler/$($ids.Ihale)" 'Read by id' @(200) $null 'YES' | Out-Null
    Invoke-Api IhaleController GET '/api/ihaleler' List @(200) $null 'YES' | Out-Null
    $ihaleBody.ad = "$prefix`_IHALE_GUNCEL"
    Invoke-Api IhaleController PUT "/api/ihaleler/$($ids.Ihale)" Update @(200) $ihaleBody 'PENDING' | Out-Null
    Assert-Db IhaleController PUT '/api/ihaleler/{guid}' 'Update DB' "SELECT AD FROM IHALE WHERE GUID='$($ids.Ihale)';" "$prefix`_IHALE_GUNCEL" | Out-Null
    Invoke-Api IhaleController POST '/api/ihaleler' 'Validation empty body' @(400) @{} | Out-Null
    Invoke-Api IhaleController POST '/api/ihaleler' 'Foreign firma not found' @(404) @{firmaGuid='CODEX_MISSING';ad='BAD'} | Out-Null
    $candidate = Invoke-Api IhaleController POST '/api/ihaleler' 'Delete candidate create' @(201) @{firmaGuid=$ids.Firma;ad="$prefix`_IHALE_DELETE"}
    Invoke-Api IhaleController DELETE "/api/ihaleler/$($candidate.Json.guid)" Delete @(204) $null 'PENDING' | Out-Null
    Assert-Db IhaleController DELETE '/api/ihaleler/{guid}' 'Soft delete DB' "SELECT COUNT(*) FROM IHALE WHERE GUID='$($candidate.Json.guid)' AND SILINDI=TRUE;" '1' | Out-Null
    Invoke-Api IhaleController GET "/api/ihaleler/$($candidate.Json.guid)" 'Read after delete' @(404) | Out-Null
    $afterDeleteList = Invoke-Api IhaleController GET '/api/ihaleler' 'List after delete' @(200) $null 'YES'
    Assert-Value IhaleController GET '/api/ihaleler' 'Deleted record excluded from list' (@($afterDeleteList.Json | Where-Object guid -eq $candidate.Json.guid).Count -eq 0) $afterDeleteList.Raw | Out-Null
    Invoke-Api IhaleController DELETE "/api/ihaleler/$($candidate.Json.guid)" 'Second delete' @(404) | Out-Null

    # DepoController, two dependency records and one delete candidate.
    foreach ($label in @('A','B')) {
        $r = Invoke-Api DepoController POST '/api/depolar' "Create depot $label" @(201) @{ad="$prefix`_DEPO_$label";aciklama='Codex E2E';durum='AKTIF'} 'PENDING'
        $ids["Depo$label"] = $r.Json.guid
        Assert-Db DepoController POST '/api/depolar' "Create depot $label DB" "SELECT AD FROM DEPO WHERE GUID='$($r.Json.guid)';" "$prefix`_DEPO_$label" | Out-Null
    }
    Invoke-Api DepoController GET "/api/depolar/$($ids.DepoA)" 'Read by id' @(200) $null 'YES' | Out-Null
    Invoke-Api DepoController GET '/api/depolar' List @(200) $null 'YES' | Out-Null
    Invoke-Api DepoController PUT "/api/depolar/$($ids.DepoA)" Update @(200) @{ad="$prefix`_DEPO_A_GUNCEL";aciklama='Guncel';durum='AKTIF'} 'PENDING' | Out-Null
    Assert-Db DepoController PUT '/api/depolar/{guid}' 'Update DB' "SELECT AD FROM DEPO WHERE GUID='$($ids.DepoA)';" "$prefix`_DEPO_A_GUNCEL" | Out-Null
    Invoke-Api DepoController POST '/api/depolar' 'Validation empty body' @(400) @{} | Out-Null
    Invoke-Api DepoController GET '/api/depolar/CODEX_MISSING' 'Not found' @(404) | Out-Null
    $candidate = Invoke-Api DepoController POST '/api/depolar' 'Delete candidate create' @(201) @{ad="$prefix`_DEPO_DELETE"}
    Invoke-Api DepoController DELETE "/api/depolar/$($candidate.Json.guid)" Delete @(204) $null 'PENDING' | Out-Null
    Assert-Db DepoController DELETE '/api/depolar/{guid}' 'Soft delete DB' "SELECT COUNT(*) FROM DEPO WHERE GUID='$($candidate.Json.guid)' AND SILINDI=TRUE;" '1' | Out-Null
    Invoke-Api DepoController GET "/api/depolar/$($candidate.Json.guid)" 'Read after delete' @(404) | Out-Null
    $afterDeleteList = Invoke-Api DepoController GET '/api/depolar' 'List after delete' @(200) $null 'YES'
    Assert-Value DepoController GET '/api/depolar' 'Deleted record excluded from list' (@($afterDeleteList.Json | Where-Object guid -eq $candidate.Json.guid).Count -eq 0) $afterDeleteList.Raw | Out-Null
    Invoke-Api DepoController DELETE "/api/depolar/$($candidate.Json.guid)" 'Second delete' @(404) | Out-Null

    # EnvanterController: exercise every route through both aliases.
    $env1Body = @{envanterAdi="$prefix`_DEMIRBAS";envanterTuru='DEMIRBAS';depoGuid=$ids.DepoA;modelGuid=$ids.Model;demirbasNumarasi=900001;seriNo="$prefix`_SERI";barkod="$prefix`_BARKOD";sube=1;aciklama='Codex E2E';ilSubeDaireBaskanlikNumarasi='34';detay=@{alimYolu=1;alimTuru='IHALE';saymanlikNo='CODEX';ihaleGuid=$ids.Ihale;firmaGuid=$ids.Firma;garantiBaslangicTarihi='2026-01-01T00:00:00';garantiBitisTarihi='2027-01-01T00:00:00'}}
    $r = Invoke-Api EnvanterController POST '/api/envanter' 'Create via singular alias' @(201) $env1Body 'PENDING'
    $ids.Envanter1 = $r.Json.guid
    Assert-Db EnvanterController POST '/api/envanter' 'Create DB + initial movement/adet' "SELECT COUNT(*) FROM ENVANTER e JOIN ENVANTER_DETAY d ON d.ENVANTER_GUID=e.GUID JOIN DEPO_ZIMMET_HAREKET h ON h.ENVANTER_GUID=e.GUID WHERE e.GUID='$($ids.Envanter1)' AND h.ADET=1;" '1' | Out-Null
    $env2Body = @{envanterAdi="$prefix`_SARF";envanterTuru='SARF';depoGuid=$ids.DepoA;modelGuid=$ids.Model;barkod="$prefix`_SARF_BARKOD";aciklama='Codex E2E'}
    $r = Invoke-Api EnvanterController POST '/api/envanterler' 'Create via plural alias' @(201) $env2Body 'PENDING'
    $ids.Envanter2 = $r.Json.guid
    Assert-Db EnvanterController POST '/api/envanterler' 'Create DB + initial movement/adet' "SELECT COUNT(*) FROM ENVANTER e JOIN DEPO_ZIMMET_HAREKET h ON h.ENVANTER_GUID=e.GUID WHERE e.GUID='$($ids.Envanter2)' AND h.ADET=1;" '1' | Out-Null
    foreach ($base in @('/api/envanter','/api/envanterler')) {
        Invoke-Api EnvanterController GET $base "List $base" @(200) $null 'YES' | Out-Null
        Invoke-Api EnvanterController GET "$base/sarf" "Sarf list $base" @(200) $null 'YES' | Out-Null
        Invoke-Api EnvanterController GET "$base/demirbas" "Demirbas list $base" @(200) $null 'YES' | Out-Null
        Invoke-Api EnvanterController GET "$base/$($ids.Envanter1)" "Read by id $base" @(200) $null 'YES' | Out-Null
        Invoke-Api EnvanterController GET "$base/$($ids.Envanter1)/zimmet-durumu" "Zimmet status $base" @(200) $null 'YES' | Out-Null
    }
    $env1Update = @{envanterAdi="$prefix`_DEMIRBAS_GUNCEL";envanterTuru='DEMIRBAS';modelGuid=$ids.Model;demirbasNumarasi=900002;seriNo='GUNCEL';barkod='GUNCEL';sube=2;aciklama='Guncel';ilSubeDaireBaskanlikNumarasi='06'}
    Invoke-Api EnvanterController PUT "/api/envanter/$($ids.Envanter1)" 'Update singular alias' @(200) $env1Update 'PENDING' | Out-Null
    Assert-Db EnvanterController PUT '/api/envanter/{guid}' 'Update DB' "SELECT ENVANTER_ADI FROM ENVANTER WHERE GUID='$($ids.Envanter1)';" "$prefix`_DEMIRBAS_GUNCEL" | Out-Null
    $env2Update = @{envanterAdi="$prefix`_SARF_GUNCEL";envanterTuru='SARF';modelGuid=$ids.Model;barkod='SARF_GUNCEL';aciklama='Guncel'}
    Invoke-Api EnvanterController PUT "/api/envanterler/$($ids.Envanter2)" 'Update plural alias' @(200) $env2Update 'PENDING' | Out-Null
    Assert-Db EnvanterController PUT '/api/envanterler/{guid}' 'Update DB' "SELECT ENVANTER_ADI FROM ENVANTER WHERE GUID='$($ids.Envanter2)';" "$prefix`_SARF_GUNCEL" | Out-Null
    Invoke-Api EnvanterController POST '/api/envanter' 'Validation empty body' @(400) @{} | Out-Null
    Invoke-Api EnvanterController POST '/api/envanterler' 'Foreign depo not found' @(404) @{envanterAdi='BAD';envanterTuru='DEMIRBAS';depoGuid='CODEX_MISSING'} | Out-Null
    Invoke-Api EnvanterController GET '/api/envanter/CODEX_MISSING' 'Not found singular' @(404) | Out-Null
    Invoke-Api EnvanterController GET '/api/envanterler/CODEX_MISSING' 'Not found plural' @(404) | Out-Null
    Invoke-Api EnvanterController DELETE "/api/envanter/$($ids.Envanter1)" 'Delete blocked by audit history' @(422) $null 'YES' | Out-Null
    Invoke-Api EnvanterController DELETE "/api/envanterler/$($ids.Envanter2)" 'Delete blocked by audit history' @(422) $null 'YES' | Out-Null

    $deleteBody = @{envanterAdi="$prefix`_ENVANTER_DELETE";envanterTuru='SARF';depoGuid=$ids.DepoA;modelGuid=$ids.Model;aciklama='Soft delete E2E'}
    $deleteCandidate = Invoke-Api EnvanterController POST '/api/envanter' 'Soft delete candidate create' @(201) $deleteBody 'PENDING'
    $ids.EnvanterDelete = $deleteCandidate.Json.guid
    Invoke-Sql @"
DELETE FROM DEPO_ZIMMET_HAREKET WHERE ENVANTER_GUID='$($ids.EnvanterDelete)';
COMMIT;
"@ | Out-Null
    Invoke-Api EnvanterController DELETE "/api/envanter/$($ids.EnvanterDelete)" 'Soft delete without audit history' @(204) $null 'PENDING' | Out-Null
    Assert-Db EnvanterController DELETE '/api/envanter/{guid}' 'Soft delete DB + detail preserved' "SELECT COUNT(*) FROM ENVANTER e JOIN ENVANTER_DETAY d ON d.ENVANTER_GUID=e.GUID WHERE e.GUID='$($ids.EnvanterDelete)' AND e.SILINDI=TRUE AND d.SILINDI=FALSE;" '1' | Out-Null
    Invoke-Api EnvanterController GET "/api/envanter/$($ids.EnvanterDelete)" 'Read after soft delete singular' @(404) | Out-Null
    Invoke-Api EnvanterController GET "/api/envanterler/$($ids.EnvanterDelete)" 'Read after soft delete plural' @(404) | Out-Null
    foreach ($base in @('/api/envanter','/api/envanterler')) {
        $afterDeleteList = Invoke-Api EnvanterController GET $base "List after soft delete $base" @(200) $null 'YES'
        Assert-Value EnvanterController GET $base 'Deleted record excluded from list' (@($afterDeleteList.Json | Where-Object guid -eq $ids.EnvanterDelete).Count -eq 0) $afterDeleteList.Raw | Out-Null
    }
    Invoke-Api EnvanterController DELETE "/api/envanterler/$($ids.EnvanterDelete)" 'Second delete' @(404) | Out-Null

    # AparatController.
    $r = Invoke-Api AparatController POST '/api/aparatlar' Create @(201) @{envanterGuid=$ids.Envanter1;aparatAdi="$prefix`_APARAT";aciklama='Codex E2E'} 'PENDING'
    $ids.Aparat = $r.Json.guid
    Assert-Db AparatController POST '/api/aparatlar' 'Create DB' "SELECT APARAT_ADI FROM APARAT WHERE GUID='$($ids.Aparat)';" "$prefix`_APARAT" | Out-Null
    Invoke-Api AparatController GET "/api/aparatlar/$($ids.Aparat)" 'Read by id' @(200) $null 'YES' | Out-Null
    Invoke-Api AparatController GET '/api/aparatlar' List @(200) $null 'YES' | Out-Null
    Invoke-Api AparatController POST '/api/aparatlar' 'Validation empty body' @(400) @{} | Out-Null
    Invoke-Api AparatController POST '/api/aparatlar' 'Foreign envanter not found' @(404) @{envanterGuid='CODEX_MISSING';aparatAdi='BAD'} | Out-Null
    Invoke-Api AparatController DELETE "/api/aparatlar/$($ids.Aparat)" Delete @(204) $null 'PENDING' | Out-Null
    Assert-Db AparatController DELETE '/api/aparatlar/{guid}' 'Soft delete DB' "SELECT COUNT(*) FROM APARAT WHERE GUID='$($ids.Aparat)' AND SILINDI=TRUE;" '1' | Out-Null
    Invoke-Api AparatController GET "/api/aparatlar/$($ids.Aparat)" 'Read after delete' @(404) | Out-Null
    $afterDeleteList = Invoke-Api AparatController GET '/api/aparatlar' 'List after delete' @(200) $null 'YES'
    Assert-Value AparatController GET '/api/aparatlar' 'Deleted record excluded from list' (@($afterDeleteList.Json | Where-Object guid -eq $ids.Aparat).Count -eq 0) $afterDeleteList.Raw | Out-Null
    Invoke-Api AparatController DELETE "/api/aparatlar/$($ids.Aparat)" 'Second delete' @(404) | Out-Null
    $ids.Aparat = $null

    # ZimmetController business flow on Envanter1.
    Invoke-Api ZimmetController GET "/api/zimmet/envanter/$($ids.Envanter1)/personel-hareketleri" 'Initial personel history' @(200) $null 'YES' | Out-Null
    $initialDepotHistory = Invoke-Api ZimmetController GET "/api/zimmet/envanter/$($ids.Envanter1)/depo-hareketleri" 'Initial depot history' @(200) $null 'YES'
    Assert-Value ZimmetController GET '/api/zimmet/envanter/{guid}/depo-hareketleri' 'Initial movement response adet' (@($initialDepotHistory.Json)[0].adet -eq 1) $initialDepotHistory.Raw | Out-Null
    $transfer = Invoke-Api ZimmetController POST '/api/zimmet/depo-transferi' 'Depot transfer' @(201) @{envanterGuid=$ids.Envanter1;gidecegiDepoGuid=$ids.DepoB;aciklama='CODEX transfer';islemYapanPersonel='CODEX'} 'PENDING'
    Assert-Value ZimmetController POST '/api/zimmet/depo-transferi' 'Transfer response adet' ($transfer.Json.adet -eq 1) $transfer.Raw | Out-Null
    Assert-Db ZimmetController POST '/api/zimmet/depo-transferi' 'Transfer DB/current depot/adet' "SELECT COUNT(*) FROM ENVANTER e JOIN DEPO_ZIMMET_HAREKET h ON h.ENVANTER_GUID=e.GUID WHERE e.GUID='$($ids.Envanter1)' AND e.DEPO_GUID='$($ids.DepoB)' AND h.HAREKET_TIPI='DEPO_TRANSFERI' AND h.ADET=1;" '2' | Out-Null
    Invoke-Api ZimmetController POST '/api/zimmet/depo-transferi' 'Same depot rejected' @(422) @{envanterGuid=$ids.Envanter1;gidecegiDepoGuid=$ids.DepoB} | Out-Null
    $z = Invoke-Api ZimmetController POST '/api/zimmet/personel' 'Assign personel' @(201) @{envanterGuid=$ids.Envanter1;personelKodu="$prefix`_PERSONEL_A";aciklama='CODEX zimmet';islemYapanPersonel='CODEX'} 'PENDING'
    Assert-Db ZimmetController POST '/api/zimmet/personel' 'Personel assignment DB' "SELECT COUNT(*) FROM PERSONEL_ZIMMET_HAREKET p JOIN ENVANTER e ON e.GUID=p.ENVANTER_GUID WHERE p.ID=$($z.Json.id) AND p.AKTIF=TRUE AND e.DEPO_GUID IS NULL;" '1' | Out-Null
    Invoke-Api ZimmetController POST '/api/zimmet/personel' 'Duplicate active assignment' @(422) @{envanterGuid=$ids.Envanter1;personelKodu='OTHER'} | Out-Null
    Invoke-Api ZimmetController GET "/api/zimmet/envanter/$($ids.Envanter1)/personel-hareketleri" 'Personel history' @(200) $null 'YES' | Out-Null
    Invoke-Api ZimmetController POST '/api/zimmet/iade' Return @(201) @{envanterGuid=$ids.Envanter1;depoGuid=$ids.DepoA;aciklama='CODEX iade';islemYapanPersonel='CODEX'} 'PENDING' | Out-Null
    Assert-Db ZimmetController POST '/api/zimmet/iade' 'Return DB/current depot/adet' "SELECT COUNT(*) FROM DEPO_ZIMMET_HAREKET h JOIN ENVANTER e ON e.GUID=h.ENVANTER_GUID WHERE e.GUID='$($ids.Envanter1)' AND e.DEPO_GUID='$($ids.DepoA)' AND h.HAREKET_TIPI='IADE' AND h.ADET=1;" '1' | Out-Null
    $depotHistory = Invoke-Api ZimmetController GET "/api/zimmet/envanter/$($ids.Envanter1)/depo-hareketleri" 'Depot history after movements' @(200) $null 'YES'
    Assert-Value ZimmetController GET '/api/zimmet/envanter/{guid}/depo-hareketleri' 'Movement history response adet' (@($depotHistory.Json | Where-Object { $_.adet -eq 1 }).Count -ge 3) $depotHistory.Raw | Out-Null
    Invoke-Api ZimmetController POST '/api/zimmet/personel' 'Validation empty body' @(400) @{} | Out-Null
    Invoke-Api ZimmetController GET '/api/zimmet/envanter/CODEX_MISSING/depo-hareketleri' 'History not found' @(404) | Out-Null

    # PersonelZimmetController business flow on Envanter2.
    $person = "$prefix`_PERSONEL_B"
    Invoke-Api PersonelZimmetController GET "/api/personeller/$person/envanterler" 'Initial active inventory' @(200) $null 'YES' | Out-Null
    Invoke-Api PersonelZimmetController GET "/api/personeller/$person/zimmet-gecmisi" 'Initial history' @(200) $null 'YES' | Out-Null
    $p = Invoke-Api PersonelZimmetController POST "/api/personeller/$person/zimmetler" Assign @(201) @{envanterId=$ids.Envanter2;aciklama='CODEX personel API';islemYapanPersonel='CODEX'} 'PENDING'
    $ids.PersonelHareket = $p.Json.hareketId
    Assert-Value PersonelZimmetController POST '/api/personeller/{personelKodu}/zimmetler' 'Response marka/model consistency' ($p.Json.marka -eq "$prefix`_MARKA_GUNCEL" -and $p.Json.model -eq "$prefix`_MODEL_GUNCEL") "marka=$($p.Json.marka) model=$($p.Json.model)" | Out-Null
    Assert-Db PersonelZimmetController POST '/api/personeller/{personelKodu}/zimmetler' 'Assignment DB' "SELECT COUNT(*) FROM PERSONEL_ZIMMET_HAREKET WHERE ID=$($ids.PersonelHareket) AND PERSONEL_KODU='$person' AND AKTIF=TRUE;" '1' | Out-Null
    Invoke-Api PersonelZimmetController GET "/api/personeller/$person/envanterler" 'Active inventory after assign' @(200) $null 'YES' | Out-Null
    Invoke-Api PersonelZimmetController GET "/api/personeller/$person/zimmet-gecmisi" 'History after assign' @(200) $null 'YES' | Out-Null
    Invoke-Api PersonelZimmetController GET "/api/personeller/$person/zimmet-gecmisi/$($ids.PersonelHareket)" 'History by movement id' @(200) $null 'YES' | Out-Null
    $duplicate = Invoke-Api PersonelZimmetController POST "/api/personeller/$person/zimmetler" 'Duplicate active assignment' @(422) @{envanterId=$ids.Envanter2}
    Assert-Value PersonelZimmetController POST '/api/personeller/{personelKodu}/zimmetler' 'Duplicate error contract' ($duplicate.Json.message -eq 'Envanter zaten aktif olarak bir personele zimmetli') $duplicate.Raw | Out-Null
    Invoke-Api PersonelZimmetController POST "/api/personeller/$person/zimmetler/$($ids.Envanter2)/iade" 'Return validation: depoGuid required' @(422) @{} | Out-Null
    Invoke-Api PersonelZimmetController POST "/api/personeller/$person/zimmetler/$($ids.Envanter2)/iade" Return @(201) @{depoGuid=$ids.DepoB;aciklama='CODEX personel iade';islemYapanPersonel='CODEX'} 'PENDING' | Out-Null
    Assert-Db PersonelZimmetController POST '/api/personeller/{personelKodu}/zimmetler/{envanterId}/iade' 'Return DB + depot movement adet' "SELECT COUNT(*) FROM DEPO_ZIMMET_HAREKET h JOIN ENVANTER e ON e.GUID=h.ENVANTER_GUID WHERE e.GUID='$($ids.Envanter2)' AND e.DEPO_GUID='$($ids.DepoB)' AND h.HAREKET_TIPI='IADE' AND h.ADET=1;" '1' | Out-Null
    Invoke-Api PersonelZimmetController GET "/api/personeller/$person/envanterler" 'No active inventory after return' @(200) $null 'YES' | Out-Null
    Invoke-Api PersonelZimmetController GET "/api/personeller/$person/zimmet-gecmisi" 'History after return' @(200) $null 'YES' | Out-Null
    Invoke-Api PersonelZimmetController GET "/api/personeller/$person/zimmet-gecmisi/999999999" 'Movement not found' @(404) | Out-Null
    Invoke-Api PersonelZimmetController POST "/api/personeller/$person/zimmetler" 'Validation empty body' @(400) @{} | Out-Null

} finally {
    try { Cleanup-TestData } catch {
        Add-Result Cleanup SQL '-' Cleanup '-' '-' 'NO' $false $_.Exception.Message
    }
    $leftovers = Invoke-Sql "SELECT (SELECT COUNT(*) FROM KATEGORI WHERE BASLIK LIKE '$prefix%') + (SELECT COUNT(*) FROM MARKA WHERE MARKA_ADI LIKE '$prefix%') + (SELECT COUNT(*) FROM MODEL WHERE MODEL_ADI LIKE '$prefix%') + (SELECT COUNT(*) FROM FIRMA WHERE AD LIKE '$prefix%') + (SELECT COUNT(*) FROM DEPO WHERE AD LIKE '$prefix%') + (SELECT COUNT(*) FROM IHALE WHERE AD LIKE '$prefix%') + (SELECT COUNT(*) FROM ENVANTER WHERE ENVANTER_ADI LIKE '$prefix%') FROM DUAL;"
    Add-Result Cleanup SQL '-' 'CODEX test data cleanup' '-' '-' 'YES' ($leftovers -eq '0') "leftovers=$leftovers"
}

$jsonResults = $results | ConvertTo-Json -Depth 10
$resultFile = [System.IO.Path]::GetFullPath((Join-Path (Get-Location) $ResultsPath))
[System.IO.Directory]::CreateDirectory([System.IO.Path]::GetDirectoryName($resultFile)) | Out-Null
[System.IO.File]::WriteAllText($resultFile, $jsonResults, [System.Text.UTF8Encoding]::new($false))
$failed = @($results | Where-Object Result -eq 'FAIL').Count
Write-Output "SUMMARY total=$($results.Count) pass=$($results.Count-$failed) fail=$failed prefix=$prefix results=$resultFile"
if ($failed -gt 0) { exit 2 }
