# Reglas de R8 para Misnotiks-.
#
# Mantenemos los nombres de clases (sin ofuscar) para evitar romper nada por
# reflexion en las librerias de AndroidX/Compose, pero sí dejamos que R8
# elimine el codigo muerto y optimice el bytecode: eso es lo que realmente
# reduce el tamaño del APK y el trabajo en tiempo de arranque.
-dontobfuscate

# org.json (usado para leer/escribir el JSON de datos) es parte del propio
# Android y no necesita reglas especiales.

# Las librerias de AndroidX/Compose y Navigation Compose incluyen sus propias
# reglas de consumidor dentro de sus .aar, asi que no hace falta declarar
# keep rules manuales para ellas.
