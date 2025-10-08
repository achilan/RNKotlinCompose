# RNKotlinCompose

Prueba técnica: App React Native que integra módulo nativo Android (Kotlin) con UI Jetpack Compose. El consumo del API GoREST se hace en Kotlin con Bearer Token protegido en Android.

## Requisitos

- Node 20.19.x (usa nvm): `nvm install 20.19.4 && nvm use 20.19.4`
- Android SDK + JDK 17 (Android Studio recomendado)
- Emulador Android o dispositivo físico con depuración USB

## Arquitectura (Clean-ish)

- `android/core`: Dominio (entidades `User`, `PageState`, `UsersRepository` interface)
- `android/perse-data`: Data (Retrofit + DTOs + `UsersRepositoryImpl`), inyecta `API_BASE_URL` y `GOREST_TOKEN` via BuildConfig
- `android/perse-ui`: Presentación (Compose `UsersScreen`)
- `android/perse-bridge`: Integración RN (ReactPackage/ViewManager que hostea `UsersScreen` y construye el repo)
- `android/coreui`: Base UI (placeholder reutilizable)
- App JS: `App.tsx` renderiza `PerseView` sólo en Android

Separación: JS nunca maneja el token; todo el networking se ejecuta en Kotlin.

## Seguridad de credenciales

1) Crea/edita `android/local.properties` (no se versiona) y agrega:

```
GOREST_TOKEN=tu_token
```

2) En CI puedes definir `GOREST_TOKEN` como variable de entorno (lo recoge el BuildConfig si no hay local.properties).

3) El token se expone sólo como `BuildConfig.GOREST_TOKEN` dentro del módulo `perse-data` y nunca llega a JS/TS.

4) R8/ProGuard está habilitado en release; evita literales sensibles y usa tokens de corto plazo en builds productivos.

## Instalar dependencias

```
npm install
```

## Ejecutar en Android

En una terminal (Metro):

```
npx react-native start --reset-cache
```

En otra terminal (build + install):

```
cd android
./gradlew :app:installDebug --no-daemon
```

o usando CLI:

```
npx react-native run-android
```

En la pantalla principal se renderiza el `PerseView` (Compose) con:

- Listado paginado (siguiente/anterior)
- Estados loading/empty/error
- Form de creación (nombre, email, género, estado) con validaciones básicas y Toast de feedback

## Troubleshooting

- Metro busca index.android: ya está configurado `getJSMainModuleName() = "index"`. Reinicia Metro con `--reset-cache`.
- Pantalla en blanco esporádica tras instalar:
	- El ViewManager usa `DisposeOnViewTreeLifecycleDestroyed` y compone al anexar la vista para evitar carreras con Fabric preallocation.
	- Si persiste, abre Dev Menu y elige Reload; o cierra/abre la app.
- “Unable to create converter for List<UserDto>”: ya se usa Moshi con `KotlinJsonAdapterFactory`.
- “No virtual method setContent(…) en ComposeView”:
	- Todas las capas usan el mismo BOM de Compose y el bridge habilita `compose` + dependencias `ui/material3`.
- Sin dispositivo: crea/abre un AVD desde Android Studio o conecta un dispositivo.

## Notas de build/seguridad

- R8 on: `minifyEnabled true` en release (app y libs). Reglas para RN/Compose incluidas.
- El token de ejemplo en `local.properties` es local. No lo subas al repo. En producción usa secretos de CI.

## Extensiones (opcional)

- Separar aún más con DI (Hilt/Koin) y Paging 3 para la lista.
- Agregar Update/Delete para CRUD completo.