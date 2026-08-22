# SpamZero - Normas de Desarrollo y Estilo de Código

Este documento establece las políticas obligatorias de codificación, documentación y commits para el proyecto SpamZero.

---

## 1. Estilo de Comentarios e Inline Docs

* **Estricta Concisión:** Prohibido incluir comentarios narrativos, explicativos o redundantes que reediten lo que el código ya expresa de forma autodocumentada.
* **Prohibición de Modismos LLM:** No utilizar frases como `"Este método se encarga de..."`, `"Paso 1: Evaluamos..."`, `"Verificamos si..."` ni firmas sintéticas.
* **Uso Exclusivo:** Los comentarios solo se permiten en casos de **lógica de negocio no trivial**, trucos de bajo nivel o banderas de configuración de compilación.

---

## 2. Convención de Commits y Control de Versiones

* **Commits Atómicos:** Cada commit debe representar un cambio discreto, funcional y pequeño.
* **Prohibición de Commits Masivos:** Evitar subir reescrituras completas de múltiples archivos en un solo commit salvo inicializaciones explícitas.
* **Formato de Mensaje:** Usar convención estándar breve:
  * `feat: add SIM indicator to TopAppBar`
  * `fix: handle null caller ID in screening service`
  * `refactor: clean unused preferences`

---

## 3. Redacción de Documentación y Releases

* **Formato Humano Directo:** Las descripciones del repositorio, notas de versión (`RELEASE_NOTES.md`), `README.md` y descripciones en tiendas/repositorios deben mantener una redacción directa, concisa y sin plantillas sobre-estructuradas típicas de asistentes de inteligencia artificial.

## 4. Revisión Adversarial
Cambios en el servicio de screening, manejo de permisos o caller ID pasan obligatoriamente por revisión adversarial en sesión aislada (ver reglas generales, Sección VI) antes de integrarse.