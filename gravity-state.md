## [ESTADO ACTUAL] Última actualización: 2026-08-23 | Dominio: SpamZero Web Landing Page UI/UX & Android App v1.1.3 (LATAM)
- Objetivo Activo: Correcciones de codificación UTF-8, decodificación Keystore en CI/CD, protección BROADCAST_SMS, migración limpia Room DB y filtrado inteligente SMS con chequeo de contactos.
- Última Acción: Aplicado el prompt de corrección en `.github/workflows/build_apk.yml`, `AndroidManifest.xml`, `AppDatabase.kt`, `SmsNotificationListenerService.kt` y verificado `MainActivity.kt`.
- Decisiones/Bloqueos: Publicación en GitHub Pages + GitHub Releases + Obtainium + Play Store (Codeberg descartado según ERR-001).
- Vínculo Marketing: `d:\Suma_Proyectos\Proyectos\.marketing\SpamQuarantine` (Espiras RRSS y Guiones).
- Siguiente Paso: Probar compilación local o en GitHub Actions tras push a rama principal.

## Historial Reciente
- 2026-08-23: Aplicadas correcciones al workflow CI/CD, AndroidManifest (BROADCAST_SMS), Room DB (removido fallbackToDestructiveMigration) y SmsNotificationListenerService (filtro contactos y categoría 'Texto SMS').
- 2026-08-23: Implementado checklist de permisos desplegable (auto-minimizado al 4/4) y descartado del aviso Xiaomi. Commit `21e9eef`.
- 2026-08-23: Implementada guía de Ajustes Restringidos Android 13+ en UI y Toasts de `HomeScreen.kt` y `MainActivity.kt`. Push commit `9716165`.
- 2026-08-23: Recompilación Release, actualización de binario web `docs/SpamZero.apk` y push a `main` (commit `443f2a7`).
- 2026-08-23: Aplicación del prompt de corrección UTF-8, adición de licencia MIT estándar y remoción de overriding de firma release en debug en `app/build.gradle.kts`.
- 2026-08-23: Corrección de permiso `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` y fallback de intent en `HomeScreen.kt`. Commit `54122b7`.
