### 📱 SpamZero - Lanzamiento Oficial para Android & LATAM

Haz clic en **`app-release.apk`** para descargar e instalar la aplicación directamente en tu teléfono.

#### 🚀 Novedades v1.1.1-beta (Alertas SMS, Permisos Runtime & Reglas por SIM):

* 📲 **Permisos Runtime de SMS y Notificaciones:** Integración del receptor de fondo con solicitud de permisos `RECEIVE_SMS` y `POST_NOTIFICATIONS` desde la pantalla principal para compatibilidad total con Android 13+.
* 🚨 **Notificación de Alerta Interactivas:** Al interceptar un SMS de estafa (casinos `7K`, falsas multas, links acortados), la app emite la alerta `🚨 SMS Sospechoso Interceptado` con el botón de acción `[Ver en Cuarentena]` para navegación directa.
* 🌐 **Filtro Automático de Reglas por SIM:** Sincronización inteligente de reglas según la SIM activa + reglas globales LATAM, eliminando la acumulación de reglas de otros países.
* 📱 **Corrección Visual de Layout:** Ajuste responsive en la barra de navegación inferior (`Cuarentena`) con restricción de línea única (`maxLines = 1`, `softWrap = false`), solucionando saltos de texto en fuentes de sistema personalizadas.

* 🏷️ **Rebranding Oficial a SpamZero:** Evolución del producto a **SpamZero**, un nombre comercial directo, moderno y con pronunciación fluida en toda Latinoamérica.
* ⚡ **Silenciamiento e Interceptación a 0 Repiques:** Implementación de `setSilenceCall(true)` (API 29+) y pre-calentamiento del caché de reglas en RAM. Las llamadas spam ahora son silenciadas y cortadas instantáneamente sin dar ningún repique ni sonar el ringer del teléfono.
* 📲 **Detección Automática de SIM y Bandera en Pantalla:** Indicador visual de bandera Emoji (🇨🇱, 🇨🇴, 🇦🇷, 🇻🇪, 🇲🇽, 🇵🇪, 🌎) en la barra superior (`TopAppBar`), indicando el país detectado en la tarjeta SIM del dispositivo.
* 🌐 **Motor de Reglas Multi-País (LATAM):** Catálogo de reglas adaptado para **Chile** (600, 800, 809, 80 cobranzas, celulares corporativos 9 4343, 9 5233, 9 4434, 9 2882), **Colombia** (018000, 601, masivos 300/310/320), **Argentina** (0800, 0810, 11 CABA), **Venezuela** (0800, 0412/0414/0424), **México** (800, 55 CDMX) y **Perú** (0800, 01). Selector manual de país en la pantalla de *Reglas*.
* 📩 **Filtro Avanzado de SMS Spam & Phishing:** Sanitización de remitentes con espacios (`+34 931 77 21 55`, `44 217 0300`), invocación de `abortBroadcast()` y enriquecimiento de palabras clave contra apuestas/casinos (`[BET7K]`, `Fortune Ox`, `Fortune Mouse`, `Sugar Rush`, `7K`), falsas multas y cobros de autopistas (`Aviso TAG`, `multa pendiente`).

#### 🚀 Novedades v1.0.13:

* 📱 **Ícono Nativo Oficial de la App:** Actualización completa de todos los recursos gráficos nativos en Android (`mipmap-mdpi`, `mipmap-hdpi`, `mipmap-xhdpi`, `mipmap-xxhdpi`, `mipmap-xxxhdpi` y `ic_launcher_foreground.png`).
* 📂 **Motor de Reglas Agrupado y Explicado:** Reorganización visual de patrones en 3 categorías (*📞 Llamadas Nacionales*, *🌐 Spam e Internacionales*, *📩 SMS y Estafas (Phishing)*).
* 🛡️ **Filtro Avanzado de SMS Phishing y URLs Acortadas:** Interceptación contra enlaces acortados sospechosos (`bit.ly`, `tinyurl`, `cutt.ly`), falsos depósitos/compensaciones bancarias y remitentes internacionales fuera de contactos.
* ⚡ **Soporte Multilínea (`Pattern.DOTALL`):** Evaluación completa del cuerpo del SMS con saltos de línea.
