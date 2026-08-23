## [ESTADO ACTUAL] Última actualización: 2026-08-23 | Dominio: SpamZero Web Landing Page UI/UX & Android App v1.1.3 (LATAM)
- Objetivo Activo: Publicación oficial de la versión `v1.1.3` en GitHub Releases para distribución directa y actualización in-app.
- Última Acción: Incrementada versión a `1.1.3` (code 17) en `build.gradle.kts`, `HomeScreen.kt` y `RELEASE_NOTES.md`. Ejecutada secuencia de Git commit (`bump: release version 1.1.3`), `git push origin main` y tag `v1.1.3`.
- Decisiones/Bloqueos: Publicación exclusiva en GitHub Releases + Obtainium + Play Store (Codeberg descartado según ERR-001).
- Vínculo Marketing: `d:\Suma_Proyectos\Proyectos\.marketing\SpamQuarantine` (Espiras RRSS y Guiones).
- Siguiente Paso: Confirmación de compilación del artefacto APK en GitHub Actions.

## Historial Reciente
- 2026-08-23: Tag `v1.1.3` publicado en GitHub para compilación e instalación directa de testers.
- 2026-08-23: Commit atómico `6af6cc8` registrado con correcciones de permisos asíncronos (`Dispatchers.IO`), visibilidad `<queries>`, verificación `ComponentName` y cierre de diálogo.
- 2026-08-23: Refactorización de permisos reales: verificación unificada en `PermissionChecker`, comprobación en `onResume()`, checklist granular y detección de fabricantes agresivos.
- 2026-08-22: Refactorización integral de 8 puntos (seguridad de credenciales, deduplicación atómica SMS, migraciones Room, resiliencia ReDoS y CI/CD con gradlew wrapper).
- 2026-08-22: Implementación de `SmsNotificationListenerService` para silenciar notificaciones SMS spam en tiempo real, auto-sincronización de reglas en SQLite, expansión de regex (.vc, Free Bet) y versión `v1.1.2`.
- 2026-08-22: Corrección del algoritmo de comparación de versión en `UpdateManager.kt`, tag `v1.1.1` subida a GitHub con flujo CI/CD automático.
- 2026-08-21: Auditoría de seguridad y hardening: `allowBackup=false`, R8 + Resource Shrinking (2.3 MB) y tag `v1.1.0`.

- Último commit revisado: v1.1.3
