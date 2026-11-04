Add-Type -AssemblyName System.Drawing
$arenaSaida = $PSScriptRoot
$arenaItens = Get-Content (Join-Path $arenaSaida 'fontes.json') -Raw -Encoding UTF8 | ConvertFrom-Json
$arenaRelatorio = foreach ($arenaItem in $arenaItens) {
    $arenaOriginal = [System.Drawing.Image]::FromFile($arenaItem.source)
    $arenaBitmap = New-Object System.Drawing.Bitmap ([int]$arenaItem.width), ([int]$arenaItem.height), ([System.Drawing.Imaging.PixelFormat]::Format24bppRgb)
    $arenaGrafico = [System.Drawing.Graphics]::FromImage($arenaBitmap)
    try {
        $arenaGrafico.Clear([System.Drawing.ColorTranslator]::FromHtml('#031426'))
        $arenaGrafico.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
        $arenaGrafico.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
        $arenaEscala = [Math]::Min($arenaItem.width / $arenaOriginal.Width, $arenaItem.height / $arenaOriginal.Height)
        $arenaLargura = [int][Math]::Round($arenaOriginal.Width * $arenaEscala)
        $arenaAltura = [int][Math]::Round($arenaOriginal.Height * $arenaEscala)
        $arenaX = [int][Math]::Floor(($arenaItem.width - $arenaLargura) / 2)
        $arenaY = [int][Math]::Floor(($arenaItem.height - $arenaAltura) / 2)
        $arenaGrafico.DrawImage($arenaOriginal, $arenaX, $arenaY, $arenaLargura, $arenaAltura)
        $arenaDestino = Join-Path $arenaSaida $arenaItem.file
        $arenaBitmap.Save($arenaDestino, [System.Drawing.Imaging.ImageFormat]::Png)
    } finally {
        $arenaGrafico.Dispose()
        $arenaBitmap.Dispose()
        $arenaOriginal.Dispose()
    }
    $arenaVerificada = [System.Drawing.Image]::FromFile($arenaDestino)
    try {
        $arenaArquivo = Get-Item -LiteralPath $arenaDestino
        $arenaLimite = if ($arenaItem.width -eq 1024) { 15MB } else { 8MB }
        if ($arenaVerificada.Width -ne $arenaItem.width -or $arenaVerificada.Height -ne $arenaItem.height -or $arenaArquivo.Length -gt $arenaLimite) { throw "Arquivo fora das especificacoes: $($arenaItem.file)" }
        [pscustomobject]@{Arquivo=$arenaItem.file;Largura=$arenaVerificada.Width;Altura=$arenaVerificada.Height;Bytes=$arenaArquivo.Length;MB=[Math]::Round($arenaArquivo.Length/1MB,2);Formato='PNG RGB';Validado=$true}
    } finally { $arenaVerificada.Dispose() }
}
$arenaRelatorio | ConvertTo-Json | Set-Content (Join-Path $arenaSaida 'validacao.json') -Encoding UTF8
$arenaRelatorio | Format-Table -AutoSize
Compress-Archive -LiteralPath ($arenaItens | ForEach-Object { Join-Path $arenaSaida $_.file }) -DestinationPath (Join-Path $arenaSaida 'arena-match-play-store.zip') -Force
