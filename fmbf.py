import threading, socket
from icecream import ic

class MinecraftConnection():
    def __init__(self, name: str, handler: callable, host='127.0.0.1', port=2323):
        self.name = name
        self.handler = handler
        self.host = host
        self.port = port
        self.server = socket.socket()
        self.server.bind((self.host, self.port))
        self.client = None
        self.isRunning = False
        self.run()
    
    def run(self):
        self.server.listen()
        self.server.settimeout(0.5)
        try:
            while True:
                self.client, _ = self.server.accept()
                
                request_length = int.from_bytes(self.client.recv(2))
                request = self.client.recv(request_length*2).decode('utf-16-be')
                ic(request)
                if request==self.name:
                    self.isRunning = True
                    break
                else:
                    self.client.close()
        except socket.timeout:
            pass
        except KeyboardInterrupt:
            self.server.close()
        threading.Thread(target=self.handle_connection).start()
    
    def handle_connection(self):
        while self.isRunning:
            response = self.handler(request)
            self.client.sendall((chr(len(response))+response).encode('utf-16-be'))
            ic(response)

            request_length = int.from_bytes(self.client.recv(2))
            request = self.client.recv(request_length*2).decode('utf-16-be')
            ic(request)