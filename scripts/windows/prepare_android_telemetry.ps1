param(
    [int]$Port = 5555
)

$ErrorActionPreference = "Stop"

Write-Host "Checking whether Windows can reach the WSL2 telemetry server on localhost:$Port..."
$localCheck = Test-NetConnection -ComputerName 127.0.0.1 -Port $Port -WarningAction SilentlyContinue

if (-not $localCheck.TcpTestSucceeded) {
    $wslIpRaw = (& wsl.exe hostname -I 2>$null)
    $wslIp = ($wslIpRaw -split '\s+' | Where-Object { $_ } | Select-Object -First 1)

    if ($wslIp) {
        Write-Host "Windows localhost:$Port is not reachable. WSL2 currently reports IP $wslIp."
        $wslCheck = Test-NetConnection -ComputerName $wslIp -Port $Port -WarningAction SilentlyContinue
        if ($wslCheck.TcpTestSucceeded) {
            Write-Host "The simulator is reachable at ${wslIp}:$Port, but Windows localhost forwarding is not working."
            Write-Host "One fallback is an elevated PowerShell port proxy:"
            Write-Host "  netsh interface portproxy add v4tov4 listenaddress=127.0.0.1 listenport=$Port connectaddress=$wslIp connectport=$Port"
            Write-Host "Then rerun this script. WSL2 NAT IPs can change after a WSL restart."
        } else {
            Write-Host "The simulator is not reachable on localhost or on the current WSL2 IP."
            Write-Host "Confirm ./scripts/run_simulator.sh --tcp-only is still running and listening on 0.0.0.0:$Port."
        }
    } else {
        Write-Host "Could not determine the WSL2 IP. Confirm WSL is running and the simulator is active."
    }
    exit 1
}

$adbCommand = Get-Command adb -ErrorAction SilentlyContinue
$adbPath = if ($adbCommand) {
    $adbCommand.Source
} else {
    $candidates = @(
        $(if ($env:ANDROID_HOME) { Join-Path $env:ANDROID_HOME "platform-tools\adb.exe" }),
        $(if ($env:ANDROID_SDK_ROOT) { Join-Path $env:ANDROID_SDK_ROOT "platform-tools\adb.exe" }),
        $(if ($env:LOCALAPPDATA) { Join-Path $env:LOCALAPPDATA "Android\Sdk\platform-tools\adb.exe" })
    ) | Where-Object { $_ -and (Test-Path $_) }
    $candidates | Select-Object -First 1
}

if (-not $adbPath) {
    Write-Host "adb was not found. Install Android SDK Platform-Tools or add its platform-tools directory to PATH."
    exit 1
}

Write-Host "Using adb: $adbPath"
$deviceLines = (& $adbPath devices) | Select-Object -Skip 1 | Where-Object { $_ -match '\tdevice$' }
if (-not $deviceLines) {
    Write-Host "No booted Android emulator/device is visible to adb. Start the Automotive AVD and rerun this script."
    exit 1
}

& $adbPath reverse "tcp:$Port" "tcp:$Port"
if ($LASTEXITCODE -ne 0) {
    Write-Host "adb reverse failed. Check 'adb devices' and make sure the emulator is fully booted."
    exit $LASTEXITCODE
}

Write-Host "Ready: Android 127.0.0.1:$Port -> adb reverse -> Windows localhost:$Port -> WSL2 telemetry server."
Write-Host "The app now defaults to 127.0.0.1:$Port for this development workflow."
