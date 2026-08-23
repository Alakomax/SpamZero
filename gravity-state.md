## [ESTADO ACTUAL] Última actualización: 2026-08-23 | Dominio: SpamZero Web Landing Page UI/UX & Android App v1.1.3 (LATAM)
- Objetivo Activo: Corrección de desbordamiento de UI en pantallas con fuentes personalizadas/grandes (badge de checklist en HomeScreen).
- Última Acción: Corregido el badge del checklist en `HomeScreen.kt` con `weight(1f, fill = false)`, `maxLines = 1` y `softWrap = false`. Recompilado APK Release y actualizado en `docs/SpamZero.apk`.
- Decisiones/Bloqueos: Publicación en GitHub Pages + GitHub Releases + Obtainium + Play Store (Codeberg descartado según ERR-001).
- Vínculo Marketing: `d:\Suma_Proyectos\Proyectos\.marketing\SpamQuarantine` (Espiras RRSS y Guiones).
- Siguiente Paso: Verificación por parte del tester de la vista horizontal del badge con fuente personalizada.

## Historial Reciente
- 2026-08-23: Solucionado colapso vertical de badge en `HomeScreen.kt` provocado por fuentes del sistema personalizadas. Recompilación Release y push a `main`.
- 2026-08-23: Compilación Release exitosa, actualización de `docs/SpamZero.apk` y push a `main` (commit `8cac9c7`).
- 2026-08-23: Aplicadas correcciones al workflow CI/CD, AndroidManifest (BROADCAST_SMS), Room DB (removido fallbackToDestructiveMigration) y SmsNotificationListenerService (filtro contactos y categoría 'Texto SMS').
- 2026-08-23: Implementado checklist de permisos desplegable (auto-minimizado al 4/4) y descartado del aviso Xiaomi. Commit `21e9eef`.

- Último commit revisado: 0889480
