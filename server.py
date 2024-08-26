import socket, _thread, sys
from time import sleep

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
        conn = None
        while True:
            try:
                conn, addr = server.accept()
                _thread.start_new_thread(handle_connection, (conn, addr))
            except KeyboardInterrupt:
                print(1)
                if conn == None:
                    conn = socket.socket(socket.AF_INET, socket.SOCK_STREAM).connect((host, port))
                conn.close()
                break


def handle_connection(conn: socket.socket, addr):
    '''Обработка соединения клиента с сервером'''
    with conn:
        print(f'[ИНФО] {addr} подключился')

        # Эта переменная станет False, если клиент (т.е. Майнкрафт) закроет соединение -
        # закроет мир или Майнкрафт вылетит.
        online = True

        while online:
            try: 
                len = int.from_bytes(conn.recv(2))
                msg = conn.recv(len)
            except (ConnectionAbortedError): online = False
            print(msg.decode('utf-16-be'))
            if not online:
                break
            request = request.decode()
            print(f'[Запрос от {addr}] {request}')


            # На этом этапе есть входные данные request.
            # На их основе создай переменную command, оканчивающуюся \n.
            response = 'turn_right\n'


            # Отправка ответа
            conn.sendall(chr(len(response)).encode('utf-16-be'))
            conn.sendall(response.encode('utf-16-be'))
            print(f'[Ответ для {addr}] {response}')
    print(f'[ИНФО] {addr} отключился')


if __name__=='__main__':
    start_server()