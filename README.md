# Tetris para Android

Este repositorio contiene una app Android nativa (Kotlin) con un juego de Tetris.

## Controles
- Toque en el tercio izquierdo: mover pieza a la izquierda.
- Toque en el tercio derecho: mover pieza a la derecha.
- Toque en la parte superior: rotar pieza.
- Toque en el tercio central: caída rápida (hard drop).
- Cuando pierdes, toca la pantalla para reiniciar.

## Generar APK
1. Instala **Android Studio** o una toolchain con:
   - JDK 17 o JDK 21 (recomendado, no JDK 25)
   - Android SDK Platform 34
   - Build Tools compatibles
2. Configura `ANDROID_HOME`/`ANDROID_SDK_ROOT`.
3. Ejecuta:
   ```bash
   gradle assembleDebug
   ```
4. El APK debug quedará en:
   `app/build/outputs/apk/debug/app-debug.apk`

## Nota del entorno actual
En este entorno automatizado no se pudo generar el APK porque Gradle/Kotlin fallan con Java 25 (`IllegalArgumentException: 25.0.1`).
