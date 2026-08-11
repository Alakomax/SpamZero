# Política de Privacidad de SpamQuarantine

**Última actualización:** 11 de agosto de 2026

## 1. Declaración Fundamental de Privacidad

**SpamQuarantine** es una aplicación móvil nativa diseñada bajo el principio de **Privacidad por Diseño** (*Privacy by Design*). 

- **0 Servidores Externos:** La aplicación NO posee servidores propios, NO transmite datos a la nube y NO envía información a terceros.
- **0 Analíticas y Rastreadores:** La aplicación NO utiliza cookies, SDKs de publicidad, herramientas de rastreo (*trackers*), ni servicios de analítica o telemetría.
- **Procesamiento 100% Local:** Toda la interceptación de llamadas, normalización de números telefónicos y gestión de reglas Regex se ejecuta de forma síncrona y local en el procesador del dispositivo móvil del usuario.

---

## 2. Marco Legal Aplicable

Esta política se redacta en conformidad con la **Ley N° 19.628 sobre Protección de la Vida Privada (Chile)** y los estándares internacionales de protección de datos personales.

---

## 3. Justificación y Uso de Permisos del Dispositivo

Para cumplir con su función principal de interceptación previa al timbre de llamadas no deseadas, SpamQuarantine solicita los siguientes permisos nativos de Android:

| Permiso Android | Propósito Exclusivo | Uso de Datos |
| :--- | :--- | :--- |
| `CallScreeningService` (`BIND_SCREENING_SERVICE`) | Interceptar la llamada entrante antes de que suene el primer repique. | Evaluado localmente en milisegundos contra los patrones Regex. No se transmite ningún número a redes externas. |
| `READ_CONTACTS` | Permitir la función de **Lista Blanca Automática**. | Consulta local directa en la agenda del dispositivo. Las llamadas de contactos guardados son aprobadas inmediatamente. **La agenda NUNCA se copia, sube ni comparte.** |
| `READ_CALL_LOG` / `READ_PHONE_STATE` | Permitir la auditoría local del registro de llamadas rechazadas. | Almacenado de forma cifrada/privada en la base de datos local del teléfono (`Room DB`). |

---

## 4. Almacenamiento y Control de Datos

1. **Base de Datos de Cuarentena:** Los registros de llamadas bloqueadas (número telefónico, fecha, hora y regla coincidente) se guardan únicamente en la base de datos SQLite/Room del dispositivo.
2. **Control Total del Usuario:** El usuario puede eliminar registros individuales de cuarentena o vaciar el historial completo en cualquier momento utilizando el botón "Limpiar Todo" dentro de la aplicación.
3. **Eliminación Definitiva:** Al desinstalar SpamQuarantine, el sistema operativo Android elimina automáticamente toda la base de datos y preferencias locales asociadas a la app.

---

## 5. Ausencia de Cookies

SpamQuarantine es una aplicación nativa pura desarrollada en Kotlin/AndroidX. **No se emplean cookies de navegación, rastreadores web ni tecnologías de almacenamiento de sesión HTTP.**

---

## 6. Cambios en esta Política

Cualquier actualización a esta política será documentada en el repositorio oficial de GitHub del proyecto. Dado que la aplicación no recopila correos ni canales de comunicación de los usuarios, las modificaciones se reflejarán mediante las notas de versión (*Release Notes*).

---

## 7. Contacto y Transparencia

Si tiene dudas sobre el funcionamiento técnico o la privacidad de la aplicación, puede revisar directamente el código fuente abierto del proyecto en:
[https://github.com/Alakomax/SpamQuarantine](https://github.com/Alakomax/SpamQuarantine)
