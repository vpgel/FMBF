import socket, _thread

def start_server(host: str = 'localhost', port: int = 2323):
    # Тело программы
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as server:

        # Сервер привязывается к IP и порту, и начинает слушать входящие соединения.
        server.bind((host, port))
        server.listen()
        print(f'[ИНФО] Сервер запущен на {host}:{port}')

        # Сервер постоянно ждёт входящего соединения, и как только он его ловит, то передаёт
        # его функции handle_connection, засунутой в поток. После чего снова ждёт входящего соединения,
        # и так до бесконечности. Многопоточный сервер!
        while True:
            conn, addr = server.accept()
            _thread.start_new_thread(handle_connection, (conn, addr))


def handle_connection(conn: socket.socket, addr):
    '''Обработка соединения клиента с сервером'''
    with conn:
        print(f'[ИНФО] {addr} подключился')

        # Эта переменная станет False, если клиент (т.е. Майнкрафт) закроет соединение -
        # закроет мир или Майнкрафт вылетит.
        online = True

        while online:
            request = b'' # В этой переменной хранятся данные, выкачанные из входящего соединения.

            # Входящее соединение выкачивается порциями по 1024 байта.
            while True:
                try:
                    data = conn.recv(1024)
                except (ConnectionResetError, ConnectionAbortedError):
                    # conn.recv выдаёт ошибку, если соединение с клиентом прерывается.
                    online = False
                    break
                request += data # порции по 1024 байта прибавляются к конечному запросу

                # \n.\n - это флаг, означающий оканчивание сообщения. Его необходимо отправлять на сервер, 
                # чтобы он знал, когда завершать выкачивание.
                # .\n отбрасывается у конечного запроса.
                if len(request)==0 or (b'\n.\n' in request):
                    request = request[:-2]
                    break
            if not online:
                break
            request = request.decode()
            print(f'[Запрос от {addr}] {request}')


            # На этом этапе есть входные данные request.
            # На их основе создай переменную command, оканчивающуюся \n.
            response = 'turn_right\n'


            # Отправка ответа
            conn.sendall(response.encode())
            print(f'[Ответ для {addr}] {response}')
    print(f'[ИНФО] {addr} отключился')


if __name__=='__main__':
    start_server()