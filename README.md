# SharingApp · Contacts & Borrowed (Android/Java)

Aplicación académica para CRUD de contactos y préstamo de objetos con prestatario, persistencia JSON/SharedPreferences y listas por disponibilidad.

Demo original del autor: https://youtube.com/shorts/_tD5tgR5iwA?feature=share

## Compilación verificada en Windows

La configuración actual utiliza Android SDK 26, minSdk 19, targetSdk 26, Android Support 26.1.0, Android Gradle Plugin 4.0.2 y Gradle 6.1.1. Se verificó con **JDK 11**; el Gradle antiguo falló al ejecutarse con JDK21. No basta con indicar "Android Studio API30+".

Con las dependencias ya descargadas desde Google/Maven Central:

```powershell
$env:JAVA_HOME = 'ruta\al\jdk-11'
$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
.\gradlew.bat --offline --no-daemon :app:testDebugUnitTest :app:assembleDebug
```

APK debug: `app/build/outputs/apk/debug/app-debug.apk`. Compilación y seis pruebas JUnit pasan (cinco regresiones reales y el ejemplo original de suma). La interfaz Android y las pruebas instrumentadas en dispositivo no se han verificado en esta revisión.

## Comprobaciones del modelo sin Gradle

```powershell
.\tests-offline\Test.ps1 -JavaHome 'ruta\al\jdk' -AndroidSdk "$env:LOCALAPPDATA\Android\Sdk"
```

Doce checks del modelo con Java y el jar SDK26 comprueban trim, duplicados, nombres vacíos, identidad/hash bajo locale turco y colecciones hash. No acceden a contactos del dispositivo.

## Correcciones verificadas

- Un nuevo `ItemList` ya no borra los objetos compartidos entre actividades; copiar una lista de entrada protege de cambios del llamador.
- Contactos iguales sin distinguir mayúsculas conservan el contrato de hash incluso con locale turco y Unicode.
- Espacios en username ya no evitan la detección de duplicados; crear/editar email usa la validación de Android.
- Dependencias pasan por Maven Central/Google; Gson 2.10.1 reemplaza el SNAPSHOT local en la compilación y JUnit usa 4.13.2. Se conserva el jar original en el árbol y el respaldo; no es una dependencia activa.

La compatibilidad Java de [Gson 2.10.1 está documentada por su autor](https://github.com/google/gson/blob/gson-parent-2.10.1/README.md). La prueba de roundtrip comprueba item/prestatario/identidad con esta dependencia.

## Pendientes

Migración a AndroidX/AGP/targetSDK actuales, tests instrumentados y accesibilidad, recuperación de archivos JSON corruptos, guardado atómico y manejo de permisos/fotos. `allowBackup=true` requiere revisar la política de respaldo de contactos antes de distribuir datos reales. La revisión no instaló el APK ni accedió a dispositivos, fotos o contactos personales.
