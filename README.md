# Tetris para Android

Este repositorio contiene una app Android nativa (Kotlin) con un juego de Tetris.

## Controles
- Toque en el tercio izquierdo: mover pieza a la izquierda.
- Toque en el tercio derecho: mover pieza a la derecha.
- Toque en la parte superior: rotar pieza.
- Toque en el tercio central: caída rápida (hard drop).
- Cuando pierdes, toca la pantalla para reiniciar.

## Generar APK (local)
1. Instala Android Studio (o Android SDK + JDK 17/21).
2. Asegúrate de tener instalados:
   - Android SDK Platform 34
   - Android Build-Tools
3. Desde la raíz del proyecto, ejecuta:
   ```bash
   export JAVA_HOME=/ruta/a/jdk-21
   gradle assembleDebug
   ```
4. APK generado en:
   `app/build/outputs/apk/debug/app-debug.apk`


## Script rápido
También puedes usar:
```bash
./scripts/build_apk.sh
```

## Nota de este entorno CI
Aquí no fue posible generar el APK por restricciones de red del entorno (no puede descargar dependencias de Gradle/Android desde `google()` / `mavenCentral()`).
