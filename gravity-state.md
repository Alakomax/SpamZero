## [ESTADO ACTUAL] Última actualización: 2026-08-29 | Dominio: SpamZero Web Landing Page UI/UX & Android App v1.1.4 (LATAM)
- Objetivo Activo: Solución al problema de descarga de la versión anterior .13 desde la página web (GitHub Pages).
- Última Acción: Reemplazados los ejecutables en `docs/SpamZero.apk` y `docs/SpamQuarantine.apk` con el APK firmado v1.1.4 e subidos al repositorio (`commit ee15d48`).
- Decisiones/Bloqueos: Ninguno. GitHub Pages sirve directamente la versión v1.1.4 en la web.
- Vínculo Marketing: `d:\Suma_Proyectos\Proyectos\.marketing\SpamQuarantine` (Espiras RRSS y Guiones).
- Siguiente Paso: Probar descarga en la web (limpiando caché del navegador si es necesario).

## Historial Reciente
- 2026-08-29: Reemplazado `docs/SpamZero.apk` con la versión oficial firmada v1.1.4 (commit `ee15d48`) resolviendo la entrega de la versión antigua .13 en la página de descargas directas.
- 2026-08-29: Push de commit `2ff1ea7` y actualización de tag `v1.1.4` en GitHub remoto. Disparado workflow `build_apk.yml` para generación y publicación del APK firmado oficial.
- 2026-08-29: Corregida ruta de Keystore en `local.properties` (cambiado `../app-release-new.keystore` a `app-release-new.keystore`), habilitando la firma V2/V3 de la APK y resolviendo el bloqueo "No se instaló la app".
