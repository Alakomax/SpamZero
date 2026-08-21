# Política de Privacidad de SpamZero

**Última actualización:** 21 de agosto de 2026

## 1. Compromiso de Privacidad

**SpamZero** es una aplicación móvil nativa diseñada bajo el principio de **Privacidad por Diseño** (*Privacy by Design*). Nuestra arquitectura garantiza que **ningún dato personal, número telefónico, registro de llamadas ni mensaje SMS abandone jamás tu dispositivo**. 

SpamZero opera de manera **100% offline y local** en la memoria del procesador de tu teléfono.

## 2. Información que NO recopilamos

* **No recopilamos datos personales:** No requerimos creación de cuenta, registro de correo electrónico ni identificación personal.
* **No registramos llamadas ni mensajes en servidores:** El historial de cuarentena se guarda en una base de datos local protegida (`SQLite / Room`) dentro de la memoria interna del teléfono.
* **Sin servidores ni telemetría:** SpamZero no posee servidores externos ni envía analíticas de uso, datos de ubicación o hábitos de navegación.

## 3. Uso de Permisos del Dispositivo

Para cumplir con su función principal de interceptación previa al timbre de llamadas no deseadas, SpamZero solicita los siguientes permisos nativos de Android:

* `android.permission.READ_PHONE_STATE`: Necesario para detectar el evento de llamada entrante.
* `android.permission.READ_CALL_LOG`: Requerido por el servicio nativo `CallScreeningService` para identificar el número entrante y aplicar el bloqueo previo al timbre.
* `android.permission.READ_CONTACTS`: Utilizado exclusivamente para consultar la agenda de contactos del dispositivo como **Lista Blanca Automática**. Si un número está en tus contactos, el filtro lo permite siempre. Tu agenda jamás se envía a ningún destino externo.
* `android.permission.RECEIVE_SMS`: Utilizado exclusivamente para analizar en tiempo real el texto del SMS entrante contra patrones Regex de estafas, casinos y phishing.

## 4. Almacenamiento y Seguridad de Datos

1. **Almacenamiento Local:** Todos los logs de llamadas y SMS bloqueados se guardan en el almacenamiento privado de la aplicación.
2. **Control del Usuario:** El usuario puede vaciar el historial de cuarentena en cualquier momento desde la interfaz.
3. **Eliminación Definitiva:** Al desinstalar SpamZero, el sistema operativo Android elimina automáticamente toda la base de datos y preferencias locales asociadas a la app.

## 5. Cookies y Rastreadores

1. **Cookies:** SpamZero es una aplicación nativa pura desarrollada en Kotlin/AndroidX. No se emplean cookies de navegación, rastreadores web ni tecnologías de almacenamiento de sesión HTTP.
2. **Publicidad:** La aplicación es 100% libre de anuncios, banners publicitarios o SDKs de monetización de terceros.

## 6. Cumplimiento Normativo

Esta Política de Privacidad cumple con las directrices de la **Ley N° 19.628 de Protección de la Vida Privada** en Chile y las normativas de protección de datos personales internacionales aplicables a software móvil.

## 7. Contacto y Auditoría del Código

Al ser un proyecto de código fuente visible, cualquier usuario o auditor de seguridad puede verificar la integridad del código en nuestro repositorio oficial de GitHub:

[https://github.com/Alakomax/SpamZero](https://github.com/Alakomax/SpamZero)
