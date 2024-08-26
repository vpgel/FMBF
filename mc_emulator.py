import socket, json
from time import time

# Библиотека для красивого дебаггинга. Не могу от неё оторваться.
# Если ещё кто-то нарвётся на этот код, вам придётся поставить её через cmd:
# > pip install icecream
from icecream import ic

# Это тестовые данные "как будто с клиента майнкрафта", загруженные в строку string
data = json.load(open('data.json'))
data_string = json.dumps(data)

# Бесконечно пытаюсь подсоединиться к серверу
client = socket.socket()
while True:
    try:
        ic('Попытка не пытка...')
        client.connect(('127.0.0.1', 2323))
        break
    except ConnectionRefusedError:
        pass
print(client)

request = data_string
client.sendall((chr(len(request))+request).encode('utf-16-be'))
ic(f'Отослал серверу строчку: {request}')

# Через 30 секунд клиент отключится от сервера, а до той поры постоянно отправляет серверу строчку 'Test23'
start = time()

while True:#time()-start < 10:
    try:
        response_length = int.from_bytes(client.recv(2))
        response = client.recv(response_length*5)
        ic(f'Ответ сервера: {response.decode("utf-16-be")}')

        request = data_string
        client.sendall((chr(len(request))+request).encode('utf-16-be'))
        ic(f'Отослал серверу строчку: {request}')
    except ConnectionAbortedError:
        break

client.close()