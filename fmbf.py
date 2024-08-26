'''
Fork's Minecraft Botting Framework
----
Харош, что импортировал! Теперь чекни пример использования:

.. code-block:: python
    from fmbf import AbsoluteSolver

    solver = AbsoluteSolver()
    
    def program():
        return 'walk_forward'
    
    solver.add('Test23', program)

Читай эти подсказки по мере изучения модуля!
'''

import threading
import socket
import json
import inspect
from time import time
from typing import Protocol

# Библиотека для красивого дебаггинга. Не могу от неё оторваться.
# Если ещё кто-то нарвётся на этот код, вам придётся поставить её через cmd:
# > pip install icecream
#from icecream import ic

def _decode_data(request: str) -> dict:
    '''Эта функция преобразует входящее сообщение от бота в словарь данных об окружающем мире Майнкрафта и о состоянии самого бота.'''
    try:
        return json.loads(request)
    except json.decoder.JSONDecodeError:
        print('Ошибка в отправленном из бота сообщении. Сообщи его создателю!')

class _ProgramCallable(Protocol):
    '''Это класс, описывающий **тип** функции, отвечающей за программу, которая даётся боту. Суть в том, что эта функция может принимать неограниченное количество названных аргументов и обязана возвращать только строку, и этот класс и описывает функцию такого рода.'''
    def __call__(self, **kwargs: str) -> str: ...

# Шаблон класса, чтобы строка 39 не ругалась на несуществующий класс, который я потом всё равно создаю
class _MinecraftConnection(threading.Thread): ...

class AbsoluteSolver(threading.Thread):
    '''ЗАПУСТИ PYTHON ДО MINECRAFT'А!
    ----'''
    def __init__(self, ip='127.0.0.1', port=2323, debug=False):
        '''Перед тобой программа, бесконечно слушающая входящие соединения клиентов Майнкрафта, но разрешающая войти только тем, кто был передан ей командой **add()**. Она работает параллельно основному потоку Python.

        Этот объект запускается только один раз, сразу после его инициализации. Его можно в любой момент закрыть методом **close()**, после чего он больше не запустится.
        
        :param ip: IP-адрес (по умолчанию твой локальный)
        :param port: порт для входящих соединений (по умолчанию 2323, как и в моде Minecraft)
        :param debug: режим отладки - куча сообщений заполнят консоль мигом'''
        super().__init__()
        self.ip = ip
        self.port = port
        self.debug = debug
        self.bots: list[_MinecraftConnection] = []
        self.allowed_bots: dict[callable] = dict()
        self.socket = socket.socket()
        self.is_running = True
        self.start()
    
    def run(self):
        # Объект класса MinecraftServer работает в отдельном потоке от остальной программы.
        # Он открывает TCP-сервер и постоянно слушает входящие соединения (как ожидается, от клиентов Minecraft с модом FMBF).
        self.socket.bind((self.ip, self.port))
        self.socket.settimeout(0.5)
        self.socket.listen()
        while self.is_running:
            if self.debug:
                print(f'Потоки программы: {threading.enumerate()}')
                print(f'Разрешения Солвера: {self.allowed_bots}')
            try:
                bot, _ = self.socket.accept()
                request_length = int.from_bytes(bot.recv(2))
                request = bot.recv(request_length*2).decode('utf-16-be')
                print(request)
                name = _decode_data(request)['name']

                if name in self.allowed_bots.keys():
                    bot.sendall((chr(2)+'👋').encode('utf-16-be'))
                    self.bots.append(_MinecraftConnection(self, name, bot, self.allowed_bots[name]))

                    if self.debug:
                        print(f'К Python попытался подключиться {name}, я разрешил!')
                else:
                    bot.sendall((chr(2)+'✋').encode('utf-16-be'))
                    bot.close()

                    if self.debug:
                        print(f'К Python попытался подключиться какой-то {name}, я запретил.')
            
            except socket.timeout:
                if not self.is_running:
                    break
            except ConnectionResetError:
                pass
        
        for bot in self.bots:
            bot.close()
        self.socket.close()
        if self.debug:
            print(f'Закрыл сервер!')

    def add(self, name: str, program: _ProgramCallable):
        '''Соединить Python с ботом Minecraft и дать ему программу, по которой он будет работать.
        Эта программа запускается в ответ на каждый раз, когда бот присылает Python'у данные из Minecraft'а.

        :param name: Имя аккаунта Minecraft, к которому подключается бот.
        :param program: Функция, которая определяет поведение бота.

        Аргумент **program** это функция следующего вида:

        .. code-block:: python
            def example(**_) -> str:
                return 'move_forward'
        
        Она может принимать какие угодно **названные** аргументы и должна обязательно возвращать строку. Подробное описание этой функции и её возможностей будет приведено где-то *не тут*.
        '''
        self.allowed_bots[name] = program
        if self.debug:
            print(f'Разрешил подключаться боту {name}!')
    
    def close(self):
        '''Закрыть!'''
        self.is_running = False

class _MinecraftConnection(threading.Thread):
    '''Скрытый от общих глаз, этот поток занимается одним конкретным ботом Майнкрафта, чтобы не мешать серверу принимать новых.'''
    def __init__(self, solver: AbsoluteSolver, name: str, bot: socket.socket, program: _ProgramCallable):
        super().__init__()
        self.solver = solver
        self.name = name
        self.bot = bot
        self.is_running = True
        self.program = program
        self.start()

    def actual_program(self, **data):
        '''Бот Minecraft передаёт JSON-словарь с огромным количеством данных - о сущностях, блоках, инвентаре и т.п. Модуль построен так, что функции-программе для бота не обязательно обрабатывать весь словарь, она может посмотреть только в информацию об инвентаре, а на остальное забить. Эта меж-функция фильтрует подаваемый в self.program словарь с данным майнкрафта, оставляя только те ключи, которые заданы в self.program'''
        kwargs = set(inspect.getfullargspec(self.program)[4])
        unwanted_keys = list(set(data.keys())-kwargs)
        for key in unwanted_keys:
            del data[key]
        return self.program(**data)

    def run(self):
        try:
            while self.is_running:
                request_length = int.from_bytes(self.bot.recv(2))
                request = self.bot.recv(request_length*2).decode('utf-16-be')
                if self.solver.debug:
                    print(f'Бот {self.name} прислал запрос: {request}')

                data = _decode_data(request)
                response = self.actual_program(**data)

                self.bot.sendall((chr(len(response))+response).encode('utf-16-be'))
                if self.solver.debug:
                    print(f'Отослал боту {self.name} команду: {response}')
            self.bot.close()
        except OSError:
            pass
        self.solver.bots.remove(self)
        if self.solver.debug:
            print(f'Бот {self.name} завершил работу')

    def close(self):
        self.is_running = False

if __name__=='__main__':
    start = time()
    solver = AbsoluteSolver('127.0.0.1', 2323, True)

    def program():
        return 'move_forward'

    # Замените Test23 на никнейм вашего игрока Майнкрафт
    solver.add('Test23', program)

    while True:
        try:
            current = time()
            if current-start > 15:
                solver.close()
                break
        except KeyboardInterrupt:
            solver.close()
            break