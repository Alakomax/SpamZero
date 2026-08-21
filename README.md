# SpamZero - Filtro de Llamadas y SMS Spam para LATAM (Android Nativo)

Aplicación móvil nativa en Kotlin para Android (API 29+) diseñada para interceptar y rechazar llamadas entrantes y SMS de spam telefónico por patrones Regex antes del primer timbre, archivando los eventos en un historial de cuarentena local.

## Características

* ⚡ **Silenciamiento e Interceptación a 0 Repiques:** Utiliza `CallScreeningService` con `setSilenceCall(true)` para cortar la llamada en milisegundos sin sonar la ringer ni encender la pantalla.
* 🌐 **Soporte Multi-País LATAM:** Cobertura para Chile 🇨🇱, Colombia 🇨🇴, Argentina 🇦🇷, Venezuela 🇻🇪, México 🇲🇽, Perú 🇵🇪 y reglas globales.
* 📲 **Bandera de SIM en Pantalla:** Indicación visual del país activo en la barra superior según la tarjeta SIM instalada.
* 📩 **Filtro de SMS Spam y Phishing:** Interceptación de SMS de casinos (`[BET7K]`, `Fortune Ox`, `7K`), falsas multas y cobros urgentes.
* 📋 **Base de Datos de Cuarentena (Room):** Historial completo con fecha, hora, número, mensaje y patrón coincidente.
* ⚙️ **Administrador de Reglas Regex:** Permite agregar o desactivar rangos de números en tiempo real.
* 🔒 **Lista Blanca Automática:** Las llamadas y SMS de contactos guardados en la agenda del teléfono nunca son bloqueados.
* 🎨 **Interfaz Moderna en Jetpack Compose:** Material Design 3 con soporte para tema claro/oscuro.

## Requisitos y Compilación

* Android Studio Hedgehog (2023.1.1) o posterior.
* JDK 17+
* Gradle 8.2+

### Compilación desde CLI:
```bash
./gradlew assembleDebug
```
El archivo APK resultante estará ubicado en `app/build/outputs/apk/debug/app-debug.apk`.
