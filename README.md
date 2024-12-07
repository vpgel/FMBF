# Fork's Minecraft Botting Framework
Мод на Minecraft для версии 1.20.1, на движке Forge версии 47.3.0 и выше

## Самостоятельная сборка мода
1. Установите [Git](https://git-scm.com/downloads) и [JDK 17](https://adoptium.net/temurin/releases/?version=17&package=jdk)
2. Откройте терминал и убедитесь, что команды `git` и `java` не выдают ошибку
2. Откройте в терминале папку, где желаете создать папку проекта, и введите `git clone https://github.com/vpgel/fmbf.git -b forge-1.20.1`
3. Перейдите в эту папку в терминале
4. Введите `./gradlew build` (на Linux) или `gradlew build` (на Windows). В папке `build/libs` лежит собранный мод - тот, чьё имя кончается на `-all`.