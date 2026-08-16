### 📱 SpamQuarantine - Lanzamiento para Android

Haz clic en **`app-debug.apk`** para descargar e instalar la aplicación directamente en tu teléfono.

#### 🚀 Novedades v1.0.13:

* 📱 **Ícono Nativo Oficial de la App:** Actualización completa de todos los recursos gráficos nativos en Android (`mipmap-mdpi`, `mipmap-hdpi`, `mipmap-xhdpi`, `mipmap-xxhdpi`, `mipmap-xxxhdpi` y `ic_launcher_foreground.png`) incorporando el nuevo logo vectorial oficial (`SpamQuarantine_Logo.png`).
* 📂 **Motor de Reglas Agrupado y Explicado:** Reorganización visual y lógica de todos los patrones de interceptación en **3 Categorías Claras**: *📞 Llamadas Nacionales*, *🌐 Spam e Internacionales*, y *📩 SMS y Estafas (Phishing)*. Cada regla ahora muestra un **Título Informativo** y una **Explicación Concisa** de su funcionamiento para fácil comprensión del usuario.
* 🛡️ **Filtro Avanzado de SMS Phishing y URLs Acortadas:** Incorporación de nuevos motores de interceptación contra enlaces acortados sospechosos (`bit.ly`, `tinyurl`, `cutt.ly`), falsos depósitos/compensaciones bancarias y remitentes internacionales fuera de contactos.
* ⚡ **Soporte Multilínea (`Pattern.DOTALL`):** Evaluación completa del cuerpo del SMS a través de saltos de línea para evitar evasiones de spam.

#### 🚀 Novedades v1.0.12:

* 🔄 **Auto-Sincronización de Reglas de SMS Spam:** Se resuelve el problema por el cual las instalaciones existentes no recibían los nuevos filtros de SMS (Casinos, TAG, Copec). Las reglas por defecto ahora se sincronizan automáticamente en la base de datos local sin afectar reglas personalizadas.
* 🔄 **Botón de Restauración de Reglas:** Nueva opción en la pantalla de *Reglas* para forzar el restablecimiento o recarga de la lista oficial de patrones por defecto.
* ⚖️ **Protección de Derechos de Autor (Todos los Derechos Reservados):** Actualización de la licencia legal a *Código Fuente Visible (Proprietary / All Rights Reserved)* en `LICENSE`, `TERMS_OF_SERVICE.md`, `PRIVACY_POLICY.md` y en la suite legal de la app. El código permanece público para transparencia, pero prohibiendo legalmente su copia o comercialización por terceros.
* ☕ **Donaciones "Invítame un café ☕":** Integración de tarjeta de donaciones voluntarias con enlace directo a PayPal (`omargonzalez76@gmail.com`) en la pantalla principal y en la pestaña Legal para apoyar el mantenimiento independiente.

#### 🚀 Novedades Anteriores (v1.0.11):


* 🎨 **Ícono Oficial Definitivo:** Implementación y almacenamiento en el repositorio (`logo/official_logo.png`) del ícono oficial del escudo 3D metálico con fondo de tono slate adaptativo `#1E232A` para perfecto encuadre en el celular.
* ⚡ **Interceptación previo a timbre (0 repiques):** Bloquea llamadas spam en milisegundos sin encender la pantalla utilizando `CallScreeningService`.
* 💬 **Bloqueador de SMS Spam y Estafas:** Interceptación en segundo plano de mensajes SMS no deseados (casinos internacionales `+34 931...`, *Joker Jewels*, *Fortune Rabbit*, y estafas locales `44...`, *TAG*, *Copec*, *abonos falsos*).
* 📄 **Exportador Probatorio Auditado (.txt):** Genera e imprime informes con timestamps y evidencia estructurada para denuncias ante la **SERNAC**, **Juzgados de Policía Local** o **PDI**.
* 🛑 **Master Switch ON/OFF:** Interruptor en pantalla principal para pausar o reactivar el filtro manualmente sin perder permisos.
* 🛡️ **Patrones Chile y Globales preconfigurados:** Bloquea prefijos `600`, `800`, `809`, rangos de telemarketing (`+56 9 XXXX XXXX`), números VoIP `44` y spammers de España (`+34 931`).
* 🔒 **Lista Blanca Automática:** Las personas guardadas en tus contactos nunca son bloqueadas.
* ⚖️ **Módulo Legal In-App & Privacidad Absoluta:** 100% offline, 0 servidores, 0 analíticas, 0 cookies. Incluye visualizador interno de Política de Privacidad (Ley 19.628 Chile), Términos de Uso y Licencia MIT.
