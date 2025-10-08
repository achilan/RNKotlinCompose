Construir una app host en React Native que integra un módulo nativo Android (Kotlin) con UI Jetpack Compose.

 

El módulo nativo depende de un módulo Core (Clean Architecture).

 

La app debe listar de forma paginada y crear registros contra un API público con API Key (Bearer), protegida en Android.

 

Alcance funcional

Listado paginado de usuarios desde el módulo Kotlin (renderizado en Compose).
Controles de paginación (siguiente/anterior).
Estado de carga/errores (loading/empty/error).
Creación (insert) de un usuario vía formulario (nombre, email, género, estado).
Validaciones mínimas (email válido, campos requeridos).
Feedback de UX (snackbar/toast) para éxito/error.
 

Arquitectura requerida

Modulo perse
Core UI
React native
 

Seguridad de credenciales

API Key/Bearer Token no debe estar en JS/TS.
Cargarla solo en Android y gestionarla así:
Inyectar desde local.properties o vars de entorno CI a un BuildConfigField.
Guardar/rotar en Android Keystore si necesitas persistirla (por ejemplo, tokens temporales).
Obfuscación con R8 + evitar literales en texto plano.
JS invoca métodos nativos; la petición HTTP se ejecuta en Kotlin.
 

Buscar en internet cualquier api publicar y realizar en el crud modulo perse.