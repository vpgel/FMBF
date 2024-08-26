Fork's Minecraft Botting Framework (FMBF) - это фреймворк, позволяющий управлять ботами в Minecraft через Python. Он состоит из мода FMBF на Minecraft Forge 1.20.1 и библиотеки fmbf на Python 3.11.

Фреймворк в альфа-версии! Использование не рекомендуется
===

Как его использовать? (Туториал для Windows 10)
---
Например, так:
1. Установите Python версии 3.11 с [официального сайта](https://www.python.org/ftp/python/3.11.9/python-3.11.9-amd64.exe) (за другие версии Python не ручаюсь, но [вот ссылка на все](https://www.python.org/ftp/python/)) и при установке добавьте его скрипты в PATH.
2. Установите библиотеку fmbf с помощью введения в консоль командной строки этих строк:
```cmd
pip install --upgrade pip
pip install fmbf
```
3. Создайте файл `example.py` и введите в него следующее:
```python
from fmbf import MinecraftServer

server = MinecraftServer()

def handler(**args):
    return 'move_forward'

# Замените Test23 на никнейм вашего игрока Майнкрафт
bot = server.connect('Test23', handler)
```
4. Откройте командную строку в папке, где находится файл `example.py`, и введите строку
```cmd
python example.py
```
5. Установите [клиент игры Minecraft](https://prismlauncher.org) версии 1.20.1 с загрузчиком модов Forge версии большей или равной 47.3.0.
6. Установите последний релиз мода FMBF с его [страницы на Modrinth](https://modrinth.com/project/fmbf) в сопутствующую клиенту Майнкрафта папку модов.
7. Запустите Майнкрафт
8. Откройте любой одиночный мир или войдите в любой многопользовательский сервер

(Ожидаемый) результат: ваш игрок идёт вперёд сам по себе!

Мне нужна документация! / туториал!
----
Вам [сюда](https://github.com/vpgel/FMBF/tree/python) - в ветку [python](https://github.com/vpgel/FMBF/tree/python).

Возникли проблемы с использованием или есть предложения по изменению/добавлению чего-то во фреймворк?
----
[Вам сюда](https://github.com/vpgel/FMBF/issues).

Где посмотреть исходный код?
----

Исходный код мода располагается в ветке [forge-1.20.1](https://github.com/vpgel/FMBF/tree/forge-1.20.1), библиотеки - в ветке [python](https://github.com/vpgel/FMBF/tree/python).