# Misnotiks-

App Android (Kotlin + Jetpack Compose) para guardar fichas de datos organizadas
por categorias: tiendas, personas, cuentas, servicios, etc. Cada ficha admite
hasta 5 campos con boton de copiar, y se puede editar, compartir o eliminar.

Los datos se guardan sin cifrar en un archivo JSON dentro del almacenamiento
privado de la app, y se pueden exportar/importar como archivo `.json` desde
la pantalla principal (iconos de subir/bajar en la barra superior).

## Novedades (v1.1)

- **Mover fichas entre categorias**: dentro de una ficha, boton "mover"
  (icono de carpeta) que permite elegir la categoria destino.
- **Acerca de**: icono de informacion en la pantalla principal con los
  datos de autoria.
- **Tema claro / oscuro**: icono de sol/luna en la pantalla principal,
  se recuerda la preferencia entre sesiones.
- **Hasta 10 campos por ficha** (antes 5).
- **Compartir un campo individual**: boton de compartir junto al de
  copiar en cada campo de una ficha.
- **Reordenar arrastrando**: mantén presionado el icono de "arrastrar"
  (☰) junto a cada categoria o ficha para cambiar su orden.

## Como obtener el APK usando GitHub (sin instalar Android Studio)

1. Crea un repositorio nuevo en GitHub (puede ser privado) y sube todo el
   contenido de esta carpeta tal cual esta (respetando la estructura de
   carpetas).

   ```bash
   git init
   git add .
   git commit -m "Misnotiks- app"
   git branch -M main
   git remote add origin https://github.com/TU_USUARIO/TU_REPO.git
   git push -u origin main
   ```

2. En cuanto hagas el push, la pestaña **Actions** del repositorio ejecutara
   automaticamente el workflow `Build APK` (definido en
   `.github/workflows/build-apk.yml`). Tambien puedes lanzarlo a mano desde
   Actions > Build APK > Run workflow.

3. Cuando el workflow termine (icono verde), entra a esa ejecucion y baja
   hasta la seccion **Artifacts**: alli encontraras `misnotiks-release-apk`,
   un .zip que contiene el `app-release.apk`.

4. Descarga el .zip, extrae el APK, pasalo a tu telefono e instalalo
   (activa "Instalar apps de origenes desconocidos" si Android lo pide).

## Estructura del proyecto

```
app/src/main/java/com/misnotiks/app/
  MainActivity.kt
  data/DataStore.kt        -> modelo de datos y lectura/escritura del JSON
  ui/AppNavHost.kt         -> navegacion entre pantallas
  ui/CategoriesScreen.kt   -> lista de categorias + exportar/importar
  ui/EntriesScreen.kt      -> lista de fichas dentro de una categoria
  ui/EntryScreen.kt        -> ver/editar una ficha, copiar campos, compartir
```

## Rendimiento y optimizacion (v1.2)

- El workflow ahora compila la variante **release** con **R8** activado
  (`isMinifyEnabled = true`, `isShrinkResources = true`): se elimina el
  codigo y los recursos que no se usan, lo que reduce el tamaño del APK y
  el trabajo que hace el sistema al instalar y abrir la app, comparado con
  el build de depuracion que se generaba antes.
- Se dejaron los nombres de clases sin ofuscar (`-dontobfuscate` en
  `proguard-rules.pro`) para minimizar el riesgo de romper algo por
  reflexion, priorizando estabilidad sobre el ultimo byte de tamaño.
- Se elimino un mecanismo interno de "refresco manual" que forzaba
  recomposiciones de toda la pantalla en cada accion; ahora las listas
  usan `mutableStateListOf` de Compose y solo se redibuja lo que
  realmente cambio (esto tambien corrigio el parpadeo al arrastrar).
- La app sigue sin usar red, servicios en segundo plano, GPS ni base de
  datos pesada: en el uso diario el consumo de batería y memoria es
  minimo.
- Se firma con la clave de depuracion automatica de Android (no hace
  falta un keystore propio), asi que se instala directamente en tu
  telefono igual que antes.

## Notas

- Requiere Android 7.0 (API 24) o superior.
- Esta version no cifra los datos: cualquiera con acceso al telefono
  desbloqueado y permisos de depuracion podria leer el archivo JSON interno.
