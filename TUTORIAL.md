###### [Назад](README.md)
## Туториал

### Установка
1. Сначала установите
    - Python 3.11 и выше
    - Майнкрафт 1.20.1
    - Forge 4.7.30 и выше
2. Установите [мод последней версии](https://github.com/vpgel/FMBF/releases) и запустите с ним Майнкрафт.
3. Установите библиотеку Python последней версии, введя в командную строку следующее:
```bash
pip install fmbf
```

### Разработка первого скрипта
1. Создайте Python-файл (например, test.py) и введите туда следующее:
```py
from fmbf import AbsoluteSolver

solver = AbsoluteSolver()
    
def program(Object):
    print(Object)
    return 'куда смотришь'
    
solver.add(program, 'username')
```
2. Замените **`username`** на ваше игровое имя в Майнкрафте.
3. Запустите скрипт.
4. Войдите в мир Майнкрафта.

Теперь в консоли Python будет появляться слово `air`, если вы смотрите на воздух; `block`, если на блок; `entity`, если на сущность.

### Объяснение первого скрипта
```py
from fmbf import AbsoluteSolver
```
`fmbf` - это и есть библиотека, которой вы пользуетесь для общения с Майнкрафтом.
```py
solver = AbsoluteSolver()
```
`solver` - это бесконечно работающая программа, которая ловит клиенты Майнкрафта и позволяет отправлять им **команды**, которые 1) как-то управляют Майнкрафтом, 2) позволяют получить от Майнкрафта некие данные.
```py
def program
```
`solver` позволяет общаться с клиентом Майнкрафта, *привязывая* к нему некую функцию. Дело в том, что из Python в Майнкрафт можно посылать различные команды, в ответ на которые Майнкрафт посылает всякие данные (например, **блок, на который он сейчас смотрит**).

Например, ответ на команду `куда смотришь` выглядит так: 
```py
Object: "block"
Id: "minecraft:grass_block"
distance: 12.5
```
или так:
```py
Object: "air"
```
где `Object` означает тип того, на что смотрит игрок.

Этот самый `Object` можно засунуть в качестве аргумента функции `program`:
```py
def program(Object):
```
и он всегда будет означать тип того, на что смотрит игрок.

Функция `program` вызывается каждый раз, когда от Майнкрафта приходят данные - данные от выполнения **предыдущей** команды.

Результат выполнения `program` посылается в Майнкрафт как **следующая** команда.

Для этого нам обязательно нужно обернуть в `return` какую-то команду из [Документации](DOCS.md), например, [`куда смотришь`](DOCS.md):
```py
return 'куда смотришь'
```

В конце концов, функцию `program` надо связать с настоящим аккаунтом Майнкрафта. Достаточно использовать метод `solver.add()`, который в первом аргументе принимает функцию, а во втором - игровое имя аккаунта:
```py
solver.add(program, 'username')
```
### Послесловие
Кроме `куда смотришь`, в библиотеке есть масса других команд для Майнкрафта на выполнение.

Кроме `Object` и `Id`, в библиотеке есть масса других аргументов, которые приходят в Python после выполнения команд. Самый, наверно, важный аргумент это `ctx` - название команды, от которой пришли данные. 

Все команды и их спецификации расположены в [Документации](DOCS.md#референс-команд-fmbf).