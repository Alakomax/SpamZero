## [ESTADO ACTUAL] Última actualización: 2026-08-29 | Dominio: SpamZero Web Landing Page UI/UX & Android App v1.1.4 (LATAM)
- Objetivo Activo: Solución de fallas en descarga de actualizaciones, supresión de historial de llamadas y normalización de números internacionales.
- Última Acción: Aplicadas correcciones en UpdateManager, SpamCallScreeningService, PhoneUtils y SmsNotificationListenerService. Build Release exitoso (`BUILD SUCCESSFUL in 2m 19s`).
- Decisiones/Bloqueos: Publicación de versión v1.1.4 en GitHub Releases + Obtainium + Play Store.
- Vínculo Marketing: `d:\Suma_Proyectos\Proyectos\.marketing\SpamQuarantine` (Espiras RRSS y Guiones).
- Siguiente Paso: Commit y Push a repositorio remoto.

## Historial Reciente
- 2026-08-29: Corregidos redireccionamientos HTTP en UpdateManager, ocultación de llamadas bloqueadas en historial (setSkipCallLog), normalización de números internacionales e inclusión de bloque 9746.
- 2026-08-23: Aplicadas correcciones UTF-8, segregación de categoría 'Texto SMS' en llamadas, rootProject.file para Keystore y verificado build Gradle.
- 2026-08-23: Solucionado colapso vertical de badge en `HomeScreen.kt` provocado por fuentes del sistema personalizadas.
