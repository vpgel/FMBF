# Minecraft Client Bot
Мод на Forge 1.20.1

## Пример сервера
Имплементация многопоточного сервера на Python для работы с этим модом приведена в [этом файле](server.py)

## Самостоятельная сборка мода
1. Установите Git и JDK 21. Убедитесь, что `git` и `java` выдают желаемый результат.
2. Откройте в терминате папку, где желаете создать папку проекта, и введите
```bash
git clone https://github.com/VasyaProgrammist/Minecraft-Client-Bot.git
```
3. Перейдите в эту папку в терминале и введите `./gradlew build` (на Linux) или `gradlew.bat build` (на Windows). В папке `build/libs` лежит собранный мод.