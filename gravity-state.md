## [ESTADO ACTUAL] Última actualización: 2026-08-23 | Dominio: SpamZero Web Landing Page UI/UX & Android App v1.1.3 (LATAM)
- Objetivo Activo: Publicación oficial de la versión `v1.1.3` en GitHub Releases y vinculación dinámica en la Landing Page web.
- Última Acción: Corregidos los enlaces del botón "Descargar APK" en `docs/index.html` (Landing Page `alakomax.github.io/SpamZero/`) para apuntar a `https://github.com/Alakomax/SpamZero/releases/latest/download/app-release.apk` en lugar del binario estático local `SpamZero.apk` v1.1.0. Push a `main` completado.
- Decisiones/Bloqueos: Publicación exclusiva en GitHub Releases + GitHub Pages + Obtainium + Play Store (Codeberg descartado según ERR-001).
- Vínculo Marketing: `d:\Suma_Proyectos\Proyectos\.marketing\SpamQuarantine` (Espiras RRSS y Guiones).
- Siguiente Paso: Verificación de descarga directa en landing page web.

## Historial Reciente
- 2026-08-23: Corrección en `docs/index.html` para que el botón de la landing page descargue siempre el APK de la última versión de GitHub Releases.
- 2026-08-23: Tag `v1.1.3` publicado en GitHub para compilación e instalación directa de testers.
- 2026-08-23: Commit atómico `6af6cc8` registrado con correcciones de permisos asíncronos (`Dispatchers.IO`), visibilidad `<queries>`, verificación `ComponentName` y cierre de diálogo.
- 2026-08-22: Refactorización integral de 8 puntos (seguridad de credenciales, deduplicación atómica SMS, migraciones Room, resiliencia ReDoS y CI/CD con gradlew wrapper).
- 2026-08-21: Auditoría de seguridad y hardening: `allowBackup=false`, R8 + Resource Shrinking (2.3 MB) y tag `v1.1.0`.

- Último commit revisado: 9939342
