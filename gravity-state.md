## [ESTADO ACTUAL] Última actualización: 2026-08-29 | Dominio: SpamZero Web Landing Page UI/UX & Android App v1.1.4 (LATAM)
- Objetivo Activo: Solución del error "No se instaló la app" debido a APK no firmada (ruta relativa en local.properties corregida) y entrega de APK firmada oficial v1.1.4.
- Última Acción: Corregida ruta de KEYSTORE_FILE en local.properties. Recompilado paquete Release firmado (`:app:signingConfigWriterRelease` ejecutado, `BUILD SUCCESSFUL in 1m 27s`).
- Decisiones/Bloqueos: Ninguno. APK firmada en `app/build/outputs/apk/release/app-release.apk`.
- Vínculo Marketing: `d:\Suma_Proyectos\Proyectos\.marketing\SpamQuarantine` (Espiras RRSS y Guiones).
- Siguiente Paso: Commit y Push a repositorio remoto.

## Historial Reciente
- 2026-08-29: Corregida ruta de Keystore en `local.properties` (cambiado `../app-release-new.keystore` a `app-release-new.keystore`), habilitando la firma V2/V3 de la APK y resolviendo el bloqueo "No se instaló la app".
- 2026-08-29: Corregida búsqueda por nombre en contactos (DISPLAY_NAME_PRIMARY), Fail-Safe sin permiso READ_CONTACTS, pipeline CI/CD en GitHub Actions, anotación @Keep en Room Entities, reglas Proguard y depuración de receiver inerte.
- 2026-08-29: Corregidos redireccionamientos HTTP en UpdateManager, ocultación de llamadas bloqueadas en historial (setSkipCallLog), normalización de números internacionales e inclusión de bloque 9746.
