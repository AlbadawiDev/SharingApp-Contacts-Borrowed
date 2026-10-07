param(
    [string]$JavaHome = $env:JAVA_HOME,
    [string]$AndroidSdk = "$env:LOCALAPPDATA\Android\Sdk"
)
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$javac = Join-Path $JavaHome 'bin\javac.exe'
$java = Join-Path $JavaHome 'bin\java.exe'
$androidJar = Join-Path $AndroidSdk 'platforms\android-26\android.jar'
if (!(Test-Path -LiteralPath $javac) -or !(Test-Path -LiteralPath $androidJar)) {
    throw 'Provide JavaHome with a JDK and AndroidSdk with platform android-26.'
}
$output = Join-Path $root 'build\offline-model-checks'
New-Item -ItemType Directory -Force -Path $output | Out-Null
$models = Join-Path $root 'app\src\main\java\com\example\sharingapp'
& $javac -encoding UTF-8 -cp $androidJar -d $output "$models\Contact.java" "$models\ContactList.java" "$PSScriptRoot\ContactChecks.java"
if ($LASTEXITCODE -ne 0) { throw 'Model compilation failed.' }
& $java -cp "$output;$androidJar" ContactChecks
if ($LASTEXITCODE -ne 0) { throw 'Model checks failed.' }
